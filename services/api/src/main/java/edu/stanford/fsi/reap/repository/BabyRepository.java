package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.dto.AppBabyDTO;
import edu.stanford.fsi.reap.dto.AssignBabyDTO;
import edu.stanford.fsi.reap.entity.Baby;
import edu.stanford.fsi.reap.entity.Curriculum;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BabyRepository extends JpaRepository<Baby, Long> {

  @Override
  Baby save(Baby entity);

  @Override
  void deleteById(Long id);

  @Query(
      "select b.id as id, b.name as name, b.identity as identity, b.gender as gender, (select"
          + " c.name from Carer c where c.baby = b and c.master = true) as masterCarerName, (select"
          + " c.phone from Carer c where c.baby = b and c.master = true) as masterCarerPhone from"
          + " Baby b where b.deleted = 0 and b.chw.id = ?1 order by b.id desc")
  List<AssignBabyDTO> findAssignBabyByChwId(Long chwId);

  List<Baby> findByOrderByCreatedAtDesc();

  @Query(
      value =
          "select new edu.stanford.fsi.reap.dto.AppBabyDTO(b.id, b.name, b.identity, b.gender,"
              + " b.stage, b.edc, b.birthday, b.area, b.location, b.approved, b.remark ) from Baby"
              + " b where b.deleted = 0 and b.chw.id = ?1 and (?2 is null or ?2 = '' or b.name like"
              + " concat('%', ?2, '%')) ")
  Page<AppBabyDTO> findAppBabyByChwIdAndName(Long chwId, String name, Pageable pageable);

  @Query(
      nativeQuery = true,
      value =
          "SELECT b.* FROM baby b JOIN user u ON b.chw_id = u.id JOIN community_house_worker chw on"
              + " u.chw_id = chw.id WHERE b.approved=false AND u.deleted=false and if(?1 !='',"
              + " b.name LIKE concat('%',?1,'%') OR b.area LIKE concat('%',?1,'%') OR b.identity"
              + " LIKE concat('%',?1,'%') , 1=1) and if(?2 !='', chw.supervisor_id = ?2 , 1=1)"
              + " ORDER BY CONVERT(b.name USING gbk) COLLATE gbk_chinese_ci ASC limit ?3, ?4")
  List<Baby> findBySearchAndSupervisorIdAndApprovedFalseOrderByNameASC(
      String search, Long userId, int i, int pageSize);

  @Query(
      nativeQuery = true,
      value =
          "SELECT b.* FROM baby b JOIN user u ON b.chw_id = u.id JOIN community_house_worker chw on"
              + " u.chw_id = chw.id WHERE b.approved=false AND u.deleted=false and if(?1 !='',"
              + " b.name LIKE concat('%',?1,'%') OR b.area LIKE concat('%',?1,'%') OR b.identity"
              + " LIKE concat('%',?1,'%') , 1=1) and if(?2 !='', chw.supervisor_id = ?2 , 1=1)"
              + " ORDER BY CONVERT(b.name USING gbk) COLLATE gbk_chinese_ci DESC limit ?3, ?4")
  List<Baby> findBySearchAndSupervisorIdAndApprovedFalseOrderByNameDesc(
      String search, Long userId, int i, int pageSize);

  @Query(
      value =
          "select count(1) FROM Baby baby JOIN User user ON baby.chw.id = user.id JOIN"
              + " CommunityHouseWorker chw on user.chw.id = chw.id WHERE baby.approved=false AND"
              + " user.deleted=false AND chw.supervisor.id = ?2 AND (?1 IS NULL OR ?1='' OR"
              + " (baby.name LIKE concat('%',?1,'%') OR baby.area LIKE concat('%',?1,'%') OR"
              + " baby.identity LIKE concat('%',?1,'%'))) ")
  Integer findTotalBySearchAndSupervisorIdAndApprovedFalse(String search, Long userId);

  @Query(value = "select c.name from Carer c where c.baby.id = ?1 and c.master = true")
  String getNameByBabyId(Long babyId);

  @Query(value = "select c.phone from Carer c where c.baby.id = ?1 and c.master = true")
  String getPhoneByBabyId(Long babyId);

  @Query(value = "select c from Curriculum  c where c.id = ?1 and c.published = true ")
  Curriculum getCurriculumByBabyId(Long babyCurriculumId);

  Optional<Baby> findByIdAndChwIdAndDeletedFalseAndApprovedTrue(Long id, Long chwId);

  @Query(
      value =
          "FROM Baby baby WHERE baby.approved = true AND baby.projectId=?2 AND (?1 IS NULL OR ?1=''"
              + " OR (baby.name LIKE concat('%',?1,'%') OR baby.area LIKE concat('%',?1,'%') OR"
              + " baby.identity LIKE concat('%',?1,'%')))")
  Page<Baby> findBySearchAndApprovedTrueOrderBy(String search, Long projectId, Pageable pageable);

  @Query(
      nativeQuery = true,
      value =
          "SELECT count(1) FROM visit WHERE (visit.deleted=0) AND visit.baby_id=?1 AND"
              + " (visit.status='DONE' OR visit.status='UNDONE')")
  Integer getVisitCountByBabyId(Long babyId);

  @Query(
      nativeQuery = true,
      value =
          "SELECT lesson.name FROM visit CROSS JOIN lesson WHERE (visit.deleted=0) AND"
              + " visit.lesson_id=lesson.id AND visit.baby_id=?1 ORDER BY visit.id DESC LIMIT 1 ")
  String getCurrentLessonNameByBabyId(Long babyId);

  @Query(
      value =
          "FROM Baby baby WHERE baby.approved=false AND baby.projectId=?2 AND (?1 IS NULL OR ?1=''"
              + " OR (baby.name LIKE concat('%',?1,'%') OR baby.area LIKE concat('%',?1,'%') OR"
              + " baby.identity LIKE concat('%',?1,'%'))) ")
  Page<Baby> findBySearchAndApprovedFalse(String search, Long projectId, Pageable pageable);

  @Query(
      value =
          "SELECT baby FROM Baby baby JOIN User user ON baby.chw.id = user.id JOIN"
              + " CommunityHouseWorker chw on user.chw.id = chw.id WHERE baby.approved=false AND"
              + " baby.projectId=?3 AND user.deleted=false AND chw.supervisor.id = ?2 AND (?1 IS"
              + " NULL OR ?1='' OR (baby.name LIKE concat('%',?1,'%') OR baby.area LIKE"
              + " concat('%',?1,'%') OR baby.identity LIKE concat('%',?1,'%'))) ")
  Page<Baby> findBySearchAndSupervisorIdAndApprovedFalse(
      String search, Long supervisorId, Long projectId, Pageable pageablex);

  @Query(
      "select b from Baby b where b.deleted = 0 and b.projectId=?2 and b.chw is null and (?1 is"
          + " null or (b.name like concat('%', ?1, '%') or b.area like concat('%', ?1, '%') or"
          + " b.identity = ?1)) order by b.id desc")
  Page<Baby> findByChwIsNullAndSearchOrderByIdDesc(
      String search, Long projectId, Pageable pageable);

  @Query(
      "select b from Baby b where b.deleted = 0 and b.projectId=?3 and (b.curriculum.id is null or"
          + " b.curriculum.id <> ?1) and (?2 is null or ?2 = '' or (b.name like concat('%', ?2,"
          + " '%') or b.area like concat('%', ?2, '%') or b.identity like concat('%', ?2, '%')))"
          + " order by b.id desc")
  Page<Baby> findByCurriculumIdIsNotAndSearchAndOrderByIdDesc(
      Long curriculumId, String search, Long projectId, Pageable pageable);

  @Query(
      countQuery = "select count(1) from Baby b where b.deleted = 0 and b.curriculum.id = ?1",
      value =
          "select b.id as id, b.name as name, b.identity as identity, b.gender as gender, b.area as"
              + " area, (select c.name from Carer c where c.baby = b and c.master = true) as"
              + " masterCarerName, (select c.phone from Carer c where c.baby = b and c.master ="
              + " true) as masterCarerPhone from Baby b where b.deleted = 0 and b.curriculum.id ="
              + " ?1 and b.projectId=?2 order by b.lastModifiedAt desc")
  Page<AssignBabyDTO> findByCurriculumId(Long id, Long projectId, Pageable pageable);

  @Query(
      nativeQuery = true,
      value =
          "select b.* FROM Baby b WHERE b.approved=?1 and if(?2 !='', b.name LIKE"
              + " concat('%',?2,'%') OR b.area LIKE concat('%',?2,'%') OR b.identity LIKE"
              + " concat('%',?2,'%') , 1=1) ORDER BY CONVERT(b.name USING gbk) COLLATE"
              + " gbk_chinese_ci ASC limit ?3, ?4")
  List<Baby> findBySearchAndApprovedOrderByNameAsc(
      Boolean approved, String search, Integer page, Integer Number);

  @Query(
      nativeQuery = true,
      value =
          "select b.* FROM Baby b WHERE b.approved=?1and if(?2 !='', b.name LIKE concat('%',?2,'%')"
              + " OR b.area LIKE concat('%',?2,'%') OR b.identity LIKE concat('%',?2,'%') , 1=1)"
              + " ORDER BY CONVERT(b.name USING gbk) COLLATE gbk_chinese_ci DESC limit ?3, ?4")
  List<Baby> findBySearchAndApprovedOrderByNameDESC(
      Boolean approved, String search, Integer page, Integer Number);

  @Query(
      value =
          "select count(1) FROM Baby baby WHERE baby.approved=?1 AND (?2 IS NULL OR ?2='' OR"
              + " (baby.name LIKE concat('%',?2,'%') OR baby.area LIKE concat('%',?2,'%') OR"
              + " baby.identity LIKE concat('%',?2,'%'))) ")
  Integer findBySearchAndApprovedTotal(Boolean approved, String search);

  List<Baby> findByChwIdAndCurriculumIdNotNullAndDeletedFalseAndApprovedTrue(Long userId);

  Optional<Baby> findFirstByIdentity(String identity);

  List<Baby> findByChwIdAndDeletedFalse(Long id);

  Optional<Baby> findByIdAndChwIdAndDeletedFalse(Long id, Long chwId);

  List<Baby> findBabiesByCurriculumId(Long curriculumId);

  List<Baby> findAllByDeleted(Boolean deleted);

  List<Baby> findByChwIdAndCurriculumIdNotNullAndDeletedFalse(Long userId);
}
