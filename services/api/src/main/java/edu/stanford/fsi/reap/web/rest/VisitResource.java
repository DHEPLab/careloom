package edu.stanford.fsi.reap.web.rest;

import static edu.stanford.fsi.reap.entity.enumerations.QuestionnaireBranch.MASTER;

import edu.stanford.fsi.reap.dto.*;
import edu.stanford.fsi.reap.entity.*;
import edu.stanford.fsi.reap.entity.enumerations.ErrorLogType;
import edu.stanford.fsi.reap.entity.enumerations.VisitStatus;
import edu.stanford.fsi.reap.repository.*;
import edu.stanford.fsi.reap.security.SecurityUtils;
import edu.stanford.fsi.reap.service.LessonService;
import edu.stanford.fsi.reap.service.VisitService;
import edu.stanford.fsi.reap.utils.DateRange;
import edu.stanford.fsi.reap.utils.ZonedDateTimeUtil;
import edu.stanford.fsi.reap.web.rest.errors.BadRequestAlertException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author hookszhang
 */
@Slf4j
@RestController
@RequestMapping("/api/visits")
public class VisitResource {

  private final LessonService lessonService;
  private final VisitService service;
  private final VisitRepository repository;
  private final BabyRepository babyRepository;
  private final QuestionnaireRepository questionnaireRepository;
  private final SysErrorLogRepository sysErrorLogRepository;
  private final LessonRepository lessonRepository;
  private final BabyUpdateInfoRepository babyUpdateInfoRepository;

  public VisitResource(
      LessonService lessonService,
      VisitService service,
      VisitRepository repository,
      BabyRepository babyRepository,
      QuestionnaireRepository questionnaireRepository,
      SysErrorLogRepository sysErrorLogRepository,
      LessonRepository lessonRepository,
      BabyUpdateInfoRepository babyUpdateInfoRepository) {
    this.lessonService = lessonService;
    this.service = service;
    this.repository = repository;
    this.babyRepository = babyRepository;
    this.questionnaireRepository = questionnaireRepository;
    this.sysErrorLogRepository = sysErrorLogRepository;
    this.lessonRepository = lessonRepository;
    this.babyUpdateInfoRepository = babyUpdateInfoRepository;
  }

  private String mapMsg(LocalDateTime visitTime) {
    return String.format(
        "您选择的%d年%d月%d日，没有合适的课堂，请重新创建家访并通知家长。",
        visitTime.getYear(), visitTime.getMonth().getValue(), visitTime.getDayOfMonth());
  }

  /** 创建家访（含提交离线时创建的家访） 家访日期不对的时候要记录错误日期到数据库，这种情况多是离线时提交的，前端会显示错误日志 */
  @PostMapping
  public void createVisit(@Valid @RequestBody VisitDTO dto) {
    LocalDate visitDate =
        LocalDate.of(
            dto.getVisitTime().getYear(),
            dto.getVisitTime().getMonthValue(),
            dto.getVisitTime().getDayOfMonth());
    // 家访日期不能早于今天
    if (visitDate.isBefore(LocalDate.now())) {
      saveErrorLog(dto);
      throw new BadRequestAlertException("error.visit.pastDate", mapMsg(dto.getVisitTime()));
    }

    // 宝宝有未完成的家访不能再次创建新家访
    if (repository.findByBabyIdAndStatus(dto.getBabyId(), VisitStatus.NOT_STARTED).isPresent()) {
      throw new BadRequestAlertException("error.visit.alreadyExists");
    }

    Long userId = SecurityUtils.getUserId();
    Baby baby =
        babyRepository
            .findByIdAndChwIdAndDeletedFalse(dto.getBabyId(), userId)
            .orElseThrow(() -> new BadRequestAlertException("不存在该宝宝，无法创建家访"));
    // 审核中的宝宝不能创建家访
    if (!baby.getApproved()) {
      Optional<BabyUpdateInfo> babyUpdateInfoOptional =
          this.babyUpdateInfoRepository.findByBabyIdAndDeleted(baby.getId(), false);
      if (!babyUpdateInfoOptional.isPresent() || !babyUpdateInfoOptional.get().getUpdateNormal()) {
        throw new BadRequestAlertException("error.visit.babyNotApproved");
      }
    }

    Lesson lesson =
        lessonRepository
            .findById(dto.getLessonId())
            .orElseThrow(() -> new BadRequestAlertException("error.visit.lessonNotFound"));

    Optional<List<LocalDate>> visitDateRange =
        lessonService.visitDateRange(baby, lesson, LocalDate.now());
    if (!visitDateRange.isPresent() || !DateRange.contains(visitDateRange.get(), visitDate)) {
      log.warn(
          "{} 宝宝的 {} 课程的家访时间段不符合已选择的家访时间, 课程时间段：{}, 已选: {},",
          baby.getId(),
          lesson.getId(),
          visitDateRange.orElse(null),
          visitDate);
      saveErrorLog(dto);
      throw new BadRequestAlertException(mapMsg(dto.getVisitTime()));
    }

    service.create(dto, userId);
  }

  private void saveErrorLog(@RequestBody @Valid VisitDTO dto) {
    sysErrorLogRepository.save(
        ErrorLog.builder()
            .msg(mapMsg(dto.getVisitTime()))
            .type(ErrorLogType.APP_CREATE_VISIT)
            .typeId(dto.getBabyId())
            .user(User.builder().id(SecurityUtils.getUserId()).build())
            .build());
  }

  @GetMapping("/{id}/date-range")
  public ResponseEntity<List<LocalDate>> getVisitDateRange(@PathVariable Long id) {
    return repository
        .findById(id)
        .flatMap(
            visit ->
                lessonService.visitDateRange(visit.getBaby(), visit.getLesson(), LocalDate.now()))
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public void changeVisitTime(@PathVariable Long id, @RequestBody @Valid VisitTimeWrapper wrapper) {
    repository
        .findByIdAndChwIdOrBabyChwId(id, SecurityUtils.getUserId())
        .ifPresent(
            visit -> {
              if (!VisitStatus.NOT_STARTED.equals(visit.getStatus())) {
                throw new BadRequestAlertException("error.visit.invalidStatus");
              }
              visit.yearMonthDay(wrapper.getVisitTime()).setVisitTime(wrapper.getVisitTime());
              repository.save(visit);
            });
  }

  @PutMapping("{id}/status")
  public void updateVisitStatus(
      @PathVariable Long id, @Valid @RequestBody UpdateVisitStatusWrapper wrapper) {
    repository
        .findByIdAndChwIdOrBabyChwId(id, SecurityUtils.getUserId())
        .ifPresent(visit -> service.updateVisitStatus(visit, wrapper));
  }

  @PutMapping("{id}/remark")
  public void addRemark(@PathVariable Long id, @RequestBody @Valid RemarkWrapper wrapper) {
    repository
        .findByIdAndChwIdOrBabyChwId(id, SecurityUtils.getUserId())
        .ifPresent(
            visit -> {
              if (visit.done()) {
                throw new BadRequestAlertException("error.visit.completed");
              }
              visit.setRemark(wrapper.getRemark());
              service.addRemark(visit);
            });
  }

  @GetMapping("/marked-dates")
  public List<String> getCalendarMarkedDates() {
    return service.markedDates(SecurityUtils.getUserId());
  }

  @GetMapping
  public List<VisitResultDTO> getVisits(
      @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return repository.findByDateAndChwId(
        date.getYear(), date.getMonthValue(), date.getDayOfMonth(), SecurityUtils.getUserId());
  }

  @GetMapping("/{id}")
  public ResponseEntity<VisitDetailDTO> getVisit(@PathVariable Long id) {
    return service
        .findById(id, SecurityUtils.getUserId())
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/next")
  public ResponseEntity<VisitDetailDTO> getNextVisit() {
    return service
        .findNext(SecurityUtils.getUserId())
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/questionnaire/{id}")
  public ResponseEntity<Questionnaire> getQuestionnaire(@PathVariable Long id) {
    return questionnaireRepository
        .findById(id)
        .map(
            qt -> {
              ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
              if (MASTER.equals(qt.getBranch())) {
                questionnaireRepository
                    .findBySourceId(qt.getId())
                    .ifPresent(
                        draft -> {
                          builder.header("x-draft-id", String.valueOf(draft.getId()));
                          builder.header(
                              "x-draft-date",
                              ZonedDateTimeUtil.toResponseString(
                                  draft.getLastModifiedAt().atZone(ZoneId.systemDefault())));
                        });
              }
              return builder.body(qt);
            })
        .orElse(ResponseEntity.notFound().build());
  }

  /** CHW 取消 Visit; status 未开始得才能 deleted */
  @DeleteMapping("/{id}")
  public void closeVisit(@PathVariable Long id, @Valid @RequestParam String deleteReason) {
    repository
        .findById(id)
        .ifPresent(
            visit -> {
              if (visit.getStatus().equals(VisitStatus.NOT_STARTED)) {
                visit.setDeleteReason(deleteReason);
                visit.setDeleted(true);
                repository.save(visit);
                repository.deleteById(id);
              } else {
                throw new BadRequestAlertException("error.visit.cannotCancel");
              }
            });
  }

  @GetMapping("/notStartVisit")
  public ResponseEntity<byte[]> reportExcel(
      @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "en") String lang,
      @RequestHeader(value = "X-Timezone", defaultValue = "Etc/UTC") String timezone) {
    byte[] ret = service.reportNotStartVisit(lang, timezone);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=" + UUID.randomUUID().toString() + ".xlsx")
        .body(ret);
  }

  @PostMapping("/upload/location")
  public void uploadLocation(
      @RequestBody @Valid UploadVisitLocationWrapper uploadVisitLocationWrapper) {
    service.handleVisitLocation(uploadVisitLocationWrapper);
  }

  @GetMapping("all/todo")
  public ResponseEntity<List<Visit>> getAllTodoVisit() {
    return ResponseEntity.ok(service.getAllTodoVisit(SecurityUtils.getUserId()));
  }
}
