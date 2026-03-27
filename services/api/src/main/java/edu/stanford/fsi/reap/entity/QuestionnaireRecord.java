package edu.stanford.fsi.reap.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Where(clause = AbstractNormalEntity.SKIP_DELETED_CLAUSE)
@SQLDelete(sql = "UPDATE questionnaire_record SET deleted = true WHERE id = ?")
public class QuestionnaireRecord extends AbstractNormalEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull @Size(max = 500)
  private String name;

  @NotNull @Size(max = 500)
  private String answer;

  @NotNull private String titleNo;

  @JsonIgnore @ManyToOne private Visit visit;
}
