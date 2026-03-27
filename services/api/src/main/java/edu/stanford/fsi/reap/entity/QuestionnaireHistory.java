package edu.stanford.fsi.reap.entity;

import edu.stanford.fsi.reap.converter.QuestionConverter;
import edu.stanford.fsi.reap.entity.enumerations.QuestionnaireBranch;
import edu.stanford.fsi.reap.pojo.Question;
import java.util.List;
import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
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
@SQLDelete(sql = "UPDATE questionnaire_history SET deleted = true WHERE id = ?")
public class QuestionnaireHistory extends AbstractHistoryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull @Size(max = 40)
  private String name;

  @Valid
  @NotEmpty
  @Column(columnDefinition = "json")
  @Convert(converter = QuestionConverter.class)
  private List<Question> questions;

  @Enumerated(EnumType.STRING)
  @Column(length = 10, nullable = false)
  private QuestionnaireBranch branch;

  @Builder.Default private boolean published = false;

  private Long sourceId;

  private Long historyId;

  @Column(name = "project_id")
  private Long projectId;
}
