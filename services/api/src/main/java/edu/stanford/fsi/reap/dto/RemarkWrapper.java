package edu.stanford.fsi.reap.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class RemarkWrapper {
  @NotNull @Size(max = 500)
  private String remark;
}
