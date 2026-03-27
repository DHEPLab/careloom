package edu.stanford.fsi.reap.dto;

import edu.stanford.fsi.reap.entity.Lesson;
import edu.stanford.fsi.reap.entity.LessonSchedule;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
public class CurriculumDTO {

  private Long id;

  @NotNull @Size(max = 100)
  private String name;

  @NotNull @Size(max = 1000)
  private String description;

  // TODO Rename all the lesson to session
  @NotEmpty @Valid private List<Lesson> sessions;

  @NotEmpty @Valid private List<LessonSchedule> schedules;
}
