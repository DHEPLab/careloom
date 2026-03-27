package edu.stanford.fsi.reap.dto;

import edu.stanford.fsi.reap.entity.enumerations.CurriculumBranch;
import edu.stanford.fsi.reap.entity.enumerations.ModuleTopic;
import edu.stanford.fsi.reap.pojo.Component;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * @author huey
 */
@Data
public class ModuleRequestDTO {

  private Long id;

  private boolean published = false;

  private CurriculumBranch branch;

  @NotNull @Size(max = 40)
  private String name;

  @NotNull @Size(max = 20)
  private String number;

  @NotNull @Size(max = 200)
  private String description;

  @NotNull private ModuleTopic topic;

  @Valid @NotEmpty private List<Component> components;
}
