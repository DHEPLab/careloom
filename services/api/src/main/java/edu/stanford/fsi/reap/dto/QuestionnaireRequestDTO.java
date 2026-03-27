package edu.stanford.fsi.reap.dto;

import edu.stanford.fsi.reap.pojo.Question;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class QuestionnaireRequestDTO {

  private Long id;

  @NotNull @Size(max = 40)
  private String name;

  @Valid
  @NotEmpty
  @Size(max = 20) // 问题限制20个，超出20前端提示
  private List<Question> questions;
}
