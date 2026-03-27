package edu.stanford.fsi.reap.handler.filter;

import edu.stanford.fsi.reap.entity.CommunityHouseWorker;
import edu.stanford.fsi.reap.entity.CommunityHouseWorkerHistory;
import edu.stanford.fsi.reap.repository.CommunityHouseWorkerHistoryRepository;
import edu.stanford.fsi.reap.repository.CommunityHouseWorkerRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ChwHistoryHandlerFilter implements IHistoryHandlerFilter {
  @Autowired private CommunityHouseWorkerHistoryRepository communityHouseWorkerHistoryRepository;

  @Autowired private CommunityHouseWorkerRepository communityHouseWorkerRepository;

  @Autowired private BabyHistoryHandlerFilter babyFilter;

  @Autowired private ModelMapper modelMapper;

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void recordUpdateHistory(Class repository, Object target) {
    if (checkRepository(repository)) {
      CommunityHouseWorker communityHouseWorker = (CommunityHouseWorker) target;
      saveHistoryRecord(communityHouseWorker.getId());
    } else {
      babyFilter.recordUpdateHistory(repository, target);
    }
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void recordDelHistory(Class repository, Long id) {
    if (checkRepository(repository)) {
      saveHistoryRecord(id);
    } else {
      babyFilter.recordDelHistory(repository, id);
    }
  }

  private boolean checkRepository(Class repository) {
    return repository.equals(CommunityHouseWorkerRepository.class) ? true : false;
  }

  private void saveHistoryRecord(Long id) {
    communityHouseWorkerRepository
        .findById(id)
        .ifPresent(
            (curChw) -> {
              CommunityHouseWorkerHistory communityHouseWorkerHistory =
                  modelMapper.map(curChw, CommunityHouseWorkerHistory.class);
              communityHouseWorkerHistory.setHistoryId(curChw.getId());
              communityHouseWorkerHistory.setId(null);
              communityHouseWorkerHistoryRepository.save(communityHouseWorkerHistory);
            });
  }
}
