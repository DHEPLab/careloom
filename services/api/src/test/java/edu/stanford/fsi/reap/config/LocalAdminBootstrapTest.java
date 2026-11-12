package edu.stanford.fsi.reap.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import edu.stanford.fsi.reap.entity.Project;
import edu.stanford.fsi.reap.entity.User;
import edu.stanford.fsi.reap.repository.ProjectRepository;
import edu.stanford.fsi.reap.repository.UserRepository;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

class LocalAdminBootstrapTest {
  @Test
  void isDisabledByDefaultInDockerProfile() {
    new ApplicationContextRunner()
        .withUserConfiguration(LocalAdminBootstrap.class)
        .withPropertyValues("spring.profiles.active=docker")
        .run(context -> assertFalse(context.containsBean("localAdminBootstrap")));
  }

  @Test
  void matchingExistingCredentialsAllowRestartWithoutWrites() {
    UserRepository users = mock(UserRepository.class);
    ProjectRepository projects = mock(ProjectRepository.class);
    PasswordEncoder encoder = mock(PasswordEncoder.class);
    User existing = new User();
    existing.setRole("ROLE_SUPER_ADMIN");
    existing.setPassword("existing-hash");
    existing.setProjectId(1L);
    when(users.findOneByUsername("evaluation_admin")).thenReturn(Optional.of(existing));
    when(encoder.matches("synthetic-password", "existing-hash")).thenReturn(true);
    when(projects.findById(1L)).thenReturn(Optional.of(Project.builder().id(1L).status(1).build()));
    new LocalAdminBootstrap(users, projects, encoder, "evaluation_admin", "synthetic-password")
        .run(null);
    verify(users, never()).save(any());
  }

  @Test
  void createsOnlyAnExplicitlyConfiguredNewAccount() {
    UserRepository users = mock(UserRepository.class);
    ProjectRepository projects = mock(ProjectRepository.class);
    PasswordEncoder encoder = mock(PasswordEncoder.class);
    when(users.findOneByUsername("evaluation_admin")).thenReturn(Optional.empty());
    when(projects.findAll())
        .thenReturn(
            Collections.singletonList(Project.builder().id(1L).code("health_future").build()));
    when(encoder.encode("synthetic-password")).thenReturn("encoded-synthetic-value");
    new LocalAdminBootstrap(users, projects, encoder, "evaluation_admin", "synthetic-password")
        .run(null);
    ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
    verify(users).save(saved.capture());
    assertEquals("encoded-synthetic-value", saved.getValue().getPassword());
    assertEquals("ROLE_SUPER_ADMIN", saved.getValue().getRole());
    assertEquals(1L, saved.getValue().getProjectId());
  }

  @Test
  void refusesExistingNameWithoutChangingPasswordOrRole() {
    UserRepository users = mock(UserRepository.class);
    ProjectRepository projects = mock(ProjectRepository.class);
    PasswordEncoder encoder = mock(PasswordEncoder.class);
    when(users.findOneByUsername("evaluation_admin")).thenReturn(Optional.of(new User()));
    assertThrows(
        IllegalStateException.class,
        () ->
            new LocalAdminBootstrap(
                    users, projects, encoder, "evaluation_admin", "different-password")
                .run(null));
    verify(users, never()).save(any());
    verifyNoInteractions(projects);
  }

  @Test
  void rejectsMissingPasswordBeforeCreatingAnAccount() {
    UserRepository users = mock(UserRepository.class);
    ProjectRepository projects = mock(ProjectRepository.class);
    PasswordEncoder encoder = mock(PasswordEncoder.class);
    when(users.findOneByUsername("evaluation_admin")).thenReturn(Optional.empty());
    assertThrows(
        IllegalArgumentException.class,
        () -> new LocalAdminBootstrap(users, projects, encoder, "evaluation_admin", "").run(null));
    verify(users, never()).save(any());
  }
}
