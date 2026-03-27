package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.entity.ModuleHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleHistoryRepository extends JpaRepository<ModuleHistory, Long> {

  @Override
  ModuleHistory save(ModuleHistory entity);

  @Override
  void deleteById(Long id);
}
