package edu.stanford.fsi.reap.utils;

import edu.stanford.fsi.reap.security.DomainUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomUserSecurityContextFactory
    implements WithSecurityContextFactory<WithMockCustomUser> {

  @Autowired private DomainUserDetailsService domainUserDetailsService;

  @Override
  public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    UserDetails details = domainUserDetailsService.loadUserByUsername(customUser.username());
    Authentication auth =
        new UsernamePasswordAuthenticationToken(details, "password", details.getAuthorities());
    context.setAuthentication(auth);
    return context;
  }
}
