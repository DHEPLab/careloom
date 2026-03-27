package edu.stanford.fsi.reap.entity;

import edu.stanford.fsi.reap.entity.enumerations.VisitStatus;
import java.time.LocalDateTime;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

/** ClassName: ExportVisit Description: author: huangwenxing 2021-5-6 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "visit")
public class ExportVisit extends AbstractAuditingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Builder.Default @NotNull private Boolean deleted = false;

  @NotNull private LocalDateTime visitTime;

  /** Visit year, search index */
  @NotNull private Integer year;

  /** Visit month, search index */
  @NotNull private Integer month;

  /** Visit day, search index */
  @NotNull private Integer day;

  @NotNull @ManyToOne private Baby baby;

  @NotNull @ManyToOne private ExportLesson lesson;

  @Builder.Default @NotNull private Integer nextModuleIndex = 0;

  @Builder.Default
  @NotNull @Enumerated(EnumType.STRING)
  @Column(length = 15)
  private VisitStatus status = VisitStatus.NOT_STARTED;

  /** The CHW that actually completes the visit */
  @ManyToOne(fetch = FetchType.LAZY)
  @NotFound(action = NotFoundAction.IGNORE)
  private User chw;

  private LocalDateTime startTime;

  private LocalDateTime completeTime;

  @Size(max = 200)
  private String remark;

  @Size(max = 200)
  private String deleteReason;

  public ExportVisit yearMonthDay(LocalDateTime visitTime) {
    this.year = visitTime.getYear();
    this.month = visitTime.getMonthValue();
    this.day = visitTime.getDayOfMonth();
    return this;
  }

  public boolean readonly() {
    return VisitStatus.EXPIRED.equals(status) || VisitStatus.DONE.equals(status);
  }

  public boolean done() {
    return VisitStatus.DONE.equals(status);
  }

  public boolean notStarted() {
    return VisitStatus.NOT_STARTED.equals(status);
  }
}
