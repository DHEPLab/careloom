package edu.stanford.fsi.reap.web.rest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.fsi.reap.dto.PasswordWrapper;
import edu.stanford.fsi.reap.dto.ProfileWrapper;
import edu.stanford.fsi.reap.entity.User;
import edu.stanford.fsi.reap.repository.UserRepository;
import edu.stanford.fsi.reap.service.UserService;
import edu.stanford.fsi.reap.web.rest.errors.BadRequestAlertException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@AutoConfigureMockMvc
class AccountResourceTest {

  @InjectMocks private static AccountResource resource;
  private static UserService userService;
  private static MockMvc mockMvc;
  private static PasswordEncoder passwordEncoder;
  private static ObjectMapper objectMapper = new ObjectMapper();

  @BeforeAll
  public static void beforeAll() {
    userService = mock(UserService.class);
    UserRepository userRepository = mock(UserRepository.class);
    passwordEncoder = new BCryptPasswordEncoder();
    resource = new AccountResource(userService, userRepository, passwordEncoder);
    mockMvc = MockMvcBuilders.standaloneSetup(resource).build();
  }

  @Test
  @WithMockUser
  public void should_get_profile_get() throws Exception {
    User user = new User();
    user.setUsername("admin");
    when(userService.getCurrentLogin()).thenReturn(user);
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/account/profile"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("admin"));
  }

  @Test
  @WithMockUser
  public void should_get_profile_put_param_400() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/account/profile")
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  public void should_get_profile_put_success() throws Exception {
    ProfileWrapper profileWrapper = new ProfileWrapper();
    profileWrapper.setRealName("admin");
    profileWrapper.setPhone("17805202360");
    String profileWrapperJsonStr = objectMapper.writeValueAsString(profileWrapper);
    User user = new User();
    user.setUsername("admin");

    when(userService.getCurrentUser()).thenReturn(Optional.of(user));

    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/account/profile")
                .content(profileWrapperJsonStr)
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  public void should_change_accountPassword_param_400() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/account/password")
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  public void should_change_accountPassword_error_password() throws Exception {
    PasswordWrapper passwordWrapper = new PasswordWrapper();
    passwordWrapper.setOldPassword("admin123");
    passwordWrapper.setPassword("admin123");
    User user = new User();
    user.setUsername("admin");
    user.setPassword(passwordEncoder.encode("admin12"));

    when(userService.getCurrentLogin()).thenReturn(user);

    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/account/password")
                .content(objectMapper.writeValueAsString(passwordWrapper))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertTrue(result.getResolvedException() instanceof BadRequestAlertException));
  }

  @Test
  @WithMockUser
  public void should_change_accountPassword_success() throws Exception {
    PasswordWrapper passwordWrapper = new PasswordWrapper();
    passwordWrapper.setOldPassword("admin123");
    passwordWrapper.setPassword("admin123");
    User user = new User();
    user.setUsername("admin");
    user.setPassword(passwordEncoder.encode("admin123"));

    when(userService.getCurrentLogin()).thenReturn(user);

    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/account/password")
                .content(objectMapper.writeValueAsString(passwordWrapper))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());
  }
}
