package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.dto.ChwUserDTO;
import edu.stanford.fsi.reap.dto.SupervisorUserDTO;
import edu.stanford.fsi.reap.entity.User;
import edu.stanford.fsi.reap.pojo.SimpleUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @author hookszhang
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  @Query("FROM User where projectId=?1 order by createdAt")
  List<User> findByProjectId(Long projectId);

  @Override
  User save(User entity);

  @Override
  void deleteById(Long id);

  @Query("from User user where user.id=?1")
  User findOneById(Long id);

  Optional<User> findOneByUsername(String username);

  Optional<User> findOneByChw_Identity(String identity);

  List<User> findAllByChwSupervisor(User supervisor);

  @Query(
      countQuery =
          "select count(user) from User user where user.role = 'ROLE_CHW' and user.projectId=?3 and"
              + " (?1 is null or ?1 = '' or (user.realName like concat('%', ?1, '%') or"
              + " user.chw.identity like concat('%', ?1, '%') or user.chw.tags like concat('%', ?1,"
              + " '%'))) and (?2 is null or user.chw.supervisor.id = ?2) ",
      value =
          "select new edu.stanford.fsi.reap.dto.ChwUserDTO(user, (select count(baby) from Baby baby"
              + " where baby.deleted = 0 and baby.chw = user)) from User user where user.role ="
              + " 'ROLE_CHW'  and user.projectId=?3 and (?1 is null or ?1 = '' or (user.realName"
              + " like concat('%', ?1, '%') or user.chw.identity like concat('%', ?1, '%') or"
              + " user.chw.tags like concat('%', ?1, '%'))) and (?2 is null or"
              + " user.chw.supervisor.id = ?2) order by user.id desc")
  Page<ChwUserDTO> findChwBySearch(
      String search, Long supervisorId, Long projectId, Pageable pageable);

  @Query(
      countQuery = "select count(user) from User user where user.role = 'ROLE_SUPERVISOR'",
      value =
          "select new edu.stanford.fsi.reap.dto.SupervisorUserDTO(user, (select count(chw.id) from"
              + " CommunityHouseWorker chw where chw.supervisor = user)) from User user where"
              + " user.role = 'ROLE_SUPERVISOR' and user.projectId=?1 order by user.id desc")
  Page<SupervisorUserDTO> findAllSupervisor(Long projectId, Pageable pageable);

  @Query(
      "select user from User user where user.role = 'ROLE_CHW' and user.projectId=?2 and"
          + " user.chw.supervisor is null and (?1 is null or ?1 = '' or (user.realName like"
          + " concat('%', ?1, '%') or user.chw.identity like concat('%', ?1, '%') or user.chw.tags"
          + " like concat('%', ?1, '%')))")
  List<User> findNotAssignedChwBySearch(String search, Long projectId);

  @Query(
      "select id as id, lastModifiedPasswordAt as lastModifiedPasswordAt from User where id = ?1")
  Optional<SimpleUser> findSimpleById(Long userId);

  @Query(
      nativeQuery = true,
      value = "select count(1) from user where username = ?1 and deleted = 1")
  Long findCountByUsernameAndDeletedTrue(String username);

  List<User> findByRoleOrderByCreatedAtDesc(String role);

  Optional<User> findFirstByUsername(String username);
}
