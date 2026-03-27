package edu.stanford.fsi.reap.dto;

import edu.stanford.fsi.reap.entity.Baby;
import edu.stanford.fsi.reap.entity.enumerations.FamilyTies;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * @author hookszhang
 */
@Data
public class CarerDTO {

  @NotNull @Size(min = 1, max = 50)
  private String name;

  @NotNull private String phone;

  @Size(max = 20)
  private String otherContact;

  @NotNull private FamilyTies familyTies;

  private boolean master = false;

  @NotNull private Baby baby;
}
