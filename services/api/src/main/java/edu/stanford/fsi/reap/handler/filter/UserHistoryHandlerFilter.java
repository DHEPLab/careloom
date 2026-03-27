package edu.stanford.fsi.reap.handler.filter;

import edu.stanford.fsi.reap.entity.Questionnaire;
import edu.stanford.fsi.reap.entity.User;
import edu.stanford.fsi.reap.entity.UserHistory;
import edu.stanford.fsi.reap.repository.UserHistoryRepository;
import edu.stanford.fsi.reap.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserHistoryHandlerFilter implements IHistoryHandlerFilter {

  @Autowired private UserHistoryRepository userHistoryRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private ChwHistoryHandlerFilter chwFilter;

  @Autowired private ModelMapper modelMapper;

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void recordUpdateHistory(Class repository, Object target) {
    if (checkRepository(repository)) {
      User user = (User) target;
      saveHistoryRecord(user.getId());
    } else {
      chwFilter.recordUpdateHistory(repository, target);
    }
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void recordDelHistory(Class repository, Long id) {
    if (checkRepository(repository)) {
      saveHistoryRecord(id);
    } else {
      chwFilter.recordDelHistory(repository, id);
    }
  }

  private boolean checkRepository(Class repository) {
    return repository.equals(Questionnaire.class) ? true : false;
  }

  private void saveHistoryRecord(Long id) {
    userRepository
        .findById(id)
        .ifPresent(
            (curUser) -> {
              UserHistory userHistory = modelMapper.map(curUser, UserHistory.class);
              userHistory.setHistoryId(curUser.getId());
              userHistory.setId(null);
              userHistoryRepository.save(userHistory);
            });
  }
}
