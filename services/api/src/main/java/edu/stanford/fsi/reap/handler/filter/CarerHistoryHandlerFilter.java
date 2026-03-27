package edu.stanford.fsi.reap.handler.filter;

import edu.stanford.fsi.reap.entity.Carer;
import edu.stanford.fsi.reap.entity.CarerHistory;
import edu.stanford.fsi.reap.repository.CarerHistoryRepository;
import edu.stanford.fsi.reap.repository.CarerRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CarerHistoryHandlerFilter implements IHistoryHandlerFilter {

  @Autowired private CarerRepository carerRepository;

  @Autowired private CarerHistoryRepository carerHistoryRepository;

  @Autowired private TagHistoryHandlerFilter tagHistoryHandlerFilter;

  @Autowired private ModelMapper modelMapper;

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void recordUpdateHistory(Class repository, Object target) {
    if (checkRepository(repository)) {
      Carer carer = (Carer) target;
      saveHistoryRecord(carer.getId());
    } else {
      tagHistoryHandlerFilter.recordUpdateHistory(repository, target);
    }
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void recordDelHistory(Class repository, Long id) {
    if (checkRepository(repository)) {
      saveHistoryRecord(id);
    } else {
      tagHistoryHandlerFilter.recordDelHistory(repository, id);
    }
  }

  private boolean checkRepository(Class repository) {
    return repository.equals(CarerRepository.class) ? true : false;
  }

  private void saveHistoryRecord(Long id) {
    carerRepository
        .findById(id)
        .ifPresent(
            curCarer -> {
              CarerHistory carerHistory = modelMapper.map(curCarer, CarerHistory.class);
              carerHistory.setHistoryId(curCarer.getId());
              carerHistory.setId(null);
              carerHistoryRepository.save(carerHistory);
            });
  }
}
