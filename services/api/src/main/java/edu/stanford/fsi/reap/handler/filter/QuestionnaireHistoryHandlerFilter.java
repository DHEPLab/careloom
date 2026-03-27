package edu.stanford.fsi.reap.handler.filter;

import edu.stanford.fsi.reap.entity.Questionnaire;
import edu.stanford.fsi.reap.entity.QuestionnaireHistory;
import edu.stanford.fsi.reap.repository.QuestionnaireHistoryRepository;
import edu.stanford.fsi.reap.repository.QuestionnaireRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class QuestionnaireHistoryHandlerFilter implements IHistoryHandlerFilter {

  @Autowired private QuestionnaireRepository questionnaireRepository;

  @Autowired private QuestionnaireHistoryRepository questionnaireHistoryRepository;

  @Autowired private LessonHistoryHandlerFilter lessonFilter;

  @Autowired private ModelMapper modelMapper;

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void recordUpdateHistory(Class repository, Object target) {
    if (checkRepository(repository)) {
      Questionnaire questionnaire = (Questionnaire) target;
      saveHistoryRecord(questionnaire.getId());
    } else {
      lessonFilter.recordUpdateHistory(repository, target);
    }
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void recordDelHistory(Class repository, Long id) {
    if (checkRepository(repository)) {
      saveHistoryRecord(id);
    } else {
      lessonFilter.recordDelHistory(repository, id);
    }
  }

  private boolean checkRepository(Class repository) {
    return repository.equals(Questionnaire.class) ? true : false;
  }

  private void saveHistoryRecord(Long id) {
    questionnaireRepository
        .findById(id)
        .ifPresent(
            curQuestion -> {
              QuestionnaireHistory questionnaireHistory =
                  modelMapper.map(curQuestion, QuestionnaireHistory.class);
              questionnaireHistory.setId(null);
              questionnaireHistory.setHistoryId(curQuestion.getId());
              questionnaireHistoryRepository.save(questionnaireHistory);
            });
  }
}
