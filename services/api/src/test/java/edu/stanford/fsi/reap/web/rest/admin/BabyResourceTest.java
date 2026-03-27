package edu.stanford.fsi.reap.web.rest.admin;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.fsi.reap.dto.AdminBabyVisitDTO;
import edu.stanford.fsi.reap.entity.Baby;
import edu.stanford.fsi.reap.entity.Carer;
import edu.stanford.fsi.reap.entity.enumerations.BabyStage;
import edu.stanford.fsi.reap.entity.enumerations.Gender;
import edu.stanford.fsi.reap.entity.enumerations.VisitStatus;
import edu.stanford.fsi.reap.repository.*;
import edu.stanford.fsi.reap.security.SecurityUtils;
import edu.stanford.fsi.reap.service.BabyModifyRecordService;
import edu.stanford.fsi.reap.service.BabyService;
import edu.stanford.fsi.reap.service.ExcelService;
import edu.stanford.fsi.reap.service.GoogleMapService;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

@AutoConfigureMockMvc
class BabyResourceTest {

  @InjectMocks private static BabyResource babyResource;
  private static MockMvc mockMvc;
  private static ObjectMapper objectMapper;
  static BabyRepository repository;
  static BabyService service;
  static CarerRepository carerRepository;
  static VisitRepository visitRepository;
  private static final String url = "/admin/babies";

  static ExcelService excelService;

  @BeforeAll
  public static void beforeAll() {
    carerRepository = mock(CarerRepository.class);
    repository = mock(BabyRepository.class);
    service = mock(BabyService.class);
    ModelMapper modelMapper = new ModelMapper();
    excelService = mock(ExcelService.class);
    visitRepository = mock(VisitRepository.class);
    BabyModifyRecordRepository babyModifyRecordRepository = mock(BabyModifyRecordRepository.class);
    BabyModifyRecordService babyModifyRecordService = mock(BabyModifyRecordService.class);
    GoogleMapService googleMapService = mock(GoogleMapService.class);

    babyResource =
        new BabyResource(
            carerRepository,
            repository,
            service,
            modelMapper,
            visitRepository,
            excelService,
            babyModifyRecordRepository,
            babyModifyRecordService,
            googleMapService);
    mockMvc =
        MockMvcBuilders.standaloneSetup(babyResource)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setViewResolvers(
                new ViewResolver() {
                  @Override
                  public View resolveViewName(String viewName, Locale locale) {
                    return new MappingJackson2JsonView();
                  }
                })
            .build();

    objectMapper = new ObjectMapper();
  }

  @Test
  @WithMockUser
  void createBaby() throws Exception {
    Baby baby =
        Baby.builder()
            .name("test")
            .gender(Gender.MALE)
            .stage(BabyStage.BORN)
            .area("test")
            .location("test")
            .build();

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/admin/babies")
                .content(objectMapper.writeValueAsString(baby))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();
  }

  @Test
  @WithMockUser
  void changeBabyChw() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/admin/babies/{id}/chw/{userId}", 12341234L, 12341234123L))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void updateBady() throws Exception {
    Baby baby =
        Baby.builder()
            .id(1L)
            .name("test1")
            .gender(Gender.MALE)
            .stage(BabyStage.BORN)
            .area("上海")
            .location("静安")
            .build();
    // when(babyLocationHandler.confirmBabyLocation(baby.getArea(),baby.getLocation())).thenReturn("91.126,93.234");
    when(repository.findById(1L)).thenReturn(java.util.Optional.ofNullable(baby));
    assert baby != null;
    when(repository.save(baby)).thenReturn(baby);

    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/admin/babies/{id}", 1L)
                .content(objectMapper.writeValueAsString(baby))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(baby.getName()));
  }

  @Test
  @WithMockUser
  void approveBaby() throws Exception {
    BabyResource.IdentityWrapper wrapper = new BabyResource.IdentityWrapper();
    wrapper.setIdentity("test");

    Baby baby =
        Baby.builder()
            .id(1L)
            .name("test1")
            .gender(Gender.MALE)
            .stage(BabyStage.BORN)
            .area("上海")
            .location("静安")
            .build();

    when(repository.findById(1L)).thenReturn(java.util.Optional.of(baby));

    mockMvc
        .perform(
            MockMvcRequestBuilders.put(url + "/{id}/approve", 1L)
                .content(objectMapper.writeValueAsString(wrapper))
                .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void getBaby() throws Exception {
    Baby baby = new Baby();
    baby.setId(1L);
    baby.setName("test1");
    baby.setGender(Gender.MALE);
    baby.setStage(BabyStage.BORN);
    baby.setArea("上海");
    baby.setLocation("静安");
    baby.setEdc(LocalDate.MAX);
    baby.setBirthday(LocalDate.MIN);
    baby.setProjectId(1L);

    when(repository.findById(1L)).thenReturn(java.util.Optional.ofNullable(baby));

    mockMvc
        .perform(MockMvcRequestBuilders.get(url + "/{id}", 1L))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(baby.getName()));
  }

  @Test
  @WithMockUser
  void getApprovedBabies() throws Exception {

    when(repository.findBySearchAndApprovedTrueOrderBy(
            "test",
            -1L,
            PageRequest.of(0, 30, Sort.by(new Sort.Order(Sort.Direction.DESC, "createdAt")))))
        .thenReturn(Page.empty());

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(url + "/approved")
                .param("search", "test")
                .param("size", "30")
                .param("page", "0"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void getUnreviewedBabies() throws Exception {

    when(repository.findBySearchAndApprovedFalse(
            "test",
            -1L,
            PageRequest.of(0, 30, Sort.by(new Sort.Order(Sort.Direction.ASC, "lastModifiedAt")))))
        .thenReturn(Page.empty());
    when(repository.findBySearchAndSupervisorIdAndApprovedFalse(
            "test",
            SecurityUtils.getUserId(),
            -1L,
            PageRequest.of(0, 30, Sort.by(new Sort.Order(Sort.Direction.ASC, "lastModifiedAt")))))
        .thenReturn(Page.empty());

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(url + "/unreviewed")
                .param("search", "test")
                .param("size", "30")
                .param("page", "0"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void getBabyCarers() throws Exception {
    Baby baby =
        Baby.builder()
            .id(1L)
            .name("test1")
            .gender(Gender.MALE)
            .stage(BabyStage.BORN)
            .area("上海")
            .location("静安")
            .edc(LocalDate.MAX)
            .birthday(LocalDate.MIN)
            .build();

    Carer carer = Carer.builder().baby(baby).id(2L).name("test").phone("13217499804").build();

    List<Carer> rep = Collections.singletonList(carer);

    when(carerRepository.findByBabyIdOrderByMasterDesc(1L)).thenReturn(rep);

    mockMvc
        .perform(MockMvcRequestBuilders.get(url + "/{id}/carers", 1L))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();
  }

  @Test
  @WithMockUser
  void getBabyVisits() throws Exception {

    List<AdminBabyVisitDTO> rep = new ArrayList<>();

    when(visitRepository.findByBabyId(1L)).thenReturn(rep);

    mockMvc
        .perform(MockMvcRequestBuilders.get(url + "/{id}/visits", 1L))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();
  }

  @Test
  @WithMockUser
  void closeBabyAccount() throws Exception {
    Baby baby =
        Baby.builder()
            .id(1L)
            .name("test1")
            .gender(Gender.MALE)
            .stage(BabyStage.BORN)
            .area("上海")
            .location("静安")
            .edc(LocalDate.MAX)
            .birthday(LocalDate.MIN)
            .build();

    when(repository.findById(1L)).thenReturn(Optional.ofNullable(baby));
    assert baby != null;
    when(visitRepository.deleteByBabyIdAndStatus(baby.getId(), VisitStatus.NOT_STARTED))
        .thenReturn(1L);
    when(repository.save(baby)).thenReturn(baby);

    mockMvc
        .perform(MockMvcRequestBuilders.delete(url + "/{id}?reason=1", 1L))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();
  }

  @Test
  @WithMockUser
  void releaseBabyChw() throws Exception {
    Baby baby =
        Baby.builder()
            .id(1L)
            .name("test1")
            .gender(Gender.MALE)
            .stage(BabyStage.BORN)
            .area("上海")
            .location("静安")
            .edc(LocalDate.MAX)
            .birthday(LocalDate.MIN)
            .build();

    assert baby != null;
    when(repository.findById(1L)).thenReturn(Optional.of(baby));
    when(visitRepository.deleteByBabyIdAndStatus(baby.getId(), VisitStatus.NOT_STARTED))
        .thenReturn(1L);
    when(repository.save(baby)).thenReturn(baby);

    mockMvc
        .perform(MockMvcRequestBuilders.delete(url + "/{id}/chw", 1L))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();
  }

  @Test
  @WithMockUser
  void releaseBabyCurriculum() throws Exception {
    Baby baby =
        Baby.builder()
            .id(1L)
            .name("test1")
            .gender(Gender.MALE)
            .stage(BabyStage.BORN)
            .area("上海")
            .location("静安")
            .edc(LocalDate.MAX)
            .birthday(LocalDate.MIN)
            .build();

    when(repository.findById(1L)).thenReturn(Optional.ofNullable(baby));
    assert baby != null;
    when(visitRepository.deleteByBabyIdAndStatus(baby.getId(), VisitStatus.NOT_STARTED))
        .thenReturn(1L);
    when(repository.save(baby)).thenReturn(baby);

    mockMvc
        .perform(MockMvcRequestBuilders.delete(url + "/{id}/curriculum", 1L))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();
  }
}
