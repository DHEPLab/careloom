package edu.stanford.fsi.reap.web.rest.admin;

import com.fasterxml.jackson.databind.JsonNode;
import edu.stanford.fsi.reap.dto.*;
import edu.stanford.fsi.reap.dto.GeoLocation;
import edu.stanford.fsi.reap.entity.*;
import edu.stanford.fsi.reap.repository.*;
import edu.stanford.fsi.reap.security.SecurityUtils;
import edu.stanford.fsi.reap.service.BabyModifyRecordService;
import edu.stanford.fsi.reap.service.BabyService;
import edu.stanford.fsi.reap.service.ExcelService;
import edu.stanford.fsi.reap.service.GoogleMapService;
import edu.stanford.fsi.reap.utils.BabyAge;
import edu.stanford.fsi.reap.utils.FieldValueUtil;
import edu.stanford.fsi.reap.web.rest.errors.BadRequestAlertException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author hookszhang
 */
@RestController
@RequestMapping("/admin/babies")
public class BabyResource {

  private final CarerRepository carerRepository;
  private final BabyRepository repository;
  private final BabyService service;
  private final ModelMapper modelMapper;
  private final VisitRepository visitRepository;
  private final ExcelService excelService;
  private final BabyModifyRecordRepository babyModifyRecordRepository;
  private final BabyModifyRecordService babyModifyRecordService;
  private final GoogleMapService googleMapService;

  public BabyResource(
      CarerRepository carerRepository,
      BabyRepository repository,
      BabyService service,
      ModelMapper modelMapper,
      VisitRepository visitRepository,
      ExcelService excelService,
      BabyModifyRecordRepository babyModifyRecordRepository,
      BabyModifyRecordService babyModifyRecordService,
      GoogleMapService googleMapService) {
    this.carerRepository = carerRepository;
    this.repository = repository;
    this.service = service;
    this.modelMapper = modelMapper;
    this.visitRepository = visitRepository;
    this.excelService = excelService;
    this.babyModifyRecordRepository = babyModifyRecordRepository;
    this.babyModifyRecordService = babyModifyRecordService;
    this.googleMapService = googleMapService;
  }

  @PostMapping
  public ResponseEntity<Baby> createBaby(@Valid @RequestBody BabyDTO dto) {
    Baby baby = modelMapper.map(dto, Baby.class);
    keepIdentityUnique(baby);
    baby.setProjectId(SecurityUtils.getProjectId());
    return ResponseEntity.ok(repository.save(baby));
  }

  private void keepIdentityUnique(Baby baby) {
    repository
        .findFirstByIdentity(baby.getIdentity())
        .ifPresent(
            consumer -> {
              if (!consumer.getId().equals(baby.getId())) {
                throw new BadRequestAlertException("ID: " + baby.getIdentity() + " 已经存在");
              }
            });
  }

  @PutMapping("/{id}/chw/{userId}")
  public void changeBabyChw(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
    if (repository.findById(id).isPresent()) {
      // Baby oldBaby = repository.findById(id).get();
      repository.findById(id).ifPresent(baby -> service.changeBabyChw(baby, userId));
      // Baby newBaby = repository.findById(id).get();
      // babyModifyRecordRepository.save(new BabyModifyRecord(null, SecurityUtils.getUserId(), id,
      // oldBaby, newBaby, "chw", true));
    }
    repository.findById(id).ifPresent(baby -> service.changeBabyChw(baby, userId));
  }

  @PutMapping("/{id}")
  public ResponseEntity<Baby> updateBaby(@PathVariable Long id, @Valid @RequestBody BabyDTO dto) {
    Baby baby = modelMapper.map(dto, Baby.class);
    keepIdentityUnique(baby);

    Optional<Baby> oldBaby = repository.findById(dto.getId());
    baby.setCreatedAt(oldBaby.get().getCreatedAt());
    baby.setLastModifiedAt(LocalDateTime.now());
    baby.setLastModifiedBy(SecurityUtils.getUsername());
    baby.setCreatedBy(oldBaby.get().getCreatedBy());
    baby.setProjectId(oldBaby.get().getProjectId());

    try {
      if (repository.findById(id).isPresent()) {
        Set<String> differentColumns =
            FieldValueUtil.getDifferentValueField(baby, repository.findById(id).get(), true, true);
        if (!(differentColumns.isEmpty())) {
          saveBabyModifyRecord(id, baby);
        }
      }
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }

    return repository
        .findById(id)
        .map(consumer -> repository.save(consumer.update(baby)))
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  private void saveBabyModifyRecord(Long id, Baby baby) {

    if (repository.findById(id).isPresent()) {
      Baby oldBaby = repository.findById(id).get();
      Set<String> differentColumns;
      try {
        differentColumns = FieldValueUtil.getDifferentValueField(baby, oldBaby, true, true);
        differentColumns.remove("chw");
        differentColumns.remove("curriculum");
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
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
  }

  @PutMapping("/{id}/approve")
  public void approveBaby(@PathVariable Long id, @RequestBody IdentityWrapper wrapper) {
    repository
        .findById(id)
        .ifPresent(
            baby -> {
              if (baby.approveCreate()) {
                baby.setIdentity(wrapper.getIdentity());
                keepIdentityUnique(baby);
              }
              service.approve(baby);
            });
  }

  @PutMapping("/{id}/reject")
  public void rejectBaby(@PathVariable Long id) {
    repository
        .findById(id)
        .ifPresent(
            baby -> {
              if (!baby.getApproved()) {
                service.reject(baby);
              }
            });
  }

  @Data
  static class IdentityWrapper {
    private String identity;
  }

  @GetMapping("/{id}")
  public ResponseEntity<BabyDetailDTO> getBaby(@PathVariable Long id) {
    return repository
        .findById(id)
        .map(
            baby -> {
              BabyDetailDTO dto = modelMapper.map(baby, BabyDetailDTO.class);
              dto.setMonths(BabyAge.months(baby, LocalDate.now()));
              dto.setDays(BabyAge.days(baby, LocalDate.now()));
              return dto;
            })
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/approved")
  public Page<AdminBabyDTO> getApprovedBabies(String search, Pageable pageable) {
    List<Sort.Order> orders = pageable.getSort().get().collect(Collectors.toList());
    if (orders.size() == 0) {
      Sort.Order createdAt = new Sort.Order(Sort.Direction.DESC, "createdAt");
      pageable =
          PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(createdAt));
    }
    Long projectId = SecurityUtils.getProjectId();
    return repository
        .findBySearchAndApprovedTrueOrderBy(search, projectId, pageable)
        .map(this::mapDtoByBaby);
  }

  @GetMapping("/place/autocomplete")
  public ResponseEntity<JsonNode> getPlaceAutocomplete(@RequestParam String area) {
    JsonNode result = googleMapService.getGlaceAutocomplete(area);

    return ResponseEntity.ok(result);
  }

  @GetMapping("/place")
  public ResponseEntity<JsonNode> findPlace(@RequestParam String area) {
    JsonNode result = googleMapService.findPlace(area);

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

  @GetMapping("/unreviewed")
  public Page<AdminBabyDTO> getUnreviewedBabies(String search, Pageable pageable) {
    List<Sort.Order> orders = pageable.getSort().get().collect(Collectors.toList());
    if (orders.size() == 0) {
      Sort.Order createdAt = new Sort.Order(Sort.Direction.ASC, "lastModifiedAt");
      pageable =
          PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(createdAt));
    }
    if (SecurityUtils.hasAuthorityAdmin()) {
      return repository
          .findBySearchAndApprovedFalse(search, SecurityUtils.getProjectId(), pageable)
          .map(this::mapDtoByBaby);
    } else {
      return repository
          .findBySearchAndSupervisorIdAndApprovedFalse(
              search, SecurityUtils.getUserId(), SecurityUtils.getProjectId(), pageable)
          .map(this::mapDtoByBaby);
    }
  }

  @GetMapping("modify-records")
  public ResponseEntity<List<BabyModifyRecordDTO>> getBabyModifyRecord(
      Long babyId,
      @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "en") String lang) {
    List<BabyModifyRecordDTO> res = babyModifyRecordService.getBabyList(babyId, lang);
    return ResponseEntity.ok(res);
  }

  @PostMapping("location/import")
  public void importBabyLocation(@RequestParam(name = "records") MultipartFile records) {
    if (records.isEmpty()) {
      throw new BadRequestAlertException("上传文件不能为空！");
    }
    excelService.importBabyLocations(records);
  }

  /** 将Baby map 为AdminBabyDTO */
  private AdminBabyDTO mapDtoByBaby(Baby baby) {
    return AdminBabyDTO.builder()
        .id(baby.getId())
        .identity(baby.getIdentity())
        .name(baby.getName())
        .gender(baby.getGender())
        .area(baby.getArea())
        .chw(baby.getChw() != null ? baby.getChw().getRealName() : "")
        .visitCount(repository.getVisitCountByBabyId(baby.getId()))
        .currentLessonName(repository.getCurrentLessonNameByBabyId(baby.getId()))
        .actionFromApp(baby.getActionFromApp())
        .lastModifiedAt(baby.getLastModifiedAt())
        .createdAt(baby.getCreatedAt())
        .deleted(baby.isDeleted())
        .build();
  }

  @GetMapping("/{id}/carers")
  public List<Carer> getBabyCarers(@PathVariable Long id) {
    return carerRepository.findByBabyIdOrderByMasterDesc(id);
  }

  @GetMapping("/{id}/visits")
  public List<AdminBabyVisitDTO> getBabyVisits(@PathVariable Long id) {
    return visitRepository.findByBabyId(id);
  }

  @PutMapping("/{id}/revert")
  public void revertBabyAccount(@PathVariable Long id) {
    repository.findById(id).ifPresent(service::revertAccount);
  }

  @DeleteMapping("/{id}")
  public void closeBabyAccount(@PathVariable Long id, @RequestParam String reason) {
    repository
        .findById(id)
        .ifPresent(
            baby -> {
              baby.setCloseAccountReason(reason);
              service.closeAccount(baby);
            });
  }

  @DeleteMapping("/{id}/chw")
  public void releaseBabyChw(@PathVariable Long id) {
    service.releaseChw(id);
  }

  @DeleteMapping("/{id}/curriculum")
  public void releaseBabyCurriculum(@PathVariable Long id) {
    service.releaseCurriculum(id);
  }

  @PostMapping("/check")
  public ResponseEntity<List<Map<String, String>>> check(
      @Valid @RequestBody List<ImportBabyDto> list,
      @RequestParam(required = false, defaultValue = "zh") String lang) {
    return ResponseEntity.ok(service.check(list, lang));
  }

  @PostMapping("/imports")
  public ResponseEntity imports(@Valid @RequestBody List<ImportBabyDto> list) {
    service.imports(list);
    return ResponseEntity.ok().build();
  }

  @PostMapping("reset/location/{id}")
  public void resetLocation(@PathVariable("id") Long id) {
    service.resetLocation(id);
  }
}
