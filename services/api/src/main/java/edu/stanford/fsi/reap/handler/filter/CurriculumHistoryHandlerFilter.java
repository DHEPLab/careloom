package edu.stanford.fsi.reap.handler.filter;

import edu.stanford.fsi.reap.entity.Curriculum;
import edu.stanford.fsi.reap.entity.CurriculumHistory;
import edu.stanford.fsi.reap.repository.CurriculumHistoryRepository;
import edu.stanford.fsi.reap.repository.CurriculumRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CurriculumHistoryHandlerFilter implements IHistoryHandlerFilter {

  @Autowired private CurriculumRepository curriculumRepository;

  @Autowired private CurriculumHistoryRepository curriculumHistoryRepository;

  @Autowired private CarerHistoryHandlerFilter carerHistoryHandlerFilter;

  @Autowired private ModelMapper modelMapper;

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void recordUpdateHistory(Class repository, Object target) {
    if (checkRepository(repository)) {
      Curriculum curriculum = (Curriculum) target;
      saveHistoryRecord(curriculum.getId());
    } else {
      carerHistoryHandlerFilter.recordUpdateHistory(repository, target);
    }
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void recordDelHistory(Class repository, Long id) {
    if (checkRepository(repository)) {
      saveHistoryRecord(id);
    } else {
      carerHistoryHandlerFilter.recordDelHistory(repository, id);
    }
  }

  private boolean checkRepository(Class repository) {
    return repository.equals(CurriculumRepository.class) ? true : false;
  }

  private void saveHistoryRecord(Long id) {
    curriculumRepository
        .findById(id)
        .ifPresent(
            curCurriculum -> {
              CurriculumHistory curriculumHistory =
                  modelMapper.map(curCurriculum, CurriculumHistory.class);
              curriculumHistory.setHistoryId(curriculumHistory.getId());
              curriculumHistory.setId(null);
              curriculumHistoryRepository.save(curriculumHistory);
            });
  }
}
