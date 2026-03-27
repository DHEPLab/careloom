package edu.stanford.fsi.reap.web.rest;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.fsi.reap.dto.LoginDTO;
import edu.stanford.fsi.reap.jwt.TokenProvider;
import edu.stanford.fsi.reap.security.AuthoritiesConstants;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.problem.spring.web.autoconfigure.security.SpringSecurityExceptionHandling;

@AutoConfigureMockMvc
class AccountJWTResourceTest {

  @InjectMocks private static AccountJWTResource resource;
  private static MockMvc mockMvc;
  private static TokenProvider tokenProvider;
  private static AuthenticationManager authenticationManager;
  private static ObjectMapper objectMapper = new ObjectMapper();

  @BeforeAll
  public static void beforeAll() {
    authenticationManager = mock(AuthenticationManager.class);
    tokenProvider = mock(TokenProvider.class);
    resource = new AccountJWTResource(tokenProvider, authenticationManager);
    mockMvc =
        MockMvcBuilders.standaloneSetup(resource)
            // https://stackoverflow.com/questions/16669356/testing-spring-mvc-exceptionhandler-method-with-spring-mvc-test
            .setControllerAdvice(SpringSecurityExceptionHandling.class)
            .build();
  }

  @Test
  @WithMockUser
  public void should_app_signIn_param_400() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  public void should_get_bad_authority_when_chw_sign_in_as_app() throws Exception {
    LoginDTO loginDTO = new LoginDTO();
    loginDTO.setUsername("admin");
    loginDTO.setPassword("admin123");
    String loginDTOJsonStr = objectMapper.writeValueAsString(loginDTO);
    Authentication authentication = new UsernamePasswordAuthenticationToken("admin", "admin123");

    when(authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword())))
        .thenReturn(authentication);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/authenticate")
                .content(loginDTOJsonStr)
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isUnauthorized())
        .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Bad Authority"));
  }

  @Test
  @WithMockUser
  public void should_app_signIn_success() throws Exception {
    LoginDTO loginDTO = new LoginDTO();
    loginDTO.setUsername("admin");
    loginDTO.setPassword("admin123");
    String loginDTOJsonStr = objectMapper.writeValueAsString(loginDTO);
    List<SimpleGrantedAuthority> authorities =
        Collections.singletonList(new SimpleGrantedAuthority(AuthoritiesConstants.CHW));
    Authentication authentication =
        new UsernamePasswordAuthenticationToken("admin", "admin123", authorities);

    when(authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword())))
        .thenReturn(authentication);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/authenticate")
                .content(loginDTOJsonStr)
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()));
  }

  @Test
  @WithMockUser
  public void should_admin_signIn_param_400() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/admin/authenticate")
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  public void should_get_bad_authority_when_chw_sign_in_as_admin() throws Exception {
    String username = "chw";
    String password = "chw";
    LoginDTO dto = new LoginDTO();
    dto.setUsername(username);
    dto.setPassword(password);

    when(authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)))
        .thenReturn(
            new UsernamePasswordAuthenticationToken(
                username,
                password,
                Collections.singletonList(new SimpleGrantedAuthority(AuthoritiesConstants.CHW))));

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/admin/authenticate")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isUnauthorized())
        .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Bad Authority"));
  }

  @Test
  @WithMockUser
  public void should_admin_signIn_success() throws Exception {
    LoginDTO loginDTO = new LoginDTO();
    loginDTO.setUsername("admin");
    loginDTO.setPassword("admin123");
    String loginDTOJsonStr = objectMapper.writeValueAsString(loginDTO);
    Authentication authentication = new UsernamePasswordAuthenticationToken("admin", "admin123");

    when(authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword())))
        .thenReturn(authentication);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/admin/authenticate")
                .content(loginDTOJsonStr)
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()));
  }
}
