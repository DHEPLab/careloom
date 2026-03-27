package edu.stanford.fsi.reap.dto;

import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VisitTimeWrapper {

  @NotNull private LocalDateTime visitTime;
}
