package edu.stanford.fsi.reap.config;

import edu.stanford.fsi.reap.entity.Project;
import edu.stanford.fsi.reap.entity.User;
import edu.stanford.fsi.reap.repository.ProjectRepository;
import edu.stanford.fsi.reap.repository.UserRepository;
import edu.stanford.fsi.reap.security.AuthoritiesConstants;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Explicit, create-only setup for a new self-hosted administrator. Never resets an account. */
@Component
@Profile("docker")
@ConditionalOnProperty(name = "application.bootstrap.enabled", havingValue = "true")
public class LocalAdminBootstrap implements ApplicationRunner {
  private final UserRepository users;
  private final ProjectRepository projects;
  private final PasswordEncoder encoder;
  private final String username;
  private final String password;

  public LocalAdminBootstrap(
      UserRepository users,
      ProjectRepository projects,
      PasswordEncoder encoder,
      @Value("${application.bootstrap.username:}") String username,
      @Value("${application.bootstrap.password:}") String password) {
    this.users = users;
    this.projects = projects;
    this.encoder = encoder;
    this.username = username;
    this.password = password;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (!username.matches("[A-Za-z0-9_-]{1,50}")) {
      throw new IllegalArgumentException(
          "Set a bootstrap username using 1-50 letters, digits, _ or -");
    }
    Optional<User> existing = users.findOneByUsername(username);
    if (existing.isPresent()) {
      User user = existing.get();
      boolean matches =
          AuthoritiesConstants.SUPER_ADMIN.equals(user.getRole())
              && !user.isDeleted()
              && encoder.matches(password, user.getPassword())
              && projects
                  .findById(user.getProjectId())
                  .map(p -> p.getStatus() == Constants.PROJECT_VALID)
                  .orElse(false);
      if (matches) {
        return; // Idempotent restart only after checking the configured credentials and role.
      }
      throw new IllegalStateException(
          "Bootstrap account collision; disable bootstrap. Existing account was not changed.");
    }
    if (password.length() < 12
        || password.length() > Constants.PASSWORD_MAX_LENGTH
        || "not-configured".equals(password)) {
      throw new IllegalArgumentException("Set a bootstrap password containing 12-64 characters");
    }
    Project project =
        projects.findAll().stream()
            .filter(p -> Constants.DEFAULT_PROJECT.equals(p.getCode()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Default project migration is missing"));
    User user = new User();
    user.setUsername(username);
    user.setPassword(encoder.encode(password));
    user.setRealName("Local administrator");
    user.setPhone("");
    user.setRole(AuthoritiesConstants.SUPER_ADMIN);
    user.setProjectId(project.getId());
    users.save(user);
  }
}
