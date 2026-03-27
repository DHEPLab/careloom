package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.entity.VisitReport;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitReportRepository extends JpaRepository<VisitReport, Long> {

  @Override
  VisitReport save(VisitReport entity);

  @Override
  void deleteById(Long id);

  Optional<VisitReport> findByVisitId(Long visitId);

  List<VisitReport> findByCreatedAtBetween(LocalDateTime startDay, LocalDateTime endDay);
}
