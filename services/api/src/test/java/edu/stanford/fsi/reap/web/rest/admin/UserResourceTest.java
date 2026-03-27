package edu.stanford.fsi.reap.web.rest.admin;

import static edu.stanford.fsi.reap.security.AuthoritiesConstants.CHW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.fsi.reap.dto.AssignBabyDTO;
import edu.stanford.fsi.reap.dto.UserDTO;
import edu.stanford.fsi.reap.entity.CommunityHouseWorker;
import edu.stanford.fsi.reap.entity.User;
import edu.stanford.fsi.reap.entity.enumerations.Gender;
import edu.stanford.fsi.reap.repository.*;
import edu.stanford.fsi.reap.service.BabyService;
import edu.stanford.fsi.reap.service.UserService;
import edu.stanford.fsi.reap.web.rest.errors.BadRequestAlertException;
import edu.stanford.fsi.reap.web.rest.errors.LoginAlreadyUsedException;
import java.util.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.zalando.problem.spring.web.autoconfigure.security.SpringSecurityExceptionHandling;

@AutoConfigureMockMvc
class UserResourceTest {

  @InjectMocks private static MockMvc mockMvc;
  private static UserResource resource;
  private static BabyRepository babyRepository;
  private static UserRepository userRepository;
  private static UserService userService;
  private static final String url = "/admin/users";
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static ChwUserRepository chwUserRepository;
  private static VisitRepository visitRepository;

  @BeforeAll
  public static void beforeAll() {
    BabyService babyService = mock(BabyService.class);
    babyRepository = mock(BabyRepository.class);
    userService = mock(UserService.class);
    userRepository = mock(UserRepository.class);
    chwUserRepository = mock(ChwUserRepository.class);
    visitRepository = mock(VisitRepository.class);
    CommunityHouseWorkerRepository communityHouseWorkerRepository =
        mock(CommunityHouseWorkerRepository.class);
    ModelMapper modelMapper = new ModelMapper();
    resource =
        new UserResource(
            babyService,
            babyRepository,
            userService,
            userRepository,
            communityHouseWorkerRepository,
            modelMapper,
            chwUserRepository,
            visitRepository,
            null);

    mockMvc =
        MockMvcBuilders.standaloneSetup(resource)
            .setControllerAdvice(SpringSecurityExceptionHandling.class)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setViewResolvers(
                new ViewResolver() {
                  @Override
                  public View resolveViewName(String viewName, Locale locale) throws Exception {
                    return new MappingJackson2JsonView();
                  }
                })
            .build();
  }

  @Test
  public void should_get_babies_by_chw_id() {
    AssignBabyDTO babyDTO =
        new AssignBabyDTO() {
          @Override
          public Long getId() {
            return 1L;
          }

          @Override
          public String getName() {
            return "Baby 1";
          }

          @Override
          public String getIdentity() {
            return null;
          }

          @Override
          public Gender getGender() {
            return null;
          }

          @Override
          public String getArea() {
            return null;
          }

          @Override
          public String getMasterCarerName() {
            return null;
          }

          @Override
          public String getMasterCarerPhone() {
            return null;
          }
        };
    List<AssignBabyDTO> expected = Collections.singletonList(babyDTO);
    when(babyRepository.findAssignBabyByChwId(2L)).thenReturn(expected);
    List<AssignBabyDTO> babies = resource.getBabiesByChwId(2L);
    assertEquals(expected, babies);
    assertEquals(new ArrayList<>(), resource.getBabiesByChwId(3L));
  }

  @Test
  @WithMockUser
  void createUserByRolesExecp() throws Exception {
    UserDTO userDTO = new UserDTO();
    userDTO.setUsername("test");
    userDTO.setPassword("test1234");
    userDTO.setRealName("test");
    userDTO.setPhone("13217499804");
    userDTO.setRole("ROLE_CHW1");

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(url)
                .content(objectMapper.writeValueAsString(userDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser
  void createUserByLoginExecp() throws Exception {
    UserDTO userDTO = new UserDTO();
    userDTO.setUsername("test");
    userDTO.setPassword("test1234");
    userDTO.setRealName("test");
    userDTO.setPhone("13217499804");
    userDTO.setRole("ROLE_CHW");

    when(userRepository.findOneByUsername(userDTO.getUsername())).thenReturn(Optional.empty());
    when(userRepository.findCountByUsernameAndDeletedTrue(userDTO.getUsername()))
        .thenAnswer(
            i -> {
              return 1L;
            });

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(url)
                .content(objectMapper.writeValueAsString(userDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertTrue(result.getResolvedException() instanceof LoginAlreadyUsedException));
  }

  @Test
  @WithMockUser
  void createUserByBadRequestExecp() throws Exception {
    UserDTO userDTO = new UserDTO();
    userDTO.setUsername("test");
    userDTO.setPassword("test1234");
    userDTO.setRealName("test");
    userDTO.setPhone("13217499804");
    userDTO.setRole("ROLE_CHW");

    when(userRepository.findOneByUsername(userDTO.getUsername())).thenReturn(Optional.empty());
    when(userRepository.findCountByUsernameAndDeletedTrue(userDTO.getUsername()))
        .thenAnswer(
            i -> {
              return 0L;
            });

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(url)
                .content(objectMapper.writeValueAsString(userDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertTrue(result.getResolvedException() instanceof BadRequestAlertException));
  }

  @Test
  @WithMockUser
  void createUserBySetSupervisor() throws Exception {
    UserDTO userDTO = new UserDTO();
    userDTO.setUsername("test");
    userDTO.setPassword("test1234");
    userDTO.setRealName("test");
    userDTO.setPhone("13217499804");
    userDTO.setRole("ROLE_CHW");
    userDTO.setChw(
        CommunityHouseWorker.builder()
            .identity("01234567890123456789012345678901234567890123456789")
            .build());

    when(userRepository.findOneByUsername(userDTO.getUsername())).thenReturn(Optional.empty());
    when(userRepository.findCountByUsernameAndDeletedTrue(userDTO.getUsername()))
        .thenAnswer(
            i -> {
              return 0L;
            });
    when(userService.save(any())).thenReturn(User.builder().id(1L).build());

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(url)
                .content(objectMapper.writeValueAsString(userDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().is(201));
  }

  @Test
  @WithMockUser
  void updateUserByAccessExcep() throws Exception {
    UserResource.ChangeUserVM changeUserVM = new UserResource.ChangeUserVM();
    changeUserVM.setRealName("Test");
    changeUserVM.setPhone("13217499804");
    changeUserVM.setChw(
        CommunityHouseWorker.builder()
            .identity("01234567890123456789012345678901234567890123456789")
            .supervisor(User.builder().id(1L).build())
            .build());

    when(userRepository.findById(1L))
        .thenReturn(
            Optional.of(
                User.builder()
                    .role("ROLE_CHW")
                    .chw(
                        CommunityHouseWorker.builder()
                            .identity("01234567890123456789012345678901234567890123456789")
                            .supervisor(null)
                            .build())
                    .build()));

    mockMvc
        .perform(
            MockMvcRequestBuilders.put(url + "/{id}", 1L)
                .content(objectMapper.writeValueAsString(changeUserVM))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser
  void changeUserPassword() throws Exception {
    UserResource.PasswordVM passwordVM = new UserResource.PasswordVM();
    passwordVM.setPassword("Test123");

    mockMvc
        .perform(
            MockMvcRequestBuilders.put(url + "/{id}/password", 1L)
                .content(objectMapper.writeValueAsString(passwordVM))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isOk());

    verify(userService, times(1)).changePassword(1L, passwordVM.getPassword());
  }

  @Test
  @WithMockUser
  void getBabiesByChwId() throws Exception {
    when(babyRepository.findAssignBabyByChwId(1L)).thenReturn(new ArrayList<>());
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(url + "/chw/{id}/babies", 1L).characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void assignBabyToChw() throws Exception {
    Long[] babyIds = new Long[] {1L, 2L};
    when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(User.builder().build()));
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(url + "/chw/{id}/babies", 1L)
                .content(objectMapper.writeValueAsString(babyIds))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void deleteChw() throws Exception {
    UserResource.TakeOverWrapper takeOverWrapper = new UserResource.TakeOverWrapper();
    takeOverWrapper.setUserId(1L);

    mockMvc
        .perform(
            MockMvcRequestBuilders.delete(url + "/chw/{id}", 1L)
                .content(objectMapper.writeValueAsString(takeOverWrapper))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void releaseChwSupervisor() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.delete(url + "/chw/{id}/supervisor", 1L)
                .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void getNotAssignedChwList() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(url + "/supervisor/not_assigned/chw")
                .param("search", "test")
                .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void deleteSupervisor() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.delete(url + "/supervisor/{id}", 1L).characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void getSupervisorChwList() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(url + "/supervisor/{id}/chw", 1L).characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void assignChwToSupervisorByNoLog() throws Exception {
    Long[] chwIds = new Long[] {1L, 2L};

    when(userRepository.findById(1L))
        .thenReturn(Optional.ofNullable(User.builder().role("ROLE_SUPERVISOR").build()));

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(url + "/supervisor/{id}/chw", 1L)
                .content(objectMapper.writeValueAsString(chwIds))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void assignChwToSupervisorByLog() throws Exception {
    Long[] chwIds = new Long[] {1L, 2L};

    when(userRepository.findById(1L))
        .thenReturn(Optional.ofNullable(User.builder().role(CHW).build()));

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(url + "/supervisor/{id}/chw", 1L)
                .content(objectMapper.writeValueAsString(chwIds))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void getUser() throws Exception {
    User user = User.builder().role(CHW).chw(CommunityHouseWorker.builder().build()).build();

    when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));

    mockMvc
        .perform(MockMvcRequestBuilders.get(url + "/{id}", 1L).characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  void getUsers() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get(url).param("role", "roleTest"))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void getNotAssignedBabies() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(url + "/chw/not_assigned/babies").param("search", "test"))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void getChwList() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get(url + "/chw").param("search", "test"))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void getSupervisorList() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get(url + "/supervisor"))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void getAdminList() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get(url + "/admin"))
        .andDo(print())
        .andExpect(status().isOk());
  }
}
