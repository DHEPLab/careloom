package edu.stanford.fsi.reap.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class CloseAccountReasonWrapper {
  @NotNull @Size(max = 100)
  private String reason;
}
