package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.entity.UserHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author hookszhang
 */
@Repository
public interface UserHistoryRepository extends JpaRepository<UserHistory, Long> {
  @Override
  UserHistory save(UserHistory entity);

  @Override
  void deleteById(Long id);
}
