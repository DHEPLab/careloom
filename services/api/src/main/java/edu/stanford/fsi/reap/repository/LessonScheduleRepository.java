package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.entity.LessonSchedule;
import edu.stanford.fsi.reap.entity.enumerations.BabyStage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonScheduleRepository extends JpaRepository<LessonSchedule, Long> {

  @Override
  LessonSchedule save(LessonSchedule entity);

  @Override
  void deleteById(Long id);

  Long deleteByCurriculumId(Long id);

  List<LessonSchedule> findByCurriculumId(Long curriculumId);

  List<LessonSchedule> findByCurriculumIdAndStage(Long curriculumId, BabyStage stage);
}
