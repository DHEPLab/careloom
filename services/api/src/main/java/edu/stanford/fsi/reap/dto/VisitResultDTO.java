package edu.stanford.fsi.reap.dto;

import edu.stanford.fsi.reap.entity.enumerations.VisitStatus;
import java.time.LocalDateTime;

public interface VisitResultDTO {
  Long getId();

  String getBabyName();

  Boolean getBabyApproved();

  String getLessonName();

  LocalDateTime getVisitTime();

  VisitStatus getStatus();

  String getRemark();
}
