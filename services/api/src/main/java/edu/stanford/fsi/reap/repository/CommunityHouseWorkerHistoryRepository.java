package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.entity.CommunityHouseWorkerHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityHouseWorkerHistoryRepository
    extends JpaRepository<CommunityHouseWorkerHistory, Long> {

  @Override
  CommunityHouseWorkerHistory save(CommunityHouseWorkerHistory entity);

  @Override
  void deleteById(Long id);
}
