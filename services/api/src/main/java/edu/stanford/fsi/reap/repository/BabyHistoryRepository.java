package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.entity.BabyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BabyHistoryRepository extends JpaRepository<BabyHistory, Long> {

  @Override
  BabyHistory save(BabyHistory entity);

  @Override
  void deleteById(Long id);

  BabyHistory findFirstByHistoryIdAndApprovedTrueOrderByLastModifiedAtDesc(Long historyId);

  @Query(
      nativeQuery = true,
      value =
          "select * from baby_history where history_id=?1 and identity is not null order by"
              + " last_modified_at limit 1")
  BabyHistory findOriginalBabyHistory(Long historyId);
}
