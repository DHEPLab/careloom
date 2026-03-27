package edu.stanford.fsi.reap.web.rest.admin;

import edu.stanford.fsi.reap.dto.AssignBabyDTO;
import edu.stanford.fsi.reap.dto.CurriculumDTO;
import edu.stanford.fsi.reap.dto.CurriculumResultDTO;
import edu.stanford.fsi.reap.entity.Baby;
import edu.stanford.fsi.reap.entity.Curriculum;
import edu.stanford.fsi.reap.repository.BabyRepository;
import edu.stanford.fsi.reap.repository.CurriculumRepository;
import edu.stanford.fsi.reap.security.SecurityUtils;
import edu.stanford.fsi.reap.service.BabyService;
import edu.stanford.fsi.reap.service.CurriculumService;
import edu.stanford.fsi.reap.utils.ZonedDateTimeUtil;
import java.time.ZoneId;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/curriculums")
public class CurriculumResource {

  private final CurriculumService service;
  private final CurriculumRepository repository;
  private final BabyRepository babyRepository;
  private final BabyService babyService;

  public CurriculumResource(
      CurriculumService service,
      CurriculumRepository repository,
      BabyRepository babyRepository,
      BabyService babyService) {
    this.service = service;
    this.repository = repository;
    this.babyRepository = babyRepository;
    this.babyService = babyService;
  }

  @PostMapping
  public ResponseEntity<CurriculumResultDTO> publishCurriculum(
      @Valid @RequestBody CurriculumDTO curriculumDTO) {
    return service
        .publish(curriculumDTO)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping("/draft")
  public ResponseEntity<CurriculumResultDTO> saveCurriculumDraft(
      @Valid @RequestBody CurriculumDTO curriculumDTO) {
    return service
        .draft(curriculumDTO)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  public Page<Curriculum> getCurriculums(String search, Pageable pageable) {
    return repository.findBySearch(search, SecurityUtils.getProjectId(), pageable);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CurriculumResultDTO> getCurriculum(@PathVariable Long id) {
    return service
        .findById(id)
        .map(
            curriculum -> {
              ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
              if (curriculum.publishedMasterBranch()) {
                repository
                    .findFirstBySourceId(curriculum.getId())
                    .ifPresent(
                        draft -> {
                          builder.header("x-draft-id", String.valueOf(draft.getId()));
                          builder.header(
                              "x-draft-date",
                              ZonedDateTimeUtil.toResponseString(
                                  draft.getLastModifiedAt().atZone(ZoneId.systemDefault())));
                        });
              }
              return builder.body(curriculum);
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public void deleteCurriculum(@PathVariable Long id) {
    repository.findById(id).ifPresent(service::delete);
  }

  @GetMapping("/{id}/babies")
  public Page<AssignBabyDTO> getBabiesByCurriculumId(@PathVariable Long id, Pageable pageable) {
    return babyRepository.findByCurriculumId(id, SecurityUtils.getProjectId(), pageable);
  }

  @GetMapping("/{id}/not_assigned_babies")
  public Page<Baby> getCurriculumNotAssignedBabies(
      @PathVariable Long id, String search, Pageable pageable) {
    Page<Baby> babies =
        babyRepository.findByCurriculumIdIsNotAndSearchAndOrderByIdDesc(
            id, search, SecurityUtils.getProjectId(), pageable);
    babies.getContent().forEach(mapper -> mapper.setChw(null));
    return babies;
  }

  @PostMapping("/{id}/babies")
  public void assignCurriculumToBabies(@PathVariable Long id, @RequestBody Long[] babyIds) {
    repository
        .findById(id)
        .ifPresent(
            curriculum -> {
              babyService.assignCurriculum(curriculum, babyIds);
            });
  }
}
