package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.entity.Lesson;
import edu.stanford.fsi.reap.entity.enumerations.BabyStage;
import edu.stanford.fsi.reap.entity.enumerations.CurriculumBranch;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

  @Override
  Lesson save(Lesson entity);

  @Override
  void deleteById(Long id);

  List<Lesson> findByCurriculumId(Long curriculumId);

  @Query(
      nativeQuery = true,
      value =
          "SELECT count(JSON_SEARCH(lesson.modules,'all',?1)) FROM lesson JOIN curriculum ON"
              + " lesson.curriculum_id=curriculum.id WHERE curriculum.deleted=0 AND"
              + " lesson.deleted=0")
  Long countByModuleId(Long id);

  List<Lesson> findByQuestionnaireId(Long id);

  @Query(
      nativeQuery = true,
      value =
          "SELECT lesson.*FROM lesson JOIN curriculum ON lesson.curriculum_id=curriculum.id WHERE"
              + " curriculum.deleted=0 AND lesson.deleted=0 AND"
              + " JSON_SEARCH(lesson.modules,'all',?1) IS NOT NULL")
  List<Lesson> findByModuleId(Long id);

  List<Lesson> findByCurriculumIdAndStage(Long curriculumId, BabyStage stage);

  List<Lesson> findByCurriculumBranchAndCurriculumPublishedTrue(CurriculumBranch master);

  Optional<Lesson> findFirstByCurriculumBranchAndCurriculumPublishedTrueOrderByLastModifiedAtDesc(
      CurriculumBranch master);
}
