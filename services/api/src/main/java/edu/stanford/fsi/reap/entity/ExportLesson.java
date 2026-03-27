package edu.stanford.fsi.reap.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.stanford.fsi.reap.converter.DomainListConverter;
import edu.stanford.fsi.reap.entity.enumerations.BabyStage;
import edu.stanford.fsi.reap.pojo.Domain;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/** ClassName: ExportLesson Description: author: huangwenxing 2021-4-26 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SQLDelete(sql = "UPDATE lesson SET deleted = true WHERE id = ?")
@Table(name = "lesson")
@Where(clause = AbstractAuditingEntity.NOT_DELETED)
public class ExportLesson extends AbstractAuditingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull @Size(max = 20)
  private String number;

  @NotNull @Size(max = 20)
  private String name;

  @NotNull @Size(max = 200)
  private String description;

  @NotNull @Enumerated(EnumType.STRING)
  @Column(length = 10)
  private BabyStage stage;

  @NotNull private Integer startOfApplicableDays;

  @NotNull private Integer endOfApplicableDays;

  /** Mapper value-> Module.id; label -> Module.number */
  @NotEmpty
  @Column(columnDefinition = "json", nullable = false)
  @Convert(converter = DomainListConverter.class)
  private List<Domain> modules;

  @ManyToOne private Questionnaire questionnaire;

  @Size(max = 100)
  private String questionnaireAddress;

  @Size(max = 100)
  private String smsQuestionnaireAddress;

  @Builder.Default @NotNull private Boolean deleted = false;

  /** Lazy loading allows lessons to be used when curriculum is deleted */
  @ManyToOne(fetch = FetchType.LAZY)
  @JsonIgnore
  private Curriculum curriculum;

  /** Source of draft branch */
  @ManyToOne @JsonIgnore private Lesson source;

  public boolean equalsId(Lesson other) {
    if (other == null) return false;
    return id.equals(other.getId());
  }

  public ExportLesson copyFrom(ExportLesson other) {
    this.number = other.number;
    this.name = other.name;
    this.description = other.description;
    this.stage = other.stage;
    this.startOfApplicableDays = other.startOfApplicableDays;
    this.endOfApplicableDays = other.endOfApplicableDays;
    this.modules = other.modules;
    this.questionnaireAddress = other.questionnaireAddress;
    this.smsQuestionnaireAddress = other.smsQuestionnaireAddress;
    return this;
  }

  @Override
  public String toString() {
    return "Lesson{"
        + "id="
        + id
        + ", number='"
        + number
        + '\''
        + ", name='"
        + name
        + '\''
        + ", description='"
        + description
        + '\''
        + ", stage="
        + stage
        + ", startOfApplicableDays="
        + startOfApplicableDays
        + ", endOfApplicableDays="
        + endOfApplicableDays
        + ", modules="
        + modules
        +
        // do not load curriculum
        ", curriculum=[ignored]"
        + ", questionnaireAddress='"
        + questionnaireAddress
        + '\''
        + ", smsQuestionnaireAddress='"
        + smsQuestionnaireAddress
        + '\''
        + ", source="
        + source
        + '}';
  }
}
