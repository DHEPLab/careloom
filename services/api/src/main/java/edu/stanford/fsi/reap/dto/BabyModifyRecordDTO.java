package edu.stanford.fsi.reap.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class BabyModifyRecordDTO {

  private String userName;

  private String roleName;

  private List<String> columnName;

  private List<Object> oldValue;

  private List<Object> newValue;

  private LocalDateTime lastModifiedAt;
}
