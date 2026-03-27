package edu.stanford.fsi.reap.handler.filter;

import edu.stanford.fsi.reap.entity.Baby;
import edu.stanford.fsi.reap.entity.BabyHistory;
import edu.stanford.fsi.reap.repository.BabyHistoryRepository;
import edu.stanford.fsi.reap.repository.BabyRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BabyHistoryHandlerFilter implements IHistoryHandlerFilter {

  @Autowired private BabyRepository babyRepository;

  @Autowired private BabyHistoryRepository babyHistoryRepository;

  @Autowired private ModuleHistoryHandlerFilter moduleFilter;

  @Autowired private ModelMapper modelMapper;

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void recordUpdateHistory(Class repository, Object target) {
    if (checkRepository(repository)) {
      Baby baby = (Baby) target;
      saveHistoryRecord(baby.getId());
    } else {
      moduleFilter.recordUpdateHistory(repository, target);
    }
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void recordDelHistory(Class repository, Long id) {
    if (checkRepository(repository)) {
      saveHistoryRecord(id);
    } else {
      moduleFilter.recordDelHistory(repository, id);
    }
  }

  private boolean checkRepository(Class repository) {
    return repository.equals(BabyRepository.class) ? true : false;
  }

  private void saveHistoryRecord(Long id) {
    babyRepository
        .findById(id)
        .ifPresent(
            curBaby -> {
              BabyHistory babyHistory = modelMapper.map(curBaby, BabyHistory.class);
              BeanUtils.copyProperties(curBaby, babyHistory);
              babyHistory.setHistoryId(curBaby.getId());
              babyHistory.setId(null);
              babyHistoryRepository.save(babyHistory);
            });
  }
}
