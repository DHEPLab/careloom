package edu.stanford.fsi.reap.web.rest;

import com.fasterxml.jackson.databind.JsonNode;
import edu.stanford.fsi.reap.dto.*;
import edu.stanford.fsi.reap.entity.Baby;
import edu.stanford.fsi.reap.entity.Carer;
import edu.stanford.fsi.reap.entity.CarerModifyRecord;
import edu.stanford.fsi.reap.entity.Chw;
import edu.stanford.fsi.reap.repository.*;
import edu.stanford.fsi.reap.security.SecurityUtils;
import edu.stanford.fsi.reap.service.BabyService;
import edu.stanford.fsi.reap.service.GoogleMapService;
import edu.stanford.fsi.reap.service.LessonService;
import edu.stanford.fsi.reap.utils.BabyAge;
import edu.stanford.fsi.reap.utils.DateRange;
import edu.stanford.fsi.reap.utils.FieldValueUtil;
import edu.stanford.fsi.reap.web.rest.errors.BadRequestAlertException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author hookszhang
 */
@Slf4j
@RestController
@RequestMapping("/api/babies")
public class AppBabyResource {

  private final LessonService lessonService;
  private final BabyRepository repository;
  private final BabyService service;
  private final CarerRepository carerRepository;
  private final VisitRepository visitRepository;
  private final ModelMapper modelMapper;
  private final CarerModifyRecordRepository carerModifyRecordRepository;
  private final BabyUpdateInfoRepository babyUpdateInfoRepository;
  private final GoogleMapService googleMapService;

  public AppBabyResource(
      LessonService lessonService,
      BabyRepository repository,
      BabyService service,
      CarerRepository carerRepository,
      VisitRepository visitRepository,
      ModelMapper modelMapper,
      CarerModifyRecordRepository carerModifyRecordRepository,
      BabyUpdateInfoRepository babyUpdateInfoRepository,
      GoogleMapService googleMapService) {
    this.lessonService = lessonService;
    this.repository = repository;
    this.service = service;
    this.carerRepository = carerRepository;
    this.visitRepository = visitRepository;
    this.modelMapper = modelMapper;
    this.carerModifyRecordRepository = carerModifyRecordRepository;
    this.babyUpdateInfoRepository = babyUpdateInfoRepository;
    this.googleMapService = googleMapService;
  }

  @GetMapping
  public Page<AppBabyDTO> getAppBabies(String name, Pageable pageable) {
    List<Sort.Order> orders = pageable.getSort().get().collect(Collectors.toList());
    if (orders.size() == 0) {
      Sort.Order createdAt = new Sort.Order(Sort.Direction.DESC, "createdAt");
      pageable =
          PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(createdAt));
    }
    Long chwId = SecurityUtils.getUserId();

    Page<AppBabyDTO> ret =
        repository
            .findAppBabyByChwIdAndName(chwId, name, pageable)
            .map(
                appBabyDTO -> {
                  appBabyDTO.setCarerName(repository.getNameByBabyId(appBabyDTO.getId()));
                  appBabyDTO.setCarerPhone(repository.getPhoneByBabyId(appBabyDTO.getId()));
                  appBabyDTO.setCurriculum(
                      repository.findById(appBabyDTO.getId()).get().getCurriculum());
                  return appBabyDTO;
                });

    ret.forEach(
        appBabyDTO -> {
          appBabyDTO.setAllCarerList(
              carerRepository.findByBabyIdOrderByMasterDesc(appBabyDTO.getId()));
          Baby tempBaby =
              Baby.builder()
                  .id(appBabyDTO.getId())
                  .stage(appBabyDTO.getStage())
                  .edc(appBabyDTO.getEdc())
                  .curriculum(appBabyDTO.getCurriculum())
                  .birthday(appBabyDTO.getBirthday())
                  .build();
          if (tempBaby.getCurriculum() != null) {
            appBabyDTO.setNextShouldVisitDTO(
                NextShouldVisitDTO.builder()
                    .lesson(lessonService.match(tempBaby, LocalDate.now()).orElse(null))
                    .visitDateRange(
                        lessonService.visitDateRange(tempBaby, LocalDate.now()).orElse(null))
                    .build());
          }
          appBabyDTO.checkNextShouldVisitDTO();
        });
    return ret;
  }

  @GetMapping("/{id}/visit-date-range")
  public ResponseEntity<List<LocalDate>> getBabyVisitDateRange(@PathVariable Long id) {
    return repository
        .findByIdAndChwIdAndDeletedFalse(id, SecurityUtils.getUserId())
        .flatMap(
            baby -> {
              if (baby.noCurriculum()) {
                return Optional.empty();
              }
              return lessonService.visitDateRange(baby, LocalDate.now());
            })
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/available-for-visit")
  public List<AppBabyDTO> getBabiesAvailableForCreateVisit(
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate) {
    return service.findBabiesAvailableForCreateVisit(
        SecurityUtils.getUserId(),
        visitDate,
        DateRange.checkBaseline(LocalDate.now(), LocalDateTime.now()));
  }

  @GetMapping("/{id}/visits")
  public BabyVisits getAppBabyVisits(@PathVariable Long id) {
    return new BabyVisits(
        visitRepository.findByBabyIdAndStarted(id),
        visitRepository.findByBabyIdAndNotStarted(id),
        visitRepository.findCountByBabyIdAndStatusAndRemarkIsNull(id));
  }

  @GetMapping("/{id}/lesson")
  public ResponseEntity<AppLessonDTO> getAvailableLesson(@PathVariable Long id) {
    Optional<Baby> optional =
        repository.findByIdAndChwIdAndDeletedFalse(id, SecurityUtils.getUserId());
    if (!optional.isPresent()) {
      return ResponseEntity.notFound().build();
    }

    Baby baby = optional.get();
    if (baby.noCurriculum()) {
      throw new BadRequestAlertException("error.baby.noCurriculum");
    }

    List<VisitResultDTO> notStarted = visitRepository.findByBabyIdAndNotStarted(id);
    if (notStarted.size() > 0) {
      throw new BadRequestAlertException("error.baby.pendingVisit");
    }

    return lessonService
        .findAvailable(baby, DateRange.checkBaseline(LocalDate.now(), LocalDateTime.now()))
        .map(ResponseEntity::ok)
        .orElseThrow(() -> new BadRequestAlertException("error.baby.noMatchingLesson"));
  }

  @PostMapping
  public AppBabyDTO createBabyFromApp(@Valid @RequestBody AppCreateBabyDTO dto) {
    return new AppBabyDTO(
        service.createFromApp(dto, Chw.builder().id(SecurityUtils.getUserId()).build()), null);
  }

  @PostMapping("/{id}/carers")
  public void createBabyCarer(@PathVariable Long id, @Valid @RequestBody AppCarerDTO dto) {
    Carer carer = modelMapper.map(dto, Carer.class);
    service.saveBabyCarerWithoutReview(id, SecurityUtils.getUserId(), carer);
  }

  @PutMapping("/{id}/carers/{carerId}")
  public void updateBabyCarer(
      @PathVariable("id") Long id,
      @PathVariable("carerId") Long carerId,
      @Valid @RequestBody AppCarerDTO dto) {
    Carer carer = modelMapper.map(dto, Carer.class);
    carer.setId(carerId);
    Optional<Carer> master = carerRepository.findOneByBabyIdAndMasterIsTrue(id);
    // when changing the current master caregiver to no
    if (master.isPresent() && carerId.equals(master.get().getId()) && !carer.isMaster()) {
      throw new BadRequestAlertException("error.baby.atLeastOneMaster");
    }
    saveCarerModifyRecord(carer);
    service.saveBabyCarerWithoutReview(id, SecurityUtils.getUserId(), carer);
  }

  private void saveCarerModifyRecord(Carer carer) {
    if (carerRepository.findById(carer.getId()).isPresent()) {
      Carer oldCarer = carerRepository.findById(carer.getId()).get();
      Set<String> differentColumns;
      try {
        differentColumns = FieldValueUtil.getDifferentValueField(carer, oldCarer, true, true);
        differentColumns.remove("baby");
        if (!(differentColumns.isEmpty())) {
          String differentColumnsStr = String.join(",", differentColumns);
          carerModifyRecordRepository.save(
              new CarerModifyRecord(
                  null,
                  SecurityUtils.getUserId(),
                  carer.getId(),
                  oldCarer,
                  carer,
                  differentColumnsStr));
        }
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
  }

  @DeleteMapping("/{id}/carers/{carerId}")
  public void deleteBabyCarer(@PathVariable("id") Long id, @PathVariable("carerId") Long carerId) {
    service.deleteBabyCarerWithoutReview(id, SecurityUtils.getUserId(), carerId);
  }

  @PutMapping("/{id}/close")
  public void closeBabyAccountFromApp(
      @PathVariable Long id, @Valid @RequestBody CloseAccountReasonWrapper wrapper) {
    service.closeAccountWaitingForReview(id, SecurityUtils.getUserId(), wrapper.getReason());
  }

  @PutMapping("/{id}")
  public void updateBabyBasicInfo(@PathVariable Long id, @Valid @RequestBody BabyWrapper wrapper) {
    Optional<Baby> babyOptional = repository.findById(id);
    if (babyOptional.isPresent()) {
      if (babyOptional.get().getStage().equals(wrapper.getStage())) {
        service.updateBabyWithoutReview(
            id,
            SecurityUtils.getUserId(),
            baby -> {
              baby.setName(wrapper.getName());
              baby.setGender(wrapper.getGender());
              baby.setStage(wrapper.getStage());
              baby.setEdc(wrapper.getEdc());
              baby.setBirthday(wrapper.getBirthday());
              baby.setFeedingPattern(wrapper.getFeedingPattern());
              baby.setAssistedFood(wrapper.getAssistedFood());
            });
      } else {
        service.updateBabyWaitingForReview(
            id,
            SecurityUtils.getUserId(),
            baby -> {
              baby.setName(wrapper.getName());
              baby.setGender(wrapper.getGender());
              baby.setStage(wrapper.getStage());
              baby.setEdc(wrapper.getEdc());
              baby.setBirthday(wrapper.getBirthday());
              baby.setFeedingPattern(wrapper.getFeedingPattern());
              baby.setAssistedFood(wrapper.getAssistedFood());
            });
      }
    }
  }

  @PutMapping("/{id}/remark")
  public void updateBabyRemark(@PathVariable Long id, @Valid @RequestBody RemarkWrapper wrapper) {

    service.updateBabyWithoutReview(
        id,
        SecurityUtils.getUserId(),
        baby -> {
          baby.setRemark(wrapper.getRemark());
        });
  }

  @PutMapping("/{id}/address")
  public void updateBabyAddress(@PathVariable Long id, @Valid @RequestBody AddressWrapper wrapper) {
    service.updateBabyWaitingForReview(
        id,
        SecurityUtils.getUserId(),
        baby -> {
          baby.setArea(wrapper.getArea());
          baby.setLocation(wrapper.getLocation());
          baby.setLatitude(wrapper.getLatitude());
          baby.setLongitude(wrapper.getLongitude());
        });
  }

  @GetMapping("/{id}")
  public ResponseEntity<BabyDetailDTO> getAppBaby(@PathVariable Long id) {
    Long chwId = SecurityUtils.getUserId();
    return repository
        .findByIdAndChwIdAndDeletedFalse(id, chwId)
        .map(
            baby -> {
              BabyDetailDTO dto = modelMapper.map(baby, BabyDetailDTO.class);
              if (baby.getApproved()) {
                dto.setCanCreate(true);
              } else {
                babyUpdateInfoRepository
                    .findByBabyIdAndDeleted(id, false)
                    .ifPresent(
                        babyUpdateInfo -> {
                          if (babyUpdateInfo.getUpdateNormal()) {
                            dto.setCanCreate(true);
                          }
                        });
              }
              dto.setMonths(
                  BabyAge.months(
                      baby, DateRange.checkBaseline(LocalDate.now(), LocalDateTime.now())));
              dto.setChw(null);
              dto.setDays(BabyAge.days(baby, LocalDate.now()));
              return dto;
            })
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/{id}/carers")
  public List<Carer> getAppBabyCarers(@PathVariable Long id) {
    Long chwId = SecurityUtils.getUserId();
    return carerRepository.findByBabyIdAndBabyChwIdOrderByMasterDesc(id, chwId);
  }

  @GetMapping("/place/autocomplete")
  public ResponseEntity<JsonNode> getPlaceAutocomplete(@RequestParam String area) {
    JsonNode result = googleMapService.getGlaceAutocomplete(area);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/place/location")
  public ResponseEntity<GeoLocation> findPlaceLocation(
      @RequestParam(required = false) String placeId, @RequestParam(required = false) String area) {
    GeoLocation result =
        Optional.ofNullable(placeId)
            .map(googleMapService::getPlaceGeoLocationByPlaceId)
            .orElseGet(() -> googleMapService.getPlaceGeoLocation(area));

    return ResponseEntity.ok(result);
  }
}
