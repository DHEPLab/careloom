package edu.stanford.fsi.reap.dto;

import edu.stanford.fsi.reap.entity.enumerations.BabyStage;
import edu.stanford.fsi.reap.entity.enumerations.FeedingPattern;
import edu.stanford.fsi.reap.entity.enumerations.Gender;
import java.time.LocalDate;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class BabyWrapper {
  @NotNull @Size(min = 1, max = 50)
  private String name;

  @NotNull private Gender gender;

  @NotNull private BabyStage stage;

  private LocalDate edc;

  private LocalDate birthday;

  private FeedingPattern feedingPattern;

  private Boolean assistedFood = false;
}
