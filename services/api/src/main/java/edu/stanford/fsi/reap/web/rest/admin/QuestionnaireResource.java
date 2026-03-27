package edu.stanford.fsi.reap.web.rest.admin;

import static edu.stanford.fsi.reap.entity.enumerations.QuestionnaireBranch.MASTER;

import edu.stanford.fsi.reap.dto.QuestionnaireRequestDTO;
import edu.stanford.fsi.reap.dto.QuestionnaireResultDTO;
import edu.stanford.fsi.reap.entity.Lesson;
import edu.stanford.fsi.reap.entity.Questionnaire;
import edu.stanford.fsi.reap.repository.LessonRepository;
import edu.stanford.fsi.reap.repository.QuestionnaireRepository;
import edu.stanford.fsi.reap.security.SecurityUtils;
import edu.stanford.fsi.reap.service.QuestionnaireService;
import edu.stanford.fsi.reap.utils.ZonedDateTimeUtil;
import edu.stanford.fsi.reap.web.rest.errors.BadRequestAlertException;
import java.time.ZoneId;
import java.util.List;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/questionnaires")
@Slf4j
public class QuestionnaireResource {

  private final QuestionnaireRepository questionnaireRepository;

  private final QuestionnaireService questionnaireService;

  private final ModelMapper modelMapper;

  private final LessonRepository lessonRepository;

  public QuestionnaireResource(
      QuestionnaireRepository questionnaireRepository,
      QuestionnaireService questionnaireService,
      ModelMapper modelMapper,
      LessonRepository lessonRepository) {
    this.questionnaireRepository = questionnaireRepository;
    this.questionnaireService = questionnaireService;
    this.modelMapper = modelMapper;
    this.lessonRepository = lessonRepository;
  }

  /** 保存并发布 》》》 包含 新建 和 修改（ >>> 1:从草稿到发布；；；2：从发布到发布，单纯修改问题 ） */
  @PostMapping
  public void publishQuestionnaire(
      @Valid @RequestBody QuestionnaireRequestDTO questionnaireRequestDTO) {
    Questionnaire questionnaire = modelMapper.map(questionnaireRequestDTO, Questionnaire.class);
    questionnaireService.publish(questionnaire);
  }

  /** 保存为草稿》》》 包含 新建 和 修改 （ >>> 判断 1: 从草稿到草稿，单纯修改内容；；；2：从发布到草稿，新建数据 ） */
  @PostMapping("/draft")
  public void saveQuestionnaireDraft(
      @Valid @RequestBody QuestionnaireRequestDTO questionnaireRequestDTO) {
    Questionnaire questionnaire = modelMapper.map(questionnaireRequestDTO, Questionnaire.class);
    if (questionnaire.getProjectId() == null) {
      questionnaire.setProjectId(SecurityUtils.getProjectId());
    }
    questionnaireService.draft(questionnaire);
  }

  @GetMapping
  public Page<QuestionnaireResultDTO> getQuestionnaires(
      @RequestParam(defaultValue = "") String name, Pageable pageable) {
    return questionnaireRepository.findBySearch(name, SecurityUtils.getProjectId(), pageable);
  }

  @GetMapping("/{id}")
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

  @DeleteMapping("/{id}")
  public void deleteQuestionnaire(@PathVariable Long id) {
    questionnaireRepository
        .findById(id)
        .ifPresent(
            qt -> {
              if (MASTER.equals(qt.getBranch()) && qt.isPublished()) {
                List<Lesson> countLesson = lessonRepository.findByQuestionnaireId(id);
                log.info(
                    "Delete master branch questionnaire, id {} , Number of USES {}",
                    id,
                    countLesson.size());
                if (countLesson.size() > 0) {
                  throw new BadRequestAlertException(("error.delete.survey.inUse"));
                }
              }
              questionnaireRepository.deleteById(id);
            });
  }
}
