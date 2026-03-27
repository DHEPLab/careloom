package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.entity.CurriculumHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurriculumHistoryRepository extends JpaRepository<CurriculumHistory, Long> {

  @Override
  CurriculumHistory save(CurriculumHistory entity);

  @Override
  void deleteById(Long id);
}
