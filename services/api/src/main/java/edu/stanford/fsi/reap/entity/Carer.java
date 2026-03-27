package edu.stanford.fsi.reap.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.stanford.fsi.reap.entity.enumerations.FamilyTies;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Where(clause = AbstractAuditingEntity.SKIP_DELETED_CLAUSE)
@SQLDelete(sql = "UPDATE carer SET deleted = true WHERE id = ?")
@Proxy(lazy = false)
public class Carer extends AbstractAuditingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull @Size(min = 1, max = 50)
  private String name;

  @NotNull @Size(max = 20)
  private String phone;

  @Size(max = 20)
  private String otherContact;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private FamilyTies familyTies;

  /** A baby can only have one master carer */
  @Builder.Default
  @Column(name = "master_carer", nullable = false)
  private boolean master = false;

  @ManyToOne(fetch = FetchType.LAZY)
  @JsonIgnore
  private Baby baby;

  @Override
  public String toString() {
    return "Carer{"
        + "id="
        + id
        + ", name='"
        + name
        + '\''
        + ", phone='"
        + phone
        + '\''
        + ", otherContact='"
        + otherContact
        + '\''
        + ", familyTies="
        + familyTies
        + ", master="
        + master
        + '}';
  }
}
