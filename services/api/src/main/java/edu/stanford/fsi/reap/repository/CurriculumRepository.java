package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.entity.Curriculum;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CurriculumRepository extends JpaRepository<Curriculum, Long> {

  @Override
  Curriculum save(Curriculum entity);

  @Override
  void deleteById(Long id);

  @Query(
      "select c from Curriculum c where c.branch = 'MASTER' and c.projectId=?2 and (?1 is null or"
          + " ?1 = '' or c.name like concat('%', ?1, '%')) ")
  Page<Curriculum> findBySearch(String search, Long projectId, Pageable pageable);

  Optional<Curriculum> findFirstBySourceId(Long id);
}
