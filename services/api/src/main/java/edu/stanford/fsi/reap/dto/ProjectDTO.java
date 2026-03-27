package edu.stanford.fsi.reap.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProjectDTO {

  @NotNull private String name;

  private Integer status;
}
