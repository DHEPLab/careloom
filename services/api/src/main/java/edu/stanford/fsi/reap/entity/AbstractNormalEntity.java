package edu.stanford.fsi.reap.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Access(AccessType.FIELD)
public abstract class AbstractNormalEntity implements Serializable {
  public static final String SKIP_DELETED_CLAUSE = "deleted = false";
  public static final String NOT_DELETED = "deleted = false or deleted = true";
  private static final long serialVersionUID = 1L;

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate private LocalDateTime lastModifiedAt;

  @CreatedBy
  @Column(updatable = false, length = 50)
  private String createdBy;

  @LastModifiedBy
  @Column(length = 50)
  private String lastModifiedBy;

  @JsonIgnore private boolean deleted = false;

  public String isDeletedString() {
    return deleted ? "注销" : "正常";
  }
}
