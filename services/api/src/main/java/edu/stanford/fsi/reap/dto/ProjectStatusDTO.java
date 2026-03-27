package edu.stanford.fsi.reap.dto;

import edu.stanford.fsi.reap.config.Constants;
import lombok.Data;

@Data
public class ProjectStatusDTO {

  private Integer status = Constants.PROJECT_VALID;
}
