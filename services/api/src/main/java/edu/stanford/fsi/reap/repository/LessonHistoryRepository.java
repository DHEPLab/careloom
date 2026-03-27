package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.entity.LessonHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonHistoryRepository extends JpaRepository<LessonHistory, Long> {
  @Override
  LessonHistory save(LessonHistory entity);

  @Override
  void deleteById(Long id);
}
