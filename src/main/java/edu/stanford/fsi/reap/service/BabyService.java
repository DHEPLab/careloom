package edu.stanford.fsi.reap.service;

import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.databind.JsonNode;
import edu.stanford.fsi.reap.dto.AdminBabyVisitDTO;
import edu.stanford.fsi.reap.dto.AppBabyDTO;
import edu.stanford.fsi.reap.dto.AppCreateBabyDTO;
import edu.stanford.fsi.reap.dto.ImportBabyDto;
import edu.stanford.fsi.reap.entity.*;
import edu.stanford.fsi.reap.entity.enumerations.ActionFromApp;
import edu.stanford.fsi.reap.entity.enumerations.VisitStatus;
import edu.stanford.fsi.reap.repository.*;
import edu.stanford.fsi.reap.security.SecurityUtils;
import edu.stanford.fsi.reap.utils.DateRange;
import edu.stanford.fsi.reap.utils.FieldValueUtil;
import edu.stanford.fsi.reap.web.rest.errors.BadRequestAlertException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * @author hookszhang
 */
@Service
@Transactional
@Slf4j
public class BabyService {

  private final BabyRepository repository;
  private final BabyHistoryRepository historyRepository;
  private final CarerRepository carerRepository;
  private final UserRepository userRepository;
  private final CarerService carerService;
  private final LessonService lessonService;
  private final VisitRepository visitRepository;
  private final ExcelService excelService;
  private final ModelMapper modelMapper;
  private final VisitPositionRecordRepository visitPositionRecordRepository;
  private final AccountOperationRecordService accountOperationRecordService;
  private final BabyModifyRecordRepository babyModifyRecordRepository;
  private final BabyUpdateInfoRepository babyUpdateInfoRepository;
  private final GoogleMapService googleMapService;

  public BabyService(
      BabyRepository repository,
      CarerRepository carerRepository,
      UserRepository userRepository,
      CarerService carerService,
      LessonService lessonService,
      VisitRepository visitRepository,
      ExcelService excelService,
      ModelMapper modelMapper,
      VisitPositionRecordRepository visitPositionRecordRepository,
      AccountOperationRecordService accountOperationRecordService,
      BabyHistoryRepository historyRepository,
      BabyModifyRecordRepository babyModifyRecordRepository,
      BabyUpdateInfoRepository babyUpdateInfoRepository,
      GoogleMapService googleMapService) {
    this.repository = repository;
    this.carerRepository = carerRepository;
    this.userRepository = userRepository;
    this.carerService = carerService;
    this.lessonService = lessonService;
    this.visitRepository = visitRepository;
    this.excelService = excelService;
    this.modelMapper = modelMapper;
    this.visitPositionRecordRepository = visitPositionRecordRepository;
    this.accountOperationRecordService = accountOperationRecordService;
    this.historyRepository = historyRepository;
    this.babyModifyRecordRepository = babyModifyRecordRepository;
    this.babyUpdateInfoRepository = babyUpdateInfoRepository;
    this.googleMapService = googleMapService;
  }

  /** 导出宝宝列表 */
  public byte[] babyRosterReport(String timezone) {
    Long projectId = SecurityUtils.getProjectId();
    List<Baby> list =
        repository.findByOrderByCreatedAtDesc().stream()
            .filter(
                target -> {
                  return target.getProjectId().equals(projectId);
                })
            .collect(Collectors.toList());
    return excelService.generateBabyRoster(list, timezone);
  }

  public void assignChw(Chw chw, Long[] babyIds) {
    Arrays.stream(babyIds)
        .forEach(
            id ->
                repository
                    .findById(id)
                    .ifPresent(
                        baby -> {
                          baby.setChw(chw);
                          repository.save(baby);
                        }));
  }

  public List<Map<String, String>> check(@Valid List<ImportBabyDto> list, String lang) {

    List<Map<String, String>> results = new ArrayList<>();
    list.stream()
        .forEach(
            baby -> {
              boolean present = repository.findFirstByIdentity(baby.getIdentity()).isPresent();
              if (present) {
                Map<String, String> map = new HashMap<>();
                map.put("name", baby.getName());
                map.put("number", baby.getNumber().toString());
                map.put(
                    "matters",
                    "ID: " + baby.getIdentity() + ("zh".equals(lang) ? " 已经存在" : " exists"));
                results.add(map);
                return;
              }

              if (baby.getChw().getChw().getIdentity() != null) {
                boolean chwExtend =
                    userRepository
                        .findOneByChw_Identity(baby.getChw().getChw().getIdentity())
                        .isPresent();
                if (!chwExtend) {
                  Map<String, String> map = new HashMap<>();
                  map.put("name", baby.getName());
                  map.put("number", baby.getNumber().toString());
                  map.put("matters", ("zh".equals(lang) ? " CHW未找到" : "CHW not found"));
                  results.add(map);
                  return;
                }
              }
            });
    return results;
  }

  public void imports(List<ImportBabyDto> babies) {
    babies.stream()
        .forEach(
            dto -> {
              Baby baby = modelMapper.map(dto, Baby.class);
              if (baby.getChw() != null
                  && baby.getChw().getChw() != null
                  && baby.getChw().getChw().getIdentity() != null) {
                User user =
                    userRepository
                        .findOneByChw_Identity(baby.getChw().getChw().getIdentity())
                        .get();
                baby.setChw(Chw.builder().id(user.getId()).build());
              } else {
                baby.setChw(null);
              }
              if (baby.getAssistedFood() == null) {
                baby.setAssistedFood(false);
              }

              if (baby.getLatitude() == null || baby.getLongitude() == null) {
                String address = baby.getArea() ;
                try {
                  JsonNode json = googleMapService.findPlace(address);
                  if (json != null && "OK".equals(json.path("status").asText())) {
                    JsonNode candidates = json.get("candidates");
                    if (candidates != null && !candidates.isEmpty()) {
                      JsonNode elected = candidates.get(0);
                      JsonNode location = elected.get("geometry").get("location");
                      double lat = location.get("lat").asDouble();
                      double lng = location.get("lng").asDouble();
                      baby.setLatitude(lat);
                      baby.setLongitude(lng);
                    } else {
                      log.warn("No candidates found for address '{}'", address);
                    }
                  } else {
                    String status = json != null ? json.path("status").asText() : "null response";
                    log.warn("Find Place API error for address '{}': {}", address, status);
                  }
                } catch (Exception e) {
                  log.warn(
                      "Exception during findPlace for baby '{}': {}",
                      baby.getName(),
                      e.getMessage());
                }
              }

              if (baby.getProjectId() == null) {
                baby.setProjectId(SecurityUtils.getProjectId());
              }

              log.info("要执行存储的宝宝信息是：{}", baby.toString());
              try {
                repository.save(baby);
              } catch (Exception e) {
                log.error("保存宝宝信息异常", e);
                throw new BadRequestAlertException("保存宝宝" + baby.getName() + "信息异常，请检查数据");
              }
              List<Carer> carers =
                  dto.getCares().stream()
                      .map(
                          carer -> {
                            carer.setBaby(baby);
                            return carer;
                          })
                      .collect(Collectors.toList());
              carerRepository.saveAll(carers);
            });
  }

  public void releaseChw(Long id) {
    repository
        .findById(id)
        .ifPresent(
            baby -> {
              baby.setChw(null);
              Long count =
                  visitRepository.deleteByBabyIdAndStatus(baby.getId(), VisitStatus.NOT_STARTED);
              log.info("Release baby chw, clean up {} not started visit", count);
              repository.save(baby);
            });
  }

  public void assignCurriculum(Curriculum curriculum, Long[] babyIds) {
    Arrays.stream(babyIds)
        .forEach(
            id ->
                repository
                    .findById(id)
                    .ifPresent(
                        baby -> {
                          baby.setCurriculum(curriculum);
                          repository.save(baby);
                        }));
  }

  public void releaseCurriculum(Long id) {
    repository
        .findById(id)
        .ifPresent(
            baby -> {
              baby.setCurriculum(null);
              Long count =
                  visitRepository.deleteByBabyIdAndStatus(baby.getId(), VisitStatus.NOT_STARTED);
              log.info("Release baby curriculum, clean up {} not started visit", count);
              repository.save(baby);
            });
  }

  public List<AppBabyDTO> findBabiesAvailableForCreateVisit(
      Long userId, LocalDate visitDate, LocalDate baseline) {
    List<Baby> babies = repository.findByChwIdAndCurriculumIdNotNullAndDeletedFalse(userId);
    List<Baby> targetBabies = new ArrayList<>();
    List<Long> babyIds =
        babies.stream()
            .filter(
                baby -> {
                  return !baby.getApproved();
                })
            .map(Baby::getId)
            .collect(Collectors.toList());
    List<BabyUpdateInfo> babyUpdateInfos =
        babyUpdateInfoRepository.findByBabyIdInAndUpdateNormalTrueAndDeletedFalse(babyIds);
    if (CollUtil.isNotEmpty(babyUpdateInfos)) {
      List<Long> updateBabies =
          babyUpdateInfos.stream().map(BabyUpdateInfo::getBabyId).collect(Collectors.toList());
      babies.stream()
          .forEach(
              baby -> {
                if (baby.getApproved()) {
                  targetBabies.add(baby);
                } else {
                  if (updateBabies.contains(baby.getId())) {
                    targetBabies.add(baby);
                  }
                }
              });
    } else {
      babies.stream()
          .forEach(
              baby -> {
                if (baby.getApproved()) {
                  targetBabies.add(baby);
                }
              });
    }
    return targetBabies.stream()
        .filter(
            baby -> {
              Optional<List<LocalDate>> dates = lessonService.visitDateRange(baby, baseline);
              if (!dates.isPresent()) {
                return false;
              }
              if (visitDate != null) {
                return DateRange.includes(dates.get(), visitDate);
              }
              return true;
            })
        .map(
            baby ->
                new AppBabyDTO(
                    baby,
                    carerRepository.findOneByBabyIdAndMasterIsTrue(baby.getId()).orElse(null)))
        .collect(Collectors.toList());
  }

  public Baby createFromApp(AppCreateBabyDTO dto, Chw user) {
    Baby baby = dto.getBaby();
    List<Carer> carers = dto.getCarers();
    baby.setApproved(false);
    baby.setChw(user);
    baby.setActionFromApp(ActionFromApp.CREATE);
    baby.setProjectId(1L);
    repository.save(baby);
    carerService.saveAll(carers, baby);
    return baby;
  }

  public void updateBabyWaitingForReview(Long id, Long userId, Consumer<Baby> consumer) {
    repository
        .findByIdAndChwIdAndDeletedFalse(id, userId)
        .ifPresent(
            baby -> {
              // Updating unapproved babies does not change the original action
              if (baby.getApproved()) {
                baby.setActionFromApp(ActionFromApp.MODIFY);
              }

              Baby oldBaby = new Baby();
              BeanUtils.copyProperties(baby, oldBaby);
              baby.setApproved(false);
              consumer.accept(baby);
              saveBabyModifyRecord(id, oldBaby, baby, baby.getApproved());
              Optional<BabyUpdateInfo> babyUpdateInfoOptional =
                  babyUpdateInfoRepository.findByBabyIdAndDeleted(baby.getId(), false);
              BabyUpdateInfo babyUpdateInfo;
              if (babyUpdateInfoOptional.isPresent()) {
                babyUpdateInfo = babyUpdateInfoOptional.get();
                babyUpdateInfo.setUpdateNormal(false);
              } else {
                babyUpdateInfo = new BabyUpdateInfo();
                babyUpdateInfo.setBabyId(baby.getId());
                babyUpdateInfo.setUpdateNormal(false);
              }
              babyUpdateInfoRepository.save(babyUpdateInfo);
              repository.save(baby);
              deleteNotStartVisit(baby.getId());
            });
  }

  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void updateBabyWithoutReview(Long id, Long userId, Consumer<Baby> consumer) {
    repository
        .findByIdAndChwIdAndDeletedFalse(id, userId)
        .ifPresent(
            baby -> {
              // Updating unapproved babies does not change the original action
              if (baby.getApproved()) {
                baby.setActionFromApp(ActionFromApp.MODIFY);
              }

              Baby oldBaby = new Baby();
              BeanUtils.copyProperties(baby, oldBaby);
              consumer.accept(baby);
              baby.setApproved(false);
              saveBabyModifyRecord(id, oldBaby, baby);
              Optional<BabyUpdateInfo> babyUpdateInfoOptional =
                  babyUpdateInfoRepository.findByBabyIdAndDeleted(baby.getId(), false);
              if (!babyUpdateInfoOptional.isPresent()) {
                BabyUpdateInfo babyUpdateInfo = new BabyUpdateInfo();
                babyUpdateInfo.setBabyId(baby.getId());
                babyUpdateInfo.setUpdateNormal(true);
                babyUpdateInfoRepository.save(babyUpdateInfo);
              }
              repository.save(baby);
            });
  }

  private void deleteNotStartVisit(Long id) {
    List<AdminBabyVisitDTO> visitDTOS = visitRepository.findByBabyId(id);
    List<AdminBabyVisitDTO> notStartVisit =
        visitDTOS.stream()
            .filter(visit -> visit.getStatus().equals(VisitStatus.NOT_STARTED))
            .collect(Collectors.toList());
    for (AdminBabyVisitDTO visitDto : notStartVisit) {
      visitRepository
          .findById(visitDto.getId())
          .ifPresent(
              visit -> {
                visit.setDeleteReason("change baby info");
                visitRepository.save(visit);
                visitRepository.deleteById(visit.getId());
              });
    }
  }

  public void saveBabyCarerWithoutReview(Long id, Long userId, Carer carer) {
    updateBabyWithoutReview(
        id,
        userId,
        baby -> {
          carer.setBaby(baby);
          carerService.save(carer);
        });
  }

  public void deleteBabyCarerWithoutReview(Long id, Long userId, Long carerId) {
    updateBabyWithoutReview(
        id,
        userId,
        baby -> {
          carerRepository.deleteById(carerId);
        });
  }

  public void turnOverChwAllBabiesToOtherChw(Long id, Long takeOverUserId) {
    repository
        .findByChwIdAndDeletedFalse(id)
        .forEach(
            baby -> {
              visitRepository.deleteByBabyIdAndStatus(baby.getId(), VisitStatus.NOT_STARTED);
              if (takeOverUserId == null) {
                baby.setChw(null);
              } else {
                baby.setChw(Chw.builder().id(takeOverUserId).build());
              }
              repository.save(baby);
            });
  }

  public void closeAccount(Baby baby) {
    visitRepository.deleteByBabyIdAndStatus(baby.getId(), VisitStatus.NOT_STARTED);
    baby.setDeleted(true);
    accountOperationRecordService.saveBabyAccountOperation(baby.getId(), true);
    repository.save(baby);
  }

  public void closeAccountWaitingForReview(Long id, Long userId, String closeAccountReason) {
    updateBabyWaitingForReview(
        id,
        userId,
        baby -> {
          baby.setCloseAccountReason(closeAccountReason);
          baby.setActionFromApp(ActionFromApp.DELETE);
        });
  }

  public void approve(Baby baby) {
    babyUpdateInfoRepository
        .findByBabyIdAndDeleted(baby.getId(), false)
        .ifPresent(
            babyUpdateInfo -> {
              babyUpdateInfoRepository.delete(babyUpdateInfo);
            });
    baby.setApproved(true);
    if (baby.approveDelete()) {
      closeAccount(baby);
    } else {
      List<BabyModifyRecord> approvedBabyModifyRecords =
          babyModifyRecordRepository.findByBabyIdAndApprovedFalseOrderByLastModifiedAtDesc(
              baby.getId());
      for (BabyModifyRecord approvedBabyModifyRecord : approvedBabyModifyRecords) {
        approvedBabyModifyRecord.setApproved(true);
      }
      repository.save(baby);
    }
  }

  public void reject(Baby baby) {

    BabyHistory lastApprovedBaby =
        historyRepository.findFirstByHistoryIdAndApprovedTrueOrderByLastModifiedAtDesc(
            baby.getId());
    if (!Objects.isNull(lastApprovedBaby)) {
      BeanUtils.copyProperties(lastApprovedBaby, baby);
      baby.setApproved(true);
      baby.setId(lastApprovedBaby.getHistoryId());
    } else {
      BabyHistory originalBaby = historyRepository.findOriginalBabyHistory(baby.getId());
      BeanUtils.copyProperties(originalBaby, baby);
      baby.setApproved(true);
      baby.setId(originalBaby.getHistoryId());
    }
    babyUpdateInfoRepository
        .findByBabyIdAndDeleted(baby.getId(), false)
        .ifPresent(
            babyUpdateInfo -> {
              babyUpdateInfoRepository.delete(babyUpdateInfo);
            });
    repository.save(baby);
  }

  private void saveBabyModifyRecord(Long id, Baby oldBaby, Baby baby, Boolean approved) {
    if (repository.findById(id).isPresent()) {
      try {
        Set<String> differentColumns;
        differentColumns = FieldValueUtil.getDifferentValueField(baby, oldBaby, true, true);
        differentColumns.remove("chw");
        differentColumns.remove("curriculum");
        if (!(differentColumns.isEmpty())) {
          String differentColumnsStr = String.join(",", differentColumns);
          babyModifyRecordRepository.save(
              new BabyModifyRecord(
                  null,
                  SecurityUtils.getUserId(),
                  baby.getId(),
                  oldBaby,
                  baby,
                  differentColumnsStr,
                  approved));
        }
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
  }

  private void saveBabyModifyRecord(Long id, Baby oldBaby, Baby baby) {
    if (repository.findById(id).isPresent()) {
      try {
        Set<String> differentColumns;
        differentColumns = FieldValueUtil.getDifferentValueField(baby, oldBaby, true, true);
        differentColumns.remove("chw");
        differentColumns.remove("curriculum");
        if (!(differentColumns.isEmpty())) {
          String differentColumnsStr = String.join(",", differentColumns);
          babyModifyRecordRepository.save(
              new BabyModifyRecord(
                  null,
                  SecurityUtils.getUserId(),
                  baby.getId(),
                  oldBaby,
                  baby,
                  differentColumnsStr,
                  true));
        }
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
  }

  public void changeBabyChw(Baby baby, Long userId) {
    visitRepository.deleteByBabyIdAndStatus(baby.getId(), VisitStatus.NOT_STARTED);
    baby.setChw(Chw.builder().id(userId).build());
    repository.save(baby);
  }

  public void revertAccount(Baby baby) {
    baby.setDeleted(false);
    baby.setCloseAccountReason(null);
    accountOperationRecordService.saveBabyAccountOperation(baby.getId(), false);
    repository.save(baby);
  }

  public void resetLocation(Long id) {
    repository
        .findById(id)
        .ifPresent(
            (baby -> {
              List<VisitPositionRecord> records = visitPositionRecordRepository.findByBabyId(id);
              if (!CollectionUtils.isEmpty(records)) {
                VisitPositionRecord visitPositionRecord = records.get(0);
                baby.setShowLocation(true);
                baby.setLongitude(visitPositionRecord.getLongitude());
                baby.setLatitude(visitPositionRecord.getLatitude());
                repository.save(baby);
              }
            }));
  }
}
