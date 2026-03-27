package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.entity.VisitReportHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitReportHistoryRepository extends JpaRepository<VisitReportHistory, Long> {

  @Override
  VisitReportHistory save(VisitReportHistory entity);

  @Override
  void deleteById(Long id);
}
