package edu.stanford.fsi.reap.dto;

import static edu.stanford.fsi.reap.entity.enumerations.CurriculumBranch.MASTER;

import edu.stanford.fsi.reap.entity.Lesson;
import edu.stanford.fsi.reap.entity.LessonSchedule;
import edu.stanford.fsi.reap.entity.enumerations.CurriculumBranch;
import edu.stanford.fsi.reap.entity.enumerations.CurriculumStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hookszhang
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurriculumResultDTO {

  private Long id;

  private String name;

  private String description;

  private CurriculumStatus status;

  // TODO rename all lessons to sessions
  private List<Lesson> sessions;

  private List<LessonSchedule> schedules;

  private LocalDateTime lastPublishedAt;

  private boolean hasDraft;

  private LocalDateTime lastModifiedDraftAt;

  private CurriculumBranch branch;

  private Long sourceId;

  @Builder.Default private boolean published = false;

  public boolean publishedMasterBranch() {
    return MASTER.equals(branch) && published;
  }
}
