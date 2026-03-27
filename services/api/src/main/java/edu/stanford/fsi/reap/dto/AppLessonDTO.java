package edu.stanford.fsi.reap.dto;

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
public class AppLessonDTO {
  private Long id;
  private String name;
  private List<String> moduleNames;
}
