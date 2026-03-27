package edu.stanford.fsi.reap.dto;

import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuestionnaireRecordResultDTO implements Serializable {

  private List<Long> questionnaireRecordIds;
}
