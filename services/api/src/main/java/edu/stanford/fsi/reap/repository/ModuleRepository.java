package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.dto.ModuleDTO;
import edu.stanford.fsi.reap.entity.Module;
import edu.stanford.fsi.reap.entity.enumerations.CurriculumBranch;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {

  @Override
  Module save(Module entity);

  @Override
  void deleteById(Long id);

  @Query(
      countQuery =
          "select count(m) from Module m where m.branch = 'MASTER' and m.projectId=?3 and (?1 is"
              + " null or m.name like concat('?', ?1, '?')) and (?2 is null or m.published = ?2) ",
      value =
          "select m.id as id, m.number as number, m.name as name, m.topic as topic, m.branch as"
              + " branch, m.published as published from Module m where m.branch = 'MASTER' and"
              + " m.projectId=?3 and (?1 is null or ?1 = '' or m.name like concat('%', ?1, '%'))"
              + " and (?2 is null or m.published = ?2) order by m.topic asc, m.number asc ")
  Page<ModuleDTO> findBySearch(String search, Boolean published, Long projectId, Pageable pageable);

  Optional<Module> findOneByVersionKeyAndBranch(String versionKey, CurriculumBranch master);

  Optional<Module> findByNumberAndBranch(String number, CurriculumBranch master);

  @Query("select name from Module where id in ?1 ORDER BY FIELD(id, ?1)")
  List<String> findNamesInIdList(List<Long> moduleIds);

  List<Module> findByBranchAndPublishedTrue(CurriculumBranch master);

  Optional<Module> findFirstByBranchAndPublishedTrueOrderByLastModifiedAtDesc(
      CurriculumBranch master);

  Module findByName(String name);
}
