package edu.stanford.fsi.reap.entity;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
// The closed baby can be seen on the admin portal, but not on the app.
// @Where(clause = AbstractAuditingEntity.SKIP_DELETED_CLAUSE)
@SQLDelete(sql = "UPDATE account_operation_record SET deleted = true WHERE id = ?")
public class AccountOperationRecord extends AbstractAuditingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long accountId;

  private String accountType;

  private LocalDateTime closeTime;

  private LocalDateTime revertTime;

  private Boolean revert;
}
