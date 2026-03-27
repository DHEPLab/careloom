package edu.stanford.fsi.reap.service;

import static edu.stanford.fsi.reap.entity.enumerations.CurriculumBranch.MASTER;

import edu.stanford.fsi.reap.dto.CurriculumDTO;
import edu.stanford.fsi.reap.dto.CurriculumResultDTO;
import edu.stanford.fsi.reap.entity.Baby;
import edu.stanford.fsi.reap.entity.Curriculum;
import edu.stanford.fsi.reap.entity.Lesson;
import edu.stanford.fsi.reap.entity.LessonSchedule;
import edu.stanford.fsi.reap.entity.enumerations.CurriculumBranch;
import edu.stanford.fsi.reap.entity.enumerations.VisitStatus;
import edu.stanford.fsi.reap.pojo.Domain;
import edu.stanford.fsi.reap.repository.*;
import edu.stanford.fsi.reap.security.SecurityUtils;
import edu.stanford.fsi.reap.utils.Diff;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class CurriculumService {

  private final CurriculumRepository repository;
  private final VisitRepository visitRepository;
  private final LessonRepository lessonRepository;
  private final LessonScheduleRepository scheduleRepository;
  private final BabyRepository babyRepository;

  public CurriculumService(
      CurriculumRepository repository,
      VisitRepository visitRepository,
      LessonRepository lessonRepository,
      LessonScheduleRepository scheduleRepository,
      BabyRepository babyRepository) {
    this.repository = repository;
    this.visitRepository = visitRepository;
    this.lessonRepository = lessonRepository;
    this.scheduleRepository = scheduleRepository;
    this.babyRepository = babyRepository;
  }

  public Optional<CurriculumResultDTO> publish(CurriculumDTO dto) {
    log.info("publish curriculum, id: {}, name: {}", dto.getId(), dto.getName());

    if (dto.getId() == null) {
      return Optional.ofNullable(createPublished(dto));
    }

    return repository
        .findById(dto.getId())
        .map(
            curriculum -> {
              if (curriculum.draftBranch()) {
                return publishDraftBranch(dto, curriculum.getSource());
              }
              return updatePublished(dto);
            });
  }

  public Optional<CurriculumResultDTO> draft(CurriculumDTO dto) {
    log.info("create/update draft curriculum, id: {}, name: {}", dto.getId(), dto.getName());
    if (dto.getId() == null) {
      return Optional.ofNullable(createUnpublished(dto));
    }

    return repository
        .findById(dto.getId())
        .map(
            curriculum -> {
              if (curriculum.publishedMasterBranch()) {
                return createDraftBranch(dto, curriculum);
              }
              return updateUnpublished(dto);
            });
  }

  private CurriculumResultDTO updateUnpublished(CurriculumDTO dto) {
    return update(dto, false);
  }

  private CurriculumResultDTO createUnpublished(CurriculumDTO dto) {
    return create(dto, false);
  }

  private CurriculumResultDTO updatePublished(CurriculumDTO dto) {
    return update(dto, true);
  }

  private CurriculumResultDTO createPublished(CurriculumDTO dto) {
    return create(dto, true);
  }

  private CurriculumResultDTO createDraftBranch(CurriculumDTO dto, Curriculum source) {
    Curriculum curriculum =
        Curriculum.builder().branch(CurriculumBranch.DRAFT).published(false).source(source).build();
    mapper(dto, curriculum);
    if (curriculum.getProjectId() == null) {
      curriculum.setProjectId(SecurityUtils.getProjectId());
    }
    repository.save(curriculum);

    // create draft branch, clone lessons, schedules, bind source
    dto.getSessions()
        .forEach(
            lesson -> {
              if (lesson.getId() != null) {
                lesson.setSource(Lesson.builder().id(lesson.getId()).build());
              }
              lesson.setId(null);
            });
    dto.getSchedules()
        .forEach(
            schedule -> {
              if (schedule.getId() != null) {
                schedule.setSource(LessonSchedule.builder().id(schedule.getId()).build());
              }
              schedule.setId(null);
            });

    return save(curriculum, dto.getSessions(), dto.getSchedules());
  }

  private CurriculumResultDTO publishDraftBranch(CurriculumDTO draft, Curriculum source) {
    mapper(draft, source);
    onUpdate(source);
    if (source.getProjectId() == null) {
      source.setProjectId(SecurityUtils.getProjectId());
    }
    repository.save(source);

    // publish source lesson if exist
    draft.setSessions(
        draft.getSessions().stream()
            .map(
                lesson -> {
                  if (lesson.getId() == null) return lesson;
                  return lessonRepository
                      .findById(lesson.getId())
                      .map(
                          consumer -> {
                            if (consumer.getSource() != null) {
                              return consumer.getSource().copyFrom(consumer);
                            }
                            return consumer;
                          })
                      .orElse(lesson);
                })
            .collect(Collectors.toList()));

    // publish source schedule if exist
    draft.setSchedules(
        draft.getSchedules().stream()
            .map(
                schedule -> {
                  if (schedule.getId() == null) return schedule;
                  return scheduleRepository
                      .findById(schedule.getId())
                      .map(
                          consumer -> {
                            if (consumer.getSource() != null) {
                              return consumer.getSource().copyFrom(consumer);
                            }
                            return consumer;
                          })
                      .orElse(schedule);
                })
            .collect(Collectors.toList()));

    CurriculumResultDTO resultDTO = save(source, draft.getSessions(), draft.getSchedules());
    // delete draft curriculum
    repository.deleteById(draft.getId());
    return resultDTO;
  }

  /**
   * manually setting audit fields to resolve updates to classes and schedules does not change the
   * most recent release time
   */
  private void onUpdate(Curriculum source) {
    source.setLastModifiedAt(LocalDateTime.now());
    source.setLastModifiedBy(SecurityUtils.getUsername());
  }

  private CurriculumResultDTO update(CurriculumDTO dto, boolean published) {
    Curriculum curriculum =
        repository
            .findById(dto.getId())
            .orElseThrow(() -> new RuntimeException("Not found curriculum by id " + dto.getId()));
    mapper(dto, curriculum);
    curriculum.setPublished(published);
    onUpdate(curriculum);
    repository.save(curriculum);
    return save(curriculum, dto.getSessions(), dto.getSchedules());
  }

  private CurriculumResultDTO save(
      Curriculum curriculum, List<Lesson> lessons, List<LessonSchedule> schedules) {
    int numberOfLessonDeleted = saveLessons(curriculum, lessons);
    int numberOfScheduleCleaned = saveSchedules(curriculum, lessons, schedules);

    log.info(
        "deleted {} lessons, updated {} lessons; deleted {} schedule, updated {} schedule",
        numberOfLessonDeleted,
        lessons.size(),
        numberOfScheduleCleaned,
        lessons);
    return mapper(curriculum, lessons, schedules);
  }

  private CurriculumResultDTO create(CurriculumDTO dto, boolean published) {
    Curriculum curriculum = Curriculum.builder().branch(MASTER).published(published).build();
    mapper(dto, curriculum);
    if (curriculum.getProjectId() == null) {
      curriculum.setProjectId(SecurityUtils.getProjectId());
    }
    repository.save(curriculum);
    return save(curriculum, dto.getSessions(), dto.getSchedules());
  }

  private int saveLessons(Curriculum curriculum, List<Lesson> lessons) {
    List<Lesson> oldLessons = lessonRepository.findByCurriculumId(curriculum.getId());
    List<Lesson> deleted = Diff.deletedLessons(lessons, oldLessons);

    for (Lesson lesson : deleted) {
      if (curriculum.publishedMasterBranch()) {
        visitRepository.deleteByLessonIdAndStatus(lesson.getId(), VisitStatus.NOT_STARTED);
      }
      lessonRepository.deleteById(lesson.getId());
    }

    for (Lesson lesson : lessons) {
      lesson.setCurriculum(curriculum);
      if (lesson.getProjectId() == null) {
        lesson.setProjectId(SecurityUtils.getProjectId());
      }
      lessonRepository.save(lesson);
    }
    return deleted.size();
  }

  private int saveSchedules(
      Curriculum curriculum, List<Lesson> lessons, List<LessonSchedule> schedules) {
    List<LessonSchedule> oldSchedules = scheduleRepository.findByCurriculumId(curriculum.getId());
    List<LessonSchedule> deleted = Diff.deletedLessonSchedules(schedules, oldSchedules);

    for (LessonSchedule schedule : deleted) {
      scheduleRepository.deleteById(schedule.getId());
    }

    for (LessonSchedule schedule : schedules) {
      // find lesson by name from dto lessons, if present, set domain lesson id
      for (Domain lesson : schedule.getLessons()) {
        lesson.setValue(
            findFirst(lessons, lesson)
                .map(consumer -> String.valueOf(consumer.getId()))
                .orElse(null));
      }

      // filter domain value is null of Lesson
      schedule.setLessons(
          schedule.getLessons().stream()
              .filter(
                  domain -> {
                    boolean isNull = domain.getValue() == null;
                    if (isNull) {
                      log.warn(
                          "No corresponding Lesson was found, curriculum: {}, lesson number: {}",
                          curriculum.getId(),
                          domain.getLabel());
                    }
                    return !isNull;
                  })
              // sort lessons domain by start of applicable days asc
              .sorted(
                  Comparator.comparingInt(
                      o -> findFirst(lessons, o).map(Lesson::getStartOfApplicableDays).orElse(-1)))
              .collect(Collectors.toList()));

      schedule.setCurriculum(curriculum);
      scheduleRepository.save(schedule);
    }
    return deleted.size();
  }

  private Optional<Lesson> findFirst(List<Lesson> lessons, Domain lesson) {
    return lessons.stream()
        .filter(predicate -> predicate.getNumber().equals(lesson.getLabel()))
        .findFirst();
  }

  private CurriculumResultDTO mapper(
      Curriculum curriculum, List<Lesson> lessons, List<LessonSchedule> schedules) {
    return CurriculumResultDTO.builder()
        .id(curriculum.getId())
        .name(curriculum.getName())
        .description(curriculum.getDescription())
        .lastPublishedAt(curriculum.getLastModifiedAt())
        .sessions(lessons)
        .schedules(schedules)
        .branch(curriculum.getBranch())
        .published(curriculum.isPublished())
        .sourceId(curriculum.getSource() == null ? null : curriculum.getSource().getId())
        .build();
  }

  private void mapper(CurriculumDTO dto, Curriculum curriculum) {
    curriculum.setName(dto.getName());
    curriculum.setDescription(dto.getDescription());
  }

  public Optional<CurriculumResultDTO> findById(Long id) {
    return repository
        .findById(id)
        .map(
            curriculum -> {
              List<Lesson> lessons = lessonRepository.findByCurriculumId(curriculum.getId());
              List<LessonSchedule> schedules =
                  scheduleRepository.findByCurriculumId(curriculum.getId());
              return Optional.of(mapper(curriculum, lessons, schedules));
            })
        .orElse(Optional.empty());
  }

  public void delete(Curriculum curriculum) {
    if (curriculum.draftBranch()) {
      repository.deleteById(curriculum.getId());
      return;
    }

    List<Baby> babies = babyRepository.findBabiesByCurriculumId(curriculum.getId());
    log.info(
        "Delete master branch curriculum, id: {}, number of babies bound {}",
        curriculum.getId(),
        babies);

    for (Baby baby : babies) {
      baby.setCurriculum(null);
      Long count = visitRepository.deleteByBabyIdAndStatus(baby.getId(), VisitStatus.NOT_STARTED);
      log.info(
          "Release baby curriculum, baby id: {}, number of not started visit deleted {}",
          baby.getId(),
          count);
    }

    repository
        .findFirstBySourceId(curriculum.getId())
        .ifPresent(
            draft -> {
              repository.deleteById(draft.getId());
              log.info("Delete the draft branch, id: {}", draft.getId());
            });
    repository.deleteById(curriculum.getId());
  }
}
