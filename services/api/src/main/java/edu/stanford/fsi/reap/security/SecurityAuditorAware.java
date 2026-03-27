package edu.stanford.fsi.reap.security;

import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityAuditorAware implements AuditorAware<String> {

  @Override
  public Optional<String> getCurrentAuditor() {
    if (null != SecurityContextHolder.getContext().getAuthentication()) {
      CurrentUser user =
          (CurrentUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      return Optional.of(user.getUsername());
    }
    return Optional.of("ANONYMOUS");
  }
}
