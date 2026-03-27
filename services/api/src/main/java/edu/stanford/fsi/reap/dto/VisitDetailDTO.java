package edu.stanford.fsi.reap.dto;

import edu.stanford.fsi.reap.entity.enumerations.VisitStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VisitDetailDTO {

  private Long id;

  private LocalDateTime visitTime;

  private Integer nextModuleIndex;

  private VisitStatus status;

  private String remark;

  private AppBabyDTO baby;

  private AppLessonDTO lesson;

  private LocalDateTime startTime;

  private LocalDateTime completeTime;
}
