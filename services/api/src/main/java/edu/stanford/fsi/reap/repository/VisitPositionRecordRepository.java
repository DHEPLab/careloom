package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.entity.VisitPositionRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitPositionRecordRepository extends JpaRepository<VisitPositionRecord, Long> {
  @Override
  VisitPositionRecord save(VisitPositionRecord entity);

  @Query("from VisitPositionRecord where babyId=?1 order by createdAt desc")
  List<VisitPositionRecord> findByBabyId(Long id);
}
