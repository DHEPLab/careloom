package edu.stanford.fsi.reap.entity;

import edu.stanford.fsi.reap.entity.enumerations.ActionFromApp;
import edu.stanford.fsi.reap.entity.enumerations.BabyStage;
import edu.stanford.fsi.reap.entity.enumerations.FeedingPattern;
import edu.stanford.fsi.reap.entity.enumerations.Gender;
import java.time.LocalDate;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Where(clause = AbstractHistoryEntity.SKIP_DELETED_CLAUSE)
@SQLDelete(sql = "UPDATE baby_history SET deleted = true WHERE id = ?")
public class BabyHistory extends AbstractHistoryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull @Size(min = 1, max = 50)
  private String name;

  @Size(max = 50)
  private String identity;

  @NotNull @Enumerated(EnumType.STRING)
  @Column(length = 10)
  private Gender gender;

  @NotNull @Enumerated(EnumType.STRING)
  @Column(length = 10)
  private BabyStage stage;

  /** required on baby stage is EDC expected date of confinement Accurate to the day */
  private LocalDate edc;

  /** required on baby stage is birth Accurate to the day */
  private LocalDate birthday;

  /** required on baby stage is birth */
  @Enumerated(EnumType.STRING)
  @Column(length = 50)
  private FeedingPattern feedingPattern;

  @Builder.Default
  @Column(nullable = false)
  private Boolean assistedFood = false;

  /** Babies added on the app side need to be reviewed */
  @Builder.Default
  @Column(nullable = false)
  private Boolean approved = true;

  /** Actions from APP that require approval */
  @Enumerated(EnumType.STRING)
  @Column(length = 10)
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

  @ManyToOne private Chw chw;

  @ManyToOne private Curriculum curriculum;

  @Size(max = 100)
  private String closeAccountReason;

  private Double longitude;

  private Double latitude;

  private Boolean showLocation;

  private Long historyId;

  @Column(name = "project_id")
  private Long projectId;

  public boolean approveCreate() {
    return ActionFromApp.CREATE.equals(actionFromApp);
  }

  public boolean approveDelete() {
    return ActionFromApp.DELETE.equals(actionFromApp);
  }

  public boolean noCurriculum() {
    return this.curriculum == null;
  }
}
