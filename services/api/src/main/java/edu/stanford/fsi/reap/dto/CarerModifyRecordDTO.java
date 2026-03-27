package edu.stanford.fsi.reap.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class CarerModifyRecordDTO {

  private String userName;

  private String roleName;

  private List<String> columnName;

  private List<String> oldValue;

  private List<String> newValue;

  private LocalDateTime lastModifiedAt;
}
