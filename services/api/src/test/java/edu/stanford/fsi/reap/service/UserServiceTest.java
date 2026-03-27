package edu.stanford.fsi.reap.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.stanford.fsi.reap.entity.CommunityHouseWorker;
import edu.stanford.fsi.reap.entity.User;
import edu.stanford.fsi.reap.repository.CommunityHouseWorkerRepository;
import edu.stanford.fsi.reap.repository.UserRepository;
import edu.stanford.fsi.reap.security.AuthoritiesConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserServiceTest {

  private static UserService userService;
  private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @BeforeAll
  public static void beforeAll() {
    UserRepository userRepository = mock(UserRepository.class);
    CommunityHouseWorkerRepository chwRepository = mock(CommunityHouseWorkerRepository.class);
    when(userRepository.save(any()))
        .then(
            args -> {
              User user = args.getArgument(0);
              user.setId(1L);
              return user;
            });
    userService =
        new UserService(
            userRepository,
            chwRepository,
            passwordEncoder,
            mock(BabyService.class),
            mock(TagService.class));
  }

  @Test
  public void should_save_user() {
    String password = "111111";
    User vm =
        User.builder()
            .username("junit")
            .password(password)
            .realName("Junit Test")
            .role(AuthoritiesConstants.CHW)
            .phone("15839929999")
            .chw(new CommunityHouseWorker())
            .build();
    User user = userService.save(vm);

    assertEquals(user.getUsername(), vm.getUsername());
    assertEquals(user.getRealName(), vm.getRealName());
    assertEquals(user.getRole(), vm.getRole());
    assertEquals(user.getPhone(), vm.getPhone());
    assertTrue(passwordEncoder.matches(password, user.getPassword()));
  }
}
