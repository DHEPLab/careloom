package edu.stanford.fsi.reap.service;

import edu.stanford.fsi.reap.dto.AppLessonDTO;
import edu.stanford.fsi.reap.dto.AppOfflineLessonDTO;
import edu.stanford.fsi.reap.dto.VisitResultDTO;
import edu.stanford.fsi.reap.entity.*;
import edu.stanford.fsi.reap.entity.Module;
import edu.stanford.fsi.reap.entity.enumerations.VisitStatus;
import edu.stanford.fsi.reap.pojo.Domain;
import edu.stanford.fsi.reap.repository.*;
import edu.stanford.fsi.reap.security.SecurityUtils;
import edu.stanford.fsi.reap.utils.BabyAge;
import edu.stanford.fsi.reap.utils.DateRange;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hookszhang
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class LessonService {

  private final LessonRepository repository;
  private final LessonScheduleRepository lessonScheduleRepository;
  private final ModuleRepository moduleRepository;
  private final VisitRepository visitRepository;
  private final QuestionnaireRepository questionnaireRepository;

  public LessonService(
      LessonRepository repository,
      LessonScheduleRepository lessonScheduleRepository,
      ModuleRepository moduleRepository,
      VisitRepository visitRepository,
      QuestionnaireRepository questionnaireRepository) {
    this.repository = repository;
    this.lessonScheduleRepository = lessonScheduleRepository;
    this.moduleRepository = moduleRepository;
    this.visitRepository = visitRepository;
    this.questionnaireRepository = questionnaireRepository;
  }

  public Optional<AppLessonDTO> findAvailable(Baby baby, LocalDate baseline) {
    return match(baby, baseline)
        .map(
            lesson -> {
              List<String> moduleNames = getModuleNames(lesson);
              return AppLessonDTO.builder()
                  .id(lesson.getId())
                  .name(lesson.getName())
                  .moduleNames(moduleNames)
                  .build();
            });
  }

  public List<String> getModuleNames(Lesson lesson) {
    List<Long> moduleIds =
        lesson.getModules().stream()
            .map(domain -> Long.valueOf(domain.getValue()))
            .collect(Collectors.toList());
    return moduleRepository.findNamesInIdList(moduleIds);
  }

  public Optional<Lesson> match(Baby baby, LocalDate baseline) {
    log.debug(
        "Match baby lesson, baby stage: {}, baseline: {}, edc: {}, birthday: {}",
        baby.getStage(),
        baseline,
        baby.getEdc(),
        baby.getBirthday());
    List<VisitResultDTO> notStarted = visitRepository.findByBabyIdAndNotStarted(baby.getId());
    if (notStarted.size() > 0) {
      return Optional.empty();
    }

    List<LessonSchedule> schedules =
        lessonScheduleRepository.findByCurriculumIdAndStage(
            baby.getCurriculum().getId(), baby.getStage());
    List<Lesson> lessons =
        repository.findByCurriculumIdAndStage(baby.getCurriculum().getId(), baby.getStage());
    int days = BabyAge.days(baby, baseline);
    log.debug("Baby days: {}", days);

    return matchSchedule(schedules, days)
        .flatMap(schedule -> matchLesson(schedule, lessons, baby.getId()));
  }

  public Optional<List<LocalDate>> visitDateRange(Baby baby, LocalDate baseline) {
    log.debug(
        "Match baby lesson, baby stage: {}, baseline: {}, edc: {}, birthday: {}",
        baby.getStage(),
        baseline,
        baby.getEdc(),
        baby.getBirthday());
    List<VisitResultDTO> notStarted = visitRepository.findByBabyIdAndNotStarted(baby.getId());
    if (notStarted.size() > 0) {
      return Optional.empty();
    }

    List<LessonSchedule> schedules =
        lessonScheduleRepository.findByCurriculumIdAndStage(
            baby.getCurriculum().getId(), baby.getStage());
    List<Lesson> lessons =
        repository.findByCurriculumIdAndStage(baby.getCurriculum().getId(), baby.getStage());
    int days = BabyAge.days(baby, baseline);
    log.debug("Baby days: {}", days);

    return matchSchedule(schedules, days)
        .flatMap(
            schedule ->
                matchLesson(schedule, lessons, baby.getId())
                    .map(
                        lesson ->
                            DateRange.visit(
                                schedule, lesson, BabyAge.days(baby, baseline), baseline)));
  }

  public Optional<List<LocalDate>> visitDateRange(Baby baby, Lesson lesson, LocalDate baseline) {
    List<LessonSchedule> schedules =
        lessonScheduleRepository.findByCurriculumIdAndStage(
            baby.getCurriculum().getId(), baby.getStage());
    return schedules.stream()
        .filter(
            schedule ->
                schedule.getLessons().stream()
                    .anyMatch(domain -> domain.longValue().equals(lesson.getId())))
        .findFirst()
        .map(schedule -> DateRange.visit(schedule, lesson, BabyAge.days(baby, baseline), baseline));
  }

  /** 大纲区间 用 stage 两种 的 days时间区间匹配 第一个 大纲区间 */
  private Optional<LessonSchedule> matchSchedule(List<LessonSchedule> schedules, int days) {
    return schedules.stream().filter(schedule -> schedule.includes(days)).findFirst();
  }

  private Optional<Lesson> matchLesson(LessonSchedule schedule, List<Lesson> lessons, Long babyId) {
    return schedule.getLessons().stream()
        .filter(
            domain -> {
              Long lessonId = domain.longValue();
              List<Visit> visits = visitRepository.findByLessonIdAndBabyId(lessonId, babyId);
              for (Visit visit : visits) {
                log.debug(
                    "Founded visit by lesson id: {} and baby Id: {}, status: {}",
                    lessonId,
                    babyId,
                    visit.getStatus());
                // The lesson that have expired need to be restart, and the lesson in other status
                // have been done
                if (!VisitStatus.EXPIRED.equals(visit.getStatus())) {
                  return false;
                }
              }
              return true;
            })
        .findFirst()
        .flatMap(
            domain ->
                lessons.stream()
                    .filter(lesson -> lesson.getId().equals(domain.longValue()))
                    .findFirst());
  }

  public List<AppOfflineLessonDTO> appOfflineLessons(List<Lesson> lessons, List<Module> modules) {
    Long projectId = SecurityUtils.getProjectId();
    return lessons.stream()
        .filter(
            target -> {
              return target.getProjectId() != null && target.getProjectId().equals(projectId);
            })
        .map(
            lesson ->
                AppOfflineLessonDTO.builder()
                    .id(lesson.getId())
                    .name(lesson.getName())
                    .description(lesson.getDescription())
                    .questionnaireAddress(lesson.getQuestionnaireAddress())
                    .questionnaire(mapBasicQuestionnaire(lesson.getQuestionnaire()))
                    .modules(
                        lesson.getModules().stream()
                            .map(domain -> mapBasicModule(domain, modules))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()))
                    .build())
        .collect(Collectors.toList());
  }

  private AppOfflineLessonDTO.BasicQuestionnaire mapBasicQuestionnaire(Questionnaire qt) {
    if (qt == null) {
      return null;
    }
    return new AppOfflineLessonDTO.BasicQuestionnaire(qt.getId(), qt.getName(), qt.getQuestions());
  }

  private AppOfflineLessonDTO.BasicModule mapBasicModule(Domain domain, List<Module> modules) {
    return modules.stream()
        .filter(module -> domain.longValue().equals(module.getId()))
        .findFirst()
        .map(
            module ->
                new AppOfflineLessonDTO.BasicModule(
                    module.getId(), module.getNumber(), module.getName()))
        .orElse(null);
  }

  public void renameModuleLabel(Long id, String number) {
    repository
        .findByModuleId(id)
        .forEach(
            lesson -> {
              lesson.setModules(
                  lesson.getModules().stream()
                      .peek(
                          domain -> {
                            if (domain.longValue().equals(id)) {
                              domain.setLabel(number);
                            }
                          })
                      .collect(Collectors.toList()));
              if (lesson.getProjectId() == null) {
                lesson.setProjectId(SecurityUtils.getProjectId());
              }
              repository.save(lesson);
            });
  }

  public List<String> getModuleNames(ExportLesson lesson) {
    List<Long> moduleIds =
        lesson.getModules().stream()
            .map(domain -> Long.valueOf(domain.getValue()))
            .collect(Collectors.toList());
    return moduleRepository.findNamesInIdList(moduleIds);
  }

  public Optional<List<LocalDate>> visitDateRange(
      Baby baby, ExportLesson lesson, LocalDate baseline) {
    List<LessonSchedule> schedules =
        lessonScheduleRepository.findByCurriculumIdAndStage(
            baby.getCurriculum().getId(), baby.getStage());
    return schedules.stream()
        .filter(
            schedule ->
                schedule.getLessons().stream()
                    .anyMatch(domain -> domain.longValue().equals(lesson.getId())))
        .findFirst()
        .map(schedule -> DateRange.visit(schedule, lesson, BabyAge.days(baby, baseline), baseline));
  }
}
