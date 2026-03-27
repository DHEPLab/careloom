package edu.stanford.fsi.reap.dto;

import edu.stanford.fsi.reap.pojo.Question;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppOfflineLessonDTO {
  private Long id;
  private String name;
  private String description;
  private List<BasicModule> modules;
  private String questionnaireAddress;
  private BasicQuestionnaire questionnaire;

  @Data
  @AllArgsConstructor
  public static class BasicModule {
    private Long id;
    private String number;
    private String name;
  }

  @Data
  @AllArgsConstructor
  public static class BasicQuestionnaire {
    private Long id;
    private String name;
    private List<Question> questions;
  }
}
