package edu.stanford.fsi.reap.service;

import edu.stanford.fsi.reap.entity.Lesson;
import edu.stanford.fsi.reap.entity.Questionnaire;
import edu.stanford.fsi.reap.entity.enumerations.QuestionnaireBranch;
import edu.stanford.fsi.reap.repository.LessonRepository;
import edu.stanford.fsi.reap.repository.QuestionnaireRepository;
import edu.stanford.fsi.reap.security.SecurityUtils;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class QuestionnaireService {

  private final QuestionnaireRepository questionnaireRepository;

  private final LessonRepository lessonRepository;

  public QuestionnaireService(
      QuestionnaireRepository questionnaireRepository, LessonRepository lessonRepository) {
    this.questionnaireRepository = questionnaireRepository;
    this.lessonRepository = lessonRepository;
  }

  /** 保存并发布 》》》 包含 新建 和 修改（ >>> 1:从草稿到发布；2；；3：从发布到发布，单纯修改问题 ） */
  public void publish(Questionnaire questionnaire) {
    if (questionnaire.getId() != null) {
      questionnaireRepository
          .findById(questionnaire.getId())
          .ifPresent(
              qt -> {
                if (QuestionnaireBranch.DRAFT.equals(qt.getBranch()) && qt.getSource() != null) {
                  questionnaireRepository
                      .findById(qt.getSource().getId())
                      .ifPresent(
                          sourceQt -> {
                            sourceQt.setName(questionnaire.getName());
                            sourceQt.setQuestions(questionnaire.getQuestions());
                            if (sourceQt.getProjectId() == null) {
                              sourceQt.setProjectId(SecurityUtils.getProjectId());
                            }
                            questionnaireRepository.save(sourceQt);
                            updateLessonByQuestionnaireId(sourceQt.getId());
                          });
                  questionnaireRepository.deleteById(qt.getId());
                } else {
                  qt.setName(questionnaire.getName());
                  qt.setQuestions(questionnaire.getQuestions());
                  qt.setBranch(QuestionnaireBranch.MASTER);
                  qt.setPublished(true);
                  if (qt.getProjectId() == null) {
                    qt.setProjectId(SecurityUtils.getProjectId());
                  }
                  questionnaireRepository.save(qt);
                  updateLessonByQuestionnaireId(qt.getId());
                }
              });
    } else {
      questionnaire.setBranch(QuestionnaireBranch.MASTER);
      questionnaire.setPublished(true);
      if (questionnaire.getProjectId() == null) {
        questionnaire.setProjectId(SecurityUtils.getProjectId());
      }
      questionnaireRepository.save(questionnaire);
      updateLessonByQuestionnaireId(questionnaire.getId());
    }
  }

  /** 每次发布问卷，关联Lesson的questionnaire_id也要更新 */
  private void updateLessonByQuestionnaireId(Long id) {
    List<Lesson> allLessonByQt = lessonRepository.findByQuestionnaireId(id);
    if (allLessonByQt != null && allLessonByQt.size() > 0) {
      allLessonByQt.forEach(
          lesson -> {
            lesson.setLastModifiedAt(LocalDateTime.now());
            if (lesson.getProjectId() == null) {
              lesson.setProjectId(SecurityUtils.getProjectId());
            }
          });
      lessonRepository.saveAll(allLessonByQt);
    }
  }

  /** 保存为草稿》》》 包含 新建 和 修改 （ >>> 判断 1: 从草稿到草稿，单纯修改内容；；；2：从发布到草稿，新建数据 ） */
  public void draft(Questionnaire questionnaire) {
    if (questionnaire.getId() != null) {
      // 修改
      questionnaireRepository
          .findById(questionnaire.getId())
          .ifPresent(
              qt -> {
                if (QuestionnaireBranch.MASTER.equals(qt.getBranch())) {
                  // 从发布到草稿
                  Questionnaire draftQt =
                      questionnaireRepository.findBySourceId(qt.getId()).orElse(null);
                  if (draftQt == null) {
                    questionnaireRepository.save(
                        Questionnaire.builder()
                            .branch(QuestionnaireBranch.DRAFT)
                            .name(questionnaire.getName())
                            .questions(questionnaire.getQuestions())
                            .published(false)
                            .source(qt)
                            .build());
                  } else {
                    // 已经 存在 草稿 了
                    draftQt.setQuestions(questionnaire.getQuestions());
                    draftQt.setName(questionnaire.getName());
                    draftQt.setBranch(QuestionnaireBranch.DRAFT);
                    draftQt.setPublished(false);
                    questionnaireRepository.save(draftQt);
                  }
                } else {
                  qt.setQuestions(questionnaire.getQuestions());
                  qt.setName(questionnaire.getName());
                  qt.setBranch(QuestionnaireBranch.DRAFT);
                  qt.setPublished(false);
                  questionnaireRepository.save(qt);
                }
              });
    } else {
      // 新增
      questionnaire.setBranch(QuestionnaireBranch.DRAFT);
      questionnaire.setPublished(false);
      questionnaireRepository.save(questionnaire);
    }
  }
}
