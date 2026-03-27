package edu.stanford.fsi.reap.handler.filter;

import edu.stanford.fsi.reap.entity.LessonSchedule;
import edu.stanford.fsi.reap.entity.LessonScheduleHistory;
import edu.stanford.fsi.reap.repository.LessonScheduleHistoryRepository;
import edu.stanford.fsi.reap.repository.LessonScheduleRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LessonScheduleHistoryHandlerFilter implements IHistoryHandlerFilter {

  @Autowired private LessonScheduleRepository lessonScheduleRepository;

  @Autowired private LessonScheduleHistoryRepository lessonScheduleHistoryRepository;

  @Autowired private CurriculumHistoryHandlerFilter curriculumHistoryHandlerFilter;

  @Autowired private ModelMapper modelMapper;

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void recordUpdateHistory(Class repository, Object target) {
    if (checkRepository(repository)) {
      LessonSchedule lessonSchedule = (LessonSchedule) target;
      saveHistoryRecord(lessonSchedule.getId());
    } else {
      curriculumHistoryHandlerFilter.recordUpdateHistory(repository, target);
    }
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void recordDelHistory(Class repository, Long id) {
    if (checkRepository(repository)) {
      saveHistoryRecord(id);
    } else {
      curriculumHistoryHandlerFilter.recordDelHistory(repository, id);
    }
  }

  private boolean checkRepository(Class repository) {
    return repository.equals(LessonSchedule.class) ? true : false;
  }

  private void saveHistoryRecord(Long id) {
    lessonScheduleRepository
        .findById(id)
        .ifPresent(
            curLessonSchedule -> {
              LessonScheduleHistory lessonScheduleHistory =
                  modelMapper.map(curLessonSchedule, LessonScheduleHistory.class);
              lessonScheduleHistory.setId(null);
              lessonScheduleHistory.setHistoryId(curLessonSchedule.getId());
              lessonScheduleHistoryRepository.save(lessonScheduleHistory);
            });
  }
}
