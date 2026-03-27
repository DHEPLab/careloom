package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.entity.BabyUpdateInfo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BabyUpdateInfoRepository extends JpaRepository<BabyUpdateInfo, Long> {
  Optional<BabyUpdateInfo> findByBabyIdAndDeleted(Long babyId, boolean deleted);

  List<BabyUpdateInfo> findByBabyIdInAndUpdateNormalTrueAndDeletedFalse(List<Long> babyIds);
}
