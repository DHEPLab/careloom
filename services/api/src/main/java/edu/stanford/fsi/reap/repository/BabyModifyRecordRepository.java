package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.entity.BabyModifyRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BabyModifyRecordRepository extends JpaRepository<BabyModifyRecord, Long> {

  @Query("from BabyModifyRecord where babyId=?1  and deleted=0 order by createdAt desc")
  List<BabyModifyRecord> findByBabyId(Long id);

  List<BabyModifyRecord> findByBabyIdAndApprovedFalseOrderByLastModifiedAtDesc(Long babyId);
}
