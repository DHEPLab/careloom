package edu.stanford.fsi.reap.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.stanford.fsi.reap.security.AuthoritiesConstants;
import java.time.LocalDateTime;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

/** ClassName: Chw Description: author: huangwenxing 2021-4-30 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SQLDelete(sql = "UPDATE user SET deleted = true WHERE id = ?")
@Table(name = "user")
public class Chw extends AbstractAuditingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull @Size(min = 1, max = 50)
  @Column(length = 50, unique = true, nullable = false)
  private String username;

  @JsonIgnore
  @NotNull @Size(min = 60, max = 60)
  @Column(length = 60, name = "password_hash")
  private String password;

  @Size(max = 50)
  @Column(length = 50)
  private String realName;

  @Column(length = 20)
  @NotNull private String phone;

  @Column(length = 50, nullable = false)
  private String role;

  private LocalDateTime lastModifiedPasswordAt;

  @OneToOne private CommunityHouseWorker chw;

  public boolean roleChw() {
    return AuthoritiesConstants.CHW.equals(this.role);
  }

  public boolean roleSupervisor() {
    return AuthoritiesConstants.SUPERVISOR.equals(this.role);
  }
}
