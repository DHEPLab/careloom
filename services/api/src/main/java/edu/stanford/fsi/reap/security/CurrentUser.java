package edu.stanford.fsi.reap.security;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * @author hookszhang
 */
@Getter
public class CurrentUser extends User {
  private final Long userId;
  private LocalDateTime issuedAt;
  private final Long projectId;

  public CurrentUser(
      String username,
      String password,
      Collection<? extends GrantedAuthority> grantedAuthorities,
      Long userId,
      Date issuedAt,
      Long projectId) {
    super(username, password, grantedAuthorities);
    this.userId = userId;
    this.projectId = projectId;
    if (issuedAt != null) {
      Instant instant = issuedAt.toInstant();
      ZoneId zoneId = ZoneId.systemDefault();
      this.issuedAt = instant.atZone(zoneId).toLocalDateTime();
    }
  }

  public boolean issuedBeforeModifiedPassword(LocalDateTime lastModifiedPasswordAt) {
    if (issuedAt == null || lastModifiedPasswordAt == null) {
      return false;
    }
    return issuedAt.isBefore(lastModifiedPasswordAt);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CurrentUser)) return false;
    if (!super.equals(o)) return false;

    CurrentUser that = (CurrentUser) o;

    if (!getUserId().equals(that.getUserId())) return false;
    return getIssuedAt().equals(that.getIssuedAt());
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + getUserId().hashCode();
    result = 31 * result + getIssuedAt().hashCode();
    return result;
  }
}
