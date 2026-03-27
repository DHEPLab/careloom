package edu.stanford.fsi.reap.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import lombok.Data;

@Data
@MappedSuperclass
@Access(AccessType.FIELD)
public class AbstractHistoryEntity {
  public static final String SKIP_DELETED_CLAUSE = "deleted = false";
  public static final String NOT_DELETED = "deleted = false or deleted = true";
  private static final long serialVersionUID = 1L;

  @Column(updatable = false, name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "last_modified_at")
  private LocalDateTime lastModifiedAt;

  @Column(updatable = false, length = 50, name = "created_by")
  private String createdBy;

  @Column(length = 50, name = "last_modified_by")
  private String lastModifiedBy;

  @JsonIgnore private boolean deleted = false;

  public String isDeletedString() {
    return deleted ? "注销" : "正常";
  }
}
