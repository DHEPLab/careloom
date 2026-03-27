package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.entity.LessonScheduleHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonScheduleHistoryRepository
    extends JpaRepository<LessonScheduleHistory, Long> {

  @Override
  LessonScheduleHistory save(LessonScheduleHistory entity);

  @Override
  void deleteById(Long id);
}
