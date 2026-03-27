package edu.stanford.fsi.reap.handler.filter;

import edu.stanford.fsi.reap.entity.Lesson;
import edu.stanford.fsi.reap.entity.LessonHistory;
import edu.stanford.fsi.reap.repository.LessonHistoryRepository;
import edu.stanford.fsi.reap.repository.LessonRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LessonHistoryHandlerFilter implements IHistoryHandlerFilter {

  @Autowired private LessonRepository lessonRepository;

  @Autowired private LessonHistoryRepository lessonHistoryRepository;

  @Autowired private LessonScheduleHistoryHandlerFilter lessonScheduleHistoryHandlerFilter;

  @Autowired private ModelMapper modelMapper;

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void recordUpdateHistory(Class repository, Object target) {
    if (checkRepository(repository)) {
      Lesson lesson = (Lesson) target;
      saveHistoryRecord(lesson.getId());
    } else {
      lessonScheduleHistoryHandlerFilter.recordUpdateHistory(repository, target);
    }
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public void recordDelHistory(Class repository, Long id) {
    if (checkRepository(repository)) {
      saveHistoryRecord(id);
    } else {
      lessonScheduleHistoryHandlerFilter.recordDelHistory(repository, id);
    }
  }

  private boolean checkRepository(Class repository) {
    return repository.equals(Lesson.class) ? true : false;
  }

  private void saveHistoryRecord(Long id) {
    lessonRepository
        .findById(id)
        .ifPresent(
            curLesson -> {
              LessonHistory lessonHistory = modelMapper.map(curLesson, LessonHistory.class);
              lessonHistory.setId(null);
              lessonHistory.setHistoryId(curLesson.getId());
              lessonHistoryRepository.save(lessonHistory);
            });
  }
}
