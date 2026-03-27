package edu.stanford.fsi.reap.web.rest.admin;

import cn.hutool.core.collection.CollUtil;
import edu.stanford.fsi.reap.dto.CarerDTO;
import edu.stanford.fsi.reap.dto.CarerModifyRecordDTO;
import edu.stanford.fsi.reap.entity.Carer;
import edu.stanford.fsi.reap.entity.CarerModifyRecord;
import edu.stanford.fsi.reap.repository.CarerModifyRecordRepository;
import edu.stanford.fsi.reap.repository.CarerRepository;
import edu.stanford.fsi.reap.repository.UserRepository;
import edu.stanford.fsi.reap.security.SecurityUtils;
import edu.stanford.fsi.reap.service.CarerModifyRecordService;
import edu.stanford.fsi.reap.service.CarerService;
import edu.stanford.fsi.reap.utils.FieldValueUtil;
import edu.stanford.fsi.reap.web.rest.errors.BadRequestAlertException;
import java.util.*;
import javax.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author hookszhang
 */
@RestController
@RequestMapping("/admin")
public class CarerResource {

  private final CarerRepository repository;
  private final CarerService service;
  private final ModelMapper modelMapper;
  private final CarerModifyRecordRepository carerModifyRecordRepository;
  private final CarerModifyRecordService carerModifyRecordService;

  public CarerResource(
      CarerRepository repository,
      CarerService service,
      ModelMapper modelMapper,
      CarerModifyRecordRepository carerModifyRecordRepository,
      UserRepository userRepository,
      CarerModifyRecordService carerModifyRecordService) {
    this.repository = repository;
    this.service = service;
    this.modelMapper = modelMapper;
    this.carerModifyRecordRepository = carerModifyRecordRepository;
    this.carerModifyRecordService = carerModifyRecordService;
  }

  @GetMapping("carers/modify-records")
  public ResponseEntity<List<CarerModifyRecordDTO>> getCarerModifyRecord(
      Long babyId,
      @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "en") String lang) {
    List<Long> carerIds = repository.findCarerIdByBabyId(babyId);
    if (CollUtil.isEmpty(carerIds)) {
      return ResponseEntity.ok(Collections.EMPTY_LIST);
    }
    List<CarerModifyRecordDTO> result = new ArrayList<>();
    carerIds.stream()
        .forEach(
            (carerId) -> {
              result.addAll(carerModifyRecordService.getCarerList(carerId, lang));
            });
    return ResponseEntity.ok(result);
  }

  @PostMapping("/carers")
  public ResponseEntity<Carer> createCarer(@Valid @RequestBody CarerDTO carerDTO) {
    Carer carer = modelMapper.map(carerDTO, Carer.class);
    carer.setProjectId(SecurityUtils.getProjectId());
    return ResponseEntity.ok(service.save(carer));
  }

  @PutMapping("/carers/{id}")
  public ResponseEntity<Carer> updateCarer(
      @PathVariable Long id, @Valid @RequestBody CarerDTO carerDTO) {
    Carer carer = modelMapper.map(carerDTO, Carer.class);
    carer.setId(id);
    Optional<Carer> master = repository.findOneByBabyIdAndMasterIsTrue(carer.getBaby().getId());
    // when changing the current master caregiver to no
    if (master.isPresent() && id.equals(master.get().getId()) && !carer.isMaster()) {
      throw new BadRequestAlertException("error.carer.atLeastOneMaster");
    }
    saveCarerModifyRecord(id, carer);
    return ResponseEntity.ok(service.save(carer));
  }

  private void saveCarerModifyRecord(Long id, Carer carer) {
    if (repository.findById(id).isPresent()) {
      Carer oldCarer = repository.findById(carer.getId()).get();
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

  @DeleteMapping("/carers/{id}")
  public void deleteCarer(@PathVariable Long id) {
    repository
        .findById(id)
        .ifPresent(
            carer -> {
              if (carer.isMaster()) {
                throw new BadRequestAlertException("error.carer.cannotDeleteMaster");
              }
              repository.deleteById(id);
            });
  }
}
