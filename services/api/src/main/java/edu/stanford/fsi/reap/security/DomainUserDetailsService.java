package edu.stanford.fsi.reap.security;

import edu.stanford.fsi.reap.config.Constants;
import edu.stanford.fsi.reap.entity.Project;
import edu.stanford.fsi.reap.entity.User;
import edu.stanford.fsi.reap.repository.ProjectRepository;
import edu.stanford.fsi.reap.repository.UserRepository;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Authenticate a user from the database. */
@Component("userDetailsService")
public class DomainUserDetailsService implements UserDetailsService {

  private final Logger log = LoggerFactory.getLogger(DomainUserDetailsService.class);

  private final UserRepository userRepository;

  private final ProjectRepository projectRepository;

  public DomainUserDetailsService(
      UserRepository userRepository, ProjectRepository projectRepository) {
    this.userRepository = userRepository;
    this.projectRepository = projectRepository;
  }

  @Override
  @Transactional
  public UserDetails loadUserByUsername(final String username) {
    log.debug("Authenticating {}", username);
    Optional<User> userByUsernameFromDatabase = userRepository.findOneByUsername(username);
    return userByUsernameFromDatabase
        .map(user -> createSpringSecurityUser(username, user))
        .orElseThrow(
            () ->
                new UsernameNotFoundException(
                    "User " + username + " was not found in the " + "database"));
  }

  private CurrentUser createSpringSecurityUser(String username, User user) {
    Optional<Project> project = projectRepository.findById(user.getProjectId());
    if (project.get() == null || project.get().getStatus() == Constants.PROJECT_INVALID) {
      throw new DisabledException("the project has been closed");
    }
    List<GrantedAuthority> grantedAuthorities =
        Arrays.stream(new String[] {user.getRole()})
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    return new CurrentUser(
        username,
        user.getPassword(),
        grantedAuthorities,
        user.getId(),
        new Date(),
        user.getProjectId());
  }
}
