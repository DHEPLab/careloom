package edu.stanford.fsi.reap.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UploadVisitLocationWrapper {

  private Long visitId;

  private Long babyId;

  @NotNull private Double longitude;

  @NotNull private Double latitude;
}
