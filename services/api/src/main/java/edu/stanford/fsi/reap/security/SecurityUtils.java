package edu.stanford.fsi.reap.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/** Utility class for Spring Security. */
public final class SecurityUtils {

  public static final Long DEFAULT_PROJECT_ID = -1L;

  /**
   * Get the login of the current user.
   *
   * @return the login of the current user
   */
  public static String getUsername() {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    Authentication authentication = securityContext.getAuthentication();
    CurrentUser user = getUser(authentication);
    if (user != null) {
      return user.getUsername();
    }
    return "ANONYMOUS";
  }

  public static boolean hasAuthorityAdmin() {
    return hasAuthority(AuthoritiesConstants.ADMIN);
  }

  public static boolean hasAuthority(String role) {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    Authentication authentication = securityContext.getAuthentication();
    CurrentUser user = getUser(authentication);
    if (user == null) return false;
    return user.getAuthorities().contains((GrantedAuthority) () -> role);
  }

  public static Long getUserId() {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    Authentication authentication = securityContext.getAuthentication();
    return getUserId(authentication);
  }

  public static Long getProjectId() {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    Authentication authentication = securityContext.getAuthentication();
    return getProjectId(authentication);
  }

  public static Long getUserId(Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof CurrentUser) {
      CurrentUser user = (CurrentUser) authentication.getPrincipal();
      return user.getUserId();
    }
    return null;
  }

  public static Long getProjectId(Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof CurrentUser) {
      CurrentUser user = (CurrentUser) authentication.getPrincipal();
      return user.getProjectId();
    }
    return DEFAULT_PROJECT_ID;
  }

  public static CurrentUser getUser(Authentication authentication) {
    if (authentication != null && authentication.getPrincipal() instanceof CurrentUser) {
      return (CurrentUser) authentication.getPrincipal();
    }
    return null;
  }
}
