package edu.stanford.fsi.reap.service;

import edu.stanford.fsi.reap.dto.*;
import edu.stanford.fsi.reap.entity.*;
import edu.stanford.fsi.reap.entity.enumerations.VisitStatus;
import edu.stanford.fsi.reap.repository.*;
import edu.stanford.fsi.reap.security.SecurityUtils;
import edu.stanford.fsi.reap.utils.DistanceUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class VisitService {

  private final CarerRepository carerRepository;
  private final LessonService lessonService;
  private final VisitRepository repository;
  private final ExcelService excelService;
  private final ExportVisitRepository exportVisitRepository;
  private final QuestionnaireRecordRepository questionnaireRecordRepository;
  private final VisitReportService visitReportService;
  private final VisitPositionRecordRepository visitPositionRecordRepository;

  public VisitService(
      CarerRepository carerRepository,
      LessonService lessonService,
      VisitRepository repository,
      ExcelService excelService,
      ExportVisitRepository exportVisitRepository,
      QuestionnaireRecordRepository questionnaireRecordRepository,
      VisitReportService visitReportService,
      VisitPositionRecordRepository visitPositionRecordRepository) {
    this.carerRepository = carerRepository;
    this.lessonService = lessonService;
    this.repository = repository;
    this.excelService = excelService;
    this.exportVisitRepository = exportVisitRepository;
    this.questionnaireRecordRepository = questionnaireRecordRepository;
    this.visitReportService = visitReportService;
    this.visitPositionRecordRepository = visitPositionRecordRepository;
  }

  /**
   * Create Visit
   *
   * @param dto visit dto
   * @param userId chw user id
   * @return Visit
   */
  public Visit create(VisitDTO dto, Long userId) {
    LocalDateTime visitTime = dto.getVisitTime();
    Visit visit =
        Visit.builder()
            .baby(Baby.builder().id(dto.getBabyId()).build())
            .lesson(ExportLesson.builder().id(dto.getLessonId()).build())
            .chw(User.builder().id(userId).build())
            .visitTime(visitTime)
            .status(VisitStatus.NOT_STARTED)
            .build()
            .yearMonthDay(visitTime);
    visit.setProjectId(SecurityUtils.getProjectId());
    return repository.save(visit);
  }

  @Transactional(readOnly = true)
  public List<String> markedDates(Long userId) {
    List<VisitDateDTO> dates = repository.findDateByChwId(userId);
    return dates.stream()
        .map(dto -> LocalDate.of(dto.getYear(), dto.getMonth(), dto.getDay()).toString())
        .collect(Collectors.toList());
  }

  public Optional<VisitDetailDTO> findById(Long id, Long userId) {
    return repository
        .findById(id)
        .map(
            visit -> {
              if (visit.getChw().getId().equals(userId)
                  || visit.getBaby().getChw().getId().equals(userId)) {
                return visit;
              }
              log.warn("Unauthorized access visit, id: {} , user id: {}", id, userId);
              return null;
            })
        .map(this::getVisitDetailDTO);
  }

  /** skip visit when baby approved is false */
  public Optional<VisitDetailDTO> findNext(Long userId) {
    return repository
        .findFirstByChwIdAndStatusOrderByVisitTimeAsc(userId, VisitStatus.NOT_STARTED)
        .map(this::getVisitDetailDTO);
  }

  public VisitDetailDTO getVisitDetailDTO(Visit visit) {
    List<String> moduleNames = lessonService.getModuleNames(visit.getLesson());
    Carer masterCarer =
        carerRepository.findOneByBabyIdAndMasterIsTrue(visit.getBaby().getId()).orElse(null);

    return VisitDetailDTO.builder()
        .id(visit.getId())
        .status(visit.getStatus())
        .visitTime(visit.getVisitTime())
        .startTime(visit.getStartTime())
        .completeTime(visit.getCompleteTime())
        .nextModuleIndex(visit.getNextModuleIndex())
        .remark(visit.getRemark())
        .baby(new AppBabyDTO(visit.getBaby(), masterCarer))
        .lesson(
            AppLessonDTO.builder()
                .id(visit.getLesson().getId())
                .name(visit.getLesson().getName())
                .moduleNames(moduleNames)
                .build())
        .build();
  }

  public void begin(Visit visit) {
    visit.setStatus(VisitStatus.UNDONE);
    repository.save(visit);
  }

  public void moduleDone(Visit visit) {
    visit.setNextModuleIndex(visit.getNextModuleIndex() + 1);
    repository.save(visit);
  }

  public void done(Visit visit) {
    visit.setStatus(VisitStatus.DONE);
    repository.save(visit);
  }

  public void expired(List<Visit> visits) {
    visits.forEach(
        visit -> {
          visit.setStatus(VisitStatus.EXPIRED);
          repository.save(visit);
          visitReportService.saveVisitReport(visit);
        });
  }

  public byte[] reportNotStartVisit(String lang, String timezone) {
    List<VisitStatus> status = Arrays.asList(VisitStatus.NOT_STARTED, VisitStatus.UNDONE);
    List<ExportVisit> visits = exportVisitRepository.findByStatusInOrderByCreatedAtDesc(status);
    return excelService.writeNotStartExcel(visits, lang, timezone);
  }

  public void updateVisitStatus(Visit visit, UpdateVisitStatusWrapper wrapper) {
    if (visit.readonly()) {
      log.warn("忽略修改只读状态的家访请求，visit: {}, Modified to: {}", visit, wrapper);
      return;
    }
    if (visit.notStarted() && wrapper.getStartTime() != null) {
      visit.setStartTime(wrapper.getStartTime());
    }

    visit.setNextModuleIndex(wrapper.getNextModuleIndex());
    visit.setStatus(wrapper.getVisitStatus());

    List<QuestionnaireRecord> list = wrapper.getQuestionnaireRecords();

    if (visit.done()) {
      visit.setCompleteTime(LocalDateTime.now());

      // 家访模块完成，判断问卷有没有完成
      /*List<QuestionnaireRecord> questionnaireRecordList =
              questionnaireRecordRepository.findByVisitIdOrderByTitleNoAsc(visit.getId());
      if ((questionnaireRecordList == null || questionnaireRecordList.size() <= 0)
              && (list == null || list.size() <= 0)) {
          // 没有问卷记录, 也没传问卷
          throw new BadRequestAlertException("请完成问卷后，再提交");
      }*/
    }

    repository.save(visit);

    if (list != null && list.size() > 0) {
      list.forEach(i -> i.setVisit(visit));
      questionnaireRecordRepository.saveAll(list);
    }

    if (visit.done()) {
      // 家访模块完成，收集当前visit记录
      visitReportService.saveVisitReport(visit);
    }
  }

  public void addRemark(Visit visit) {
    // 给状态为过期的 visit 添加备注时要 修改 报表记录表的 visit remark
    if (VisitStatus.EXPIRED.equals(visit.getStatus()))
      visitReportService.updateVisitRemarkInfo(visit);

    repository.save(visit);
  }

  public void handleVisitLocation(UploadVisitLocationWrapper uploadVisitLocationWrapper) {
    repository
        .findById(uploadVisitLocationWrapper.getVisitId())
        .ifPresent(
            (visit) -> {
              Baby baby = visit.getBaby();
              Double distance =
                  DistanceUtils.getDistance(
                      uploadVisitLocationWrapper.getLongitude(),
                      uploadVisitLocationWrapper.getLatitude(),
                      baby.getLongitude(),
                      baby.getLatitude());
              if (visit.getDistance() == null || visit.getDistance() > distance) {
                visit.setDistance(distance);
                repository.save(visit);
              }
              VisitPositionRecord visitPositionRecord =
                  VisitPositionRecord.builder()
                      .visitId(visit.getId())
                      .babyId(uploadVisitLocationWrapper.getBabyId())
                      .longitude(uploadVisitLocationWrapper.getLongitude())
                      .latitude(uploadVisitLocationWrapper.getLatitude())
                      .distance(distance)
                      .build();
              visitPositionRecordRepository.save(visitPositionRecord);
            });
  }

  public List<Visit> getAllTodoVisit(Long userId) {
    return repository.findByChwIdAndStatus(userId, VisitStatus.NOT_STARTED);
  }
}
