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
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.SQLDelete;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
// The closed baby can be seen on the admin portal, but not on the app.
// @Where(clause = AbstractAuditingEntity.SKIP_DELETED_CLAUSE)
@SQLDelete(sql = "UPDATE baby SET deleted = true WHERE id = ?")
@Proxy(lazy = false)
@ToString
public class Baby extends AbstractAuditingEntity {

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

  private Double longitude;

  private Double latitude;

  private Boolean showLocation;

  @Size(max = 100)
  private String closeAccountReason;

  public Baby update(Baby baby) {
    this.identity = baby.identity;
    this.name = baby.name;
    this.gender = baby.gender;
    this.stage = baby.stage;
    this.edc = baby.edc;
    this.birthday = baby.birthday;
    this.feedingPattern = baby.feedingPattern;
    this.assistedFood = baby.assistedFood;
    this.area = baby.area;
    this.location = baby.location;
    this.remark = baby.remark;
    this.longitude = baby.longitude;
    this.latitude = baby.latitude;
    this.showLocation = baby.showLocation;
    return this;
  }

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
