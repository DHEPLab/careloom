package edu.stanford.fsi.reap.service;

import edu.stanford.fsi.reap.dto.BabyModifyRecordDTO;
import edu.stanford.fsi.reap.entity.Baby;
import edu.stanford.fsi.reap.entity.BabyModifyRecord;
import edu.stanford.fsi.reap.entity.User;
import edu.stanford.fsi.reap.repository.BabyModifyRecordRepository;
import edu.stanford.fsi.reap.repository.UserRepository;
import edu.stanford.fsi.reap.utils.FieldValueUtil;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class BabyModifyRecordService {

  @Autowired private final BabyModifyRecordRepository repository;
  @Autowired private UserRepository userRepository;

  @Autowired private ResourceBundleMessageSource localSource;

  public BabyModifyRecordService(BabyModifyRecordRepository repository) {
    this.repository = repository;
  }

  public List<BabyModifyRecord> getBabyModifyRecord(Long babyId) {
    return repository.findByBabyId(babyId);
  }

  public List<BabyModifyRecordDTO> getBabyList(Long babyId, String lang) {
    Locale locale = "zh".equals(lang) ? Locale.CHINESE : Locale.ENGLISH;
    List<BabyModifyRecord> babyModifyRecords = repository.findByBabyId(babyId);
    List<BabyModifyRecordDTO> res;
    res =
        babyModifyRecords.stream()
            .map(
                babyModifyRecord -> {
                  BabyModifyRecordDTO dto = new BabyModifyRecordDTO();
                  if (userRepository.findById(babyModifyRecord.getUserId()).isPresent()) {
                    User user = userRepository.findById(babyModifyRecord.getUserId()).get();
                    String role = localSource.getMessage(user.getRole(), null, locale);

                    dto.setRoleName(role);
                    dto.setUserName(user.getUsername());
                    dto.setLastModifiedAt(babyModifyRecord.getLastModifiedAt());
                  }

                  Baby oldBaby = babyModifyRecord.getOldBabyJson();
                  Baby newBaby = babyModifyRecord.getNewBabyJson();
                  ArrayList<Object> oldValues = new ArrayList<>();
                  ArrayList<Object> newValues = new ArrayList<>();
                  String[] split = new String[0];
                  try {
                    Map<String, Object> map = FieldValueUtil.getFieldValuePair(oldBaby, true);
                    Map<String, Object> newBabyMap =
                        FieldValueUtil.getFieldValuePair(newBaby, true);
                    String changedColumn = babyModifyRecord.getChangedColumn();
                    split = changedColumn.split(",");
                    for (String s : split) {

                      if (map.get(s) != null) {
                        oldValues.add(map.get(s));
                      }

                      if (newBabyMap.get(s) != null) {
                        newValues.add(newBabyMap.get(s));
                      }
                    }
                  } catch (IllegalAccessException e) {
                    e.printStackTrace();
                  }
                  dto.setColumnName(Arrays.asList(split));
                  dto.setOldValue(oldValues);
                  dto.setNewValue(newValues);
                  return dto;
                })
            .collect(Collectors.toList());
    return res;
  }
}
