package edu.stanford.fsi.reap.security;

/** Constants for Spring Security authorities. */
public final class AuthoritiesConstants {

  public static final String SUPER_ADMIN = "ROLE_SUPER_ADMIN";

  public static final String ADMIN = "ROLE_ADMIN";

  // CHW Supervisor
  public static final String SUPERVISOR = "ROLE_SUPERVISOR";

  // Community House Worker
  public static final String CHW = "ROLE_CHW";

  public static final String ANONYMOUS = "ROLE_ANONYMOUS";

  private AuthoritiesConstants() {}
}
