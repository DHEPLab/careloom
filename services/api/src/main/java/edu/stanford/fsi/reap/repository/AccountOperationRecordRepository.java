package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.entity.AccountOperationRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountOperationRecordRepository
    extends JpaRepository<AccountOperationRecord, Long> {

  public List<AccountOperationRecord> findByAccountIdAndRevert(Long accuntId, Boolean revert);
}
