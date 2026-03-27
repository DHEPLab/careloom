package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.entity.VisitHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitHistoryRepository extends JpaRepository<VisitHistory, Long> {

  @Override
  VisitHistory save(VisitHistory entity);

  @Override
  void deleteById(Long id);
}
