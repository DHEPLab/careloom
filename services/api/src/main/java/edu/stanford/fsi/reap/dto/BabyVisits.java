package edu.stanford.fsi.reap.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BabyVisits {
  private List<VisitResultDTO> started;
  private List<VisitResultDTO> notStarted;
  private int numberOfNoRemark;
}
