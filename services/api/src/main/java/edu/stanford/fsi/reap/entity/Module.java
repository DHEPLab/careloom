package edu.stanford.fsi.reap.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.stanford.fsi.reap.converter.ComponentListConverter;
import edu.stanford.fsi.reap.entity.enumerations.CurriculumBranch;
import edu.stanford.fsi.reap.entity.enumerations.ModuleTopic;
import edu.stanford.fsi.reap.pojo.Component;
import java.util.List;
import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * Curriculum Module
 *
 * @author hookszhang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Where(clause = AbstractAuditingEntity.SKIP_DELETED_CLAUSE)
@SQLDelete(sql = "UPDATE module SET deleted = true WHERE id = ?")
public class Module extends AbstractAuditingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * Uniquely identifies a module, different versions of the same module are allowed in the table.
   * The same module display only the most recent published module
   */
  @JsonIgnore
  @Column(length = 32, nullable = false)
  private String versionKey;

  @Enumerated(EnumType.STRING)
  @Column(length = 10, nullable = false)
  private CurriculumBranch branch;

  @Builder.Default private boolean published = false;

  @NotNull @Size(max = 40)
  private String name;

  @NotNull @Size(max = 20)
  private String number;

  @NotNull @Size(max = 200)
  private String description;

  @NotNull @Column(length = 30)
  private ModuleTopic topic;

  @Valid
  @NotEmpty
  @Column(columnDefinition = "json")
  @Convert(converter = ComponentListConverter.class)
  private List<Component> components;

  public boolean version(Module other) {
    return versionKey.equals(other.getVersionKey());
  }
}
