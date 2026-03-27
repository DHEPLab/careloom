package edu.stanford.fsi.reap.handler.filter;

import edu.stanford.fsi.reap.entity.Baby;
import edu.stanford.fsi.reap.entity.TagHistory;
import edu.stanford.fsi.reap.repository.BabyRepository;
import edu.stanford.fsi.reap.repository.TagHistoryRepository;
import edu.stanford.fsi.reap.repository.TagRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TagHistoryHandlerFilter implements IHistoryHandlerFilter {

  @Autowired private TagRepository tagRepository;

  @Autowired private TagHistoryRepository tagHistoryRepository;

  @Autowired private ModelMapper modelMapper;

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void recordUpdateHistory(Class repository, Object target) {
    if (checkRepository(repository)) {
      Baby baby = (Baby) target;
      saveHistoryRecord(baby.getId());
    } else {
      // TODO
    }
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void recordDelHistory(Class repository, Long id) {
    if (checkRepository(repository)) {
      saveHistoryRecord(id);
    } else {
      // TODO
    }
  }

  private boolean checkRepository(Class repository) {
    return repository.equals(BabyRepository.class) ? true : false;
  }

  private void saveHistoryRecord(Long id) {
    tagRepository
        .findById(id)
        .ifPresent(
            curTag -> {
              TagHistory tagHistory = modelMapper.map(curTag, TagHistory.class);
              tagHistory.setHistoryId(curTag.getId());
              tagHistory.setId(null);
              tagHistoryRepository.save(tagHistory);
            });
  }
}
