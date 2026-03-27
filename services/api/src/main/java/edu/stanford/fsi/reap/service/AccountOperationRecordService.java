package edu.stanford.fsi.reap.service;

import edu.stanford.fsi.reap.config.AccountTypeEnums;
import edu.stanford.fsi.reap.entity.AccountOperationRecord;
import edu.stanford.fsi.reap.repository.AccountOperationRecordRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class AccountOperationRecordService {

  private final AccountOperationRecordRepository repository;

  public AccountOperationRecordService(AccountOperationRecordRepository repository) {
    this.repository = repository;
  }

  /**
   * @param babyId
   * @param isClose
   */
  public void saveBabyAccountOperation(Long babyId, Boolean isClose) {
    if (isClose) {
      AccountOperationRecord accountOperationRecord = new AccountOperationRecord();
      accountOperationRecord.setAccountId(babyId);
      accountOperationRecord.setAccountType(AccountTypeEnums.BABY.name());
      accountOperationRecord.setRevert(false);
      accountOperationRecord.setCloseTime(LocalDateTime.now());
      repository.save(accountOperationRecord);
    } else {
      List<AccountOperationRecord> accountOperationRecords =
          repository.findByAccountIdAndRevert(babyId, false);
      if (!CollectionUtils.isEmpty(accountOperationRecords)) {
        AccountOperationRecord target = accountOperationRecords.get(0);
        target.setRevertTime(LocalDateTime.now());
        target.setRevert(true);
        repository.save(target);
      }
    }
  }
}
