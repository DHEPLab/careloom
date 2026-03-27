package edu.stanford.fsi.reap.dto;

import edu.stanford.fsi.reap.entity.ExportLesson;
import edu.stanford.fsi.reap.entity.enumerations.VisitStatus;
import java.time.LocalDateTime;

/**
 * @author hookszhang
 */
public interface AdminBabyVisitDTO {
  Long getId();

  ExportLesson getLesson();

  LocalDateTime getVisitTime();

  VisitStatus getStatus();

  String getRemark();

  Double getDistance();
}
