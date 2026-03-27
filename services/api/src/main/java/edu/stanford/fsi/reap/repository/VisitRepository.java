package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.dto.AdminBabyVisitDTO;
import edu.stanford.fsi.reap.dto.VisitDateDTO;
import edu.stanford.fsi.reap.dto.VisitResultDTO;
import edu.stanford.fsi.reap.entity.User;
import edu.stanford.fsi.reap.entity.Visit;
import edu.stanford.fsi.reap.entity.enumerations.VisitStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

  @Override
  Visit save(Visit entity);

  @Override
  void deleteById(Long id);

  List<Visit> findByStatusIn(List<VisitStatus> status);

  @Query(value = "SELECT * from visit where status='NOT_STARTED'", nativeQuery = true)
  List<Visit> findListByStatusNotStart();

  @Query(
      "select year as year, month as month, day as day "
          + "from Visit where chw.id = ?1 "
          + "group by year, month, day "
          + "order by year desc, month desc, day desc")
  List<VisitDateDTO> findDateByChwId(Long userId);

  @Query("from Visit where baby.id=?1 and status=?2")
  Optional<Visit> findByBabyIdAndStatus(Long babyId, VisitStatus visitStatus);

  @Query(
      "select id as id, baby.name as babyName, baby.approved as babyApproved, lesson.name as"
          + " lessonName, visitTime as visitTime, status as status from Visit where year = ?1 and"
          + " month = ?2 and day = ?3 and chw.id = ?4 order by visitTime asc")
  List<VisitResultDTO> findByDateAndChwId(int year, int month, int day, Long userId);

  @Query(
      "select id as id, lesson.name as lessonName, visitTime as visitTime, status as status, remark"
          + " as remark from Visit where baby.id = ?1 and status <> 'NOT_STARTED' ")
  List<VisitResultDTO> findByBabyIdAndStarted(Long babyId);

  @Query(
      "select id as id, lesson.name as lessonName, visitTime as visitTime, status as status "
          + "from Visit where baby.id = ?1 and status = 'NOT_STARTED' "
          + "order by visitTime desc")
  List<VisitResultDTO> findByBabyIdAndNotStarted(Long babyId);

  @Query("from Visit where id = ?1 and (chw.id = ?2 or baby.chw.id = ?2)")
  Optional<Visit> findByIdAndChwIdOrBabyChwId(Long id, Long userId);

  List<Visit> findByLessonIdAndBabyId(Long lessonId, Long babyId);

  Optional<Visit> findFirstByChwIdAndStatusOrderByVisitTimeAsc(Long userId, VisitStatus status);

  Long deleteByLessonIdAndStatus(Long lessonId, VisitStatus status);

  Long deleteByBabyIdAndStatus(Long babyId, VisitStatus status);

  @Query("from Visit where lesson.id=?1 and status=?2")
  List<Visit> findByLessonIdAndStatus(Long lessonId, VisitStatus status);

  @Query(
      "select id as id, baby.name as babyName, lesson.name as lessonName, lesson as lesson,"
          + " visitTime as visitTime, status as status, remark as remark, distance as distance from"
          + " Visit where baby.id = ?1")
  List<AdminBabyVisitDTO> findByBabyId(Long babyId);

  @Query("from Visit where year = ?1 and month = ?2 and day = ?3 and status = ?4")
  List<Visit> findByDateAndStatus(int year, int month, int day, VisitStatus status);

  @Query(
      "select count(1) from Visit where baby.id = ?1 and (status = 'EXPIRED' or status = 'UNDONE')"
          + " and remark is null")
  int findCountByBabyIdAndStatusAndRemarkIsNull(Long id);

  int countByChw(User chw);

  int countByChwAndStatus(User chw, VisitStatus status);

  @Query("from Visit where chw.id=?1 and status=?2 and deleted=0 order by visitTime")
  List<Visit> findByChwIdAndStatus(Long userId, VisitStatus notStarted);
}
