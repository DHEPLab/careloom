package edu.stanford.fsi.reap.entity;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 未经过滤的用户 User 实体使用软删除，在 User 实体上使用了 @Where 注解实现过滤已删除的记录。由于 @Where 注解无法关闭 这里拷贝一个实体，不使用 @Where
 * 注解，用来查询已经删除的记录
 *
 * @author huey
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "user")
public class UnfilteredUser extends AbstractAuditingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String username;

  private String realName;

  private String phone;

  private String role;

  private LocalDateTime lastModifiedPasswordAt;

  @OneToOne private CommunityHouseWorker chw;
}
