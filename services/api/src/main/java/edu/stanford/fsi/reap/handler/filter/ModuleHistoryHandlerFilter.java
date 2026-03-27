package edu.stanford.fsi.reap.handler.filter;

import edu.stanford.fsi.reap.entity.Module;
import edu.stanford.fsi.reap.entity.ModuleHistory;
import edu.stanford.fsi.reap.repository.ModuleHistoryRepository;
import edu.stanford.fsi.reap.repository.ModuleRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ModuleHistoryHandlerFilter implements IHistoryHandlerFilter {
  @Autowired private ModuleRepository moduleRepository;

  @Autowired private ModuleHistoryRepository moduleHistoryRepository;

  @Autowired private QuestionnaireHistoryHandlerFilter questionnaireHistoryHandlerFilter;

  @Autowired private ModelMapper modelMapper;

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void recordUpdateHistory(Class repository, Object target) {
    if (checkRepository(repository)) {
      Module module = (Module) target;
      saveHistoryRecord(module.getId());
    } else {
      questionnaireHistoryHandlerFilter.recordUpdateHistory(repository, target);
    }
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void recordDelHistory(Class repository, Long id) {
    if (checkRepository(repository)) {
      saveHistoryRecord(id);
    } else {
      questionnaireHistoryHandlerFilter.recordDelHistory(repository, id);
    }
  }

  private boolean checkRepository(Class repository) {
    return repository.equals(Module.class) ? true : false;
  }

  private void saveHistoryRecord(Long id) {
    moduleRepository
        .findById(id)
        .ifPresent(
            curModule -> {
              ModuleHistory moduleHistory = modelMapper.map(curModule, ModuleHistory.class);
              moduleHistory.setId(null);
              moduleHistory.setHistoryId(curModule.getId());
              moduleHistoryRepository.save(moduleHistory);
            });
  }
}
