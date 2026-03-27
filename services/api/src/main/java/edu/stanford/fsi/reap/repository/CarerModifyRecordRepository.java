package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.entity.CarerModifyRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CarerModifyRecordRepository extends JpaRepository<CarerModifyRecord, Long> {

  @Override
  void deleteById(Long id);

  @Query("from CarerModifyRecord where carerId=?1 order by createdAt desc")
  List<CarerModifyRecord> findByCarerId(Long id);
}
