package edu.stanford.fsi.reap.dto;

import edu.stanford.fsi.reap.entity.QuestionnaireRecord;
import edu.stanford.fsi.reap.entity.enumerations.VisitStatus;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateVisitStatusWrapper {

  private Integer nextModuleIndex;

  @NotNull private VisitStatus visitStatus;

  private LocalDateTime startTime;

  @Valid private List<QuestionnaireRecord> questionnaireRecords;
}
