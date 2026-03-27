package edu.stanford.fsi.reap.dto;

import edu.stanford.fsi.reap.entity.Lesson;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NextShouldVisitDTO {

  private List<LocalDate> visitDateRange;
  private Lesson lesson;
}
