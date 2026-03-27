package edu.stanford.fsi.reap.dto;

import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hookszhang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitDTO {

  @NotNull private LocalDateTime visitTime;

  @NotNull private Long babyId;

  @NotNull private Long lessonId;
}
