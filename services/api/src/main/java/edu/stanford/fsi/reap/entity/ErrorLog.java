package edu.stanford.fsi.reap.entity;

import edu.stanford.fsi.reap.entity.enumerations.ErrorLogType;
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
@Where(clause = AbstractNormalEntity.SKIP_DELETED_CLAUSE)
@SQLDelete(sql = "UPDATE error_log SET deleted = true WHERE id = ?")
public class ErrorLog extends AbstractNormalEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne private User user;

  @NotNull @Enumerated(EnumType.STRING)
  private ErrorLogType type;

  private Long typeId;

  @NotNull @Size(max = 500)
  private String msg;
}
