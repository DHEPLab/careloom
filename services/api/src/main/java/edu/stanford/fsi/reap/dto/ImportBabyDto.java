package edu.stanford.fsi.reap.dto;

import edu.stanford.fsi.reap.entity.Carer;
import edu.stanford.fsi.reap.entity.User;
import edu.stanford.fsi.reap.entity.enumerations.ActionFromApp;
import edu.stanford.fsi.reap.entity.enumerations.BabyStage;
import edu.stanford.fsi.reap.entity.enumerations.FeedingPattern;
import edu.stanford.fsi.reap.entity.enumerations.Gender;
import java.time.LocalDate;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/** ClassName: ImportBabyDto Description: author: huangwenxing 2021-7-9 */
@Data
public class ImportBabyDto {

  private Long id;

  private Integer number;

  @NotNull @Size(min = 1, max = 50)
  private String name;

  @Size(max = 50)
  private String identity;

  @NotNull private Gender gender;

  @NotNull private BabyStage stage;

  /** required on baby stage is EDC expected date of confinement Accurate to the day */
  private LocalDate edc;

  /** required on baby stage is birth Accurate to the day */
  private LocalDate birthday;

  /** required on baby stage is birth */
  private FeedingPattern feedingPattern;

  private Boolean assistedFood = false;

  /** Babies added on the app side need to be reviewed */
  private Boolean approved = true;

  /** Actions from APP that require approval */
  private ActionFromApp actionFromApp;

  /**
   * https://github.com/modood/Administrative-divisions-of-China “省份、城市、区县、乡镇” 四级联动数据 省份/城市/区县/乡镇
   */
  @NotNull @Size(max = 100)
  private String area;

  @NotNull @Size(max = 200)
  private String location;

  @Size(max = 500)
  private String remark;

  private User chw;

  @Size(max = 100)
  private String closeAccountReason;

  private List<Carer> cares;
}
