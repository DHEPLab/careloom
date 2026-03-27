package edu.stanford.fsi.reap.dto;

import edu.stanford.fsi.reap.entity.Baby;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author hookszhang
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BabyDetailDTO extends Baby {
  private int months;
  private int days;
  private boolean deleted;
  private LocalDateTime lastModifiedAt;
  private boolean canCreate = false;
}
