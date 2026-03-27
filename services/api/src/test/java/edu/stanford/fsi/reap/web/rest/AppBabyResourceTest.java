package edu.stanford.fsi.reap.web.rest;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.fsi.reap.dto.AddressWrapper;
import edu.stanford.fsi.reap.dto.AppCarerDTO;
import edu.stanford.fsi.reap.dto.AppCreateBabyDTO;
import edu.stanford.fsi.reap.dto.AppLessonDTO;
import edu.stanford.fsi.reap.dto.BabyDetailDTO;
import edu.stanford.fsi.reap.dto.BabyWrapper;
import edu.stanford.fsi.reap.dto.CloseAccountReasonWrapper;
import edu.stanford.fsi.reap.dto.RemarkWrapper;
import edu.stanford.fsi.reap.dto.VisitResultDTO;
import edu.stanford.fsi.reap.entity.Baby;
import edu.stanford.fsi.reap.entity.Carer;
import edu.stanford.fsi.reap.entity.Chw;
import edu.stanford.fsi.reap.entity.Curriculum;
import edu.stanford.fsi.reap.entity.enumerations.BabyStage;
import edu.stanford.fsi.reap.entity.enumerations.FamilyTies;
import edu.stanford.fsi.reap.entity.enumerations.Gender;
import edu.stanford.fsi.reap.repository.BabyRepository;
import edu.stanford.fsi.reap.repository.BabyUpdateInfoRepository;
import edu.stanford.fsi.reap.repository.CarerModifyRecordRepository;
import edu.stanford.fsi.reap.repository.CarerRepository;
import edu.stanford.fsi.reap.repository.VisitRepository;
import edu.stanford.fsi.reap.security.SecurityUtils;
import edu.stanford.fsi.reap.service.BabyService;
import edu.stanford.fsi.reap.service.GoogleMapService;
import edu.stanford.fsi.reap.service.LessonService;
import edu.stanford.fsi.reap.utils.DateRange;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
import org.zalando.problem.spring.web.autoconfigure.BasicExceptionHandling;

@AutoConfigureMockMvc
class AppBabyResourceTest {

  @InjectMocks private static AppBabyResource resource;
  private static BabyRepository babyRepository;
  private static LessonService lessonService;
  private static BabyService babyService;
  private static VisitRepository visitRepository;
  private static CarerRepository carerRepository;
  private static CarerModifyRecordRepository carerModifyRecordRepository;
  private static BabyUpdateInfoRepository babyUpdateInfoRepository;
  private static GoogleMapService googleMapService;
  private static ModelMapper modelMapper;
  private static MockMvc mockMvc;
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeAll
  public static void beforeAll() {
    babyRepository = mock(BabyRepository.class);
    lessonService = mock(LessonService.class);
    visitRepository = mock(VisitRepository.class);
    carerRepository = mock(CarerRepository.class);
    carerModifyRecordRepository = mock(CarerModifyRecordRepository.class);
    babyService = mock(BabyService.class);
    modelMapper = mock(ModelMapper.class);
    babyUpdateInfoRepository = mock(BabyUpdateInfoRepository.class);
    googleMapService = mock(GoogleMapService.class);
    when(babyRepository.findByIdAndChwIdAndDeletedFalse(11L, null))
        .thenReturn(Optional.ofNullable(Baby.builder().build()));
    resource =
        new AppBabyResource(
            lessonService,
            babyRepository,
            babyService,
            carerRepository,
            visitRepository,
            modelMapper,
            carerModifyRecordRepository,
            babyUpdateInfoRepository,
            googleMapService);
    mockMvc =
        MockMvcBuilders.standaloneSetup(resource)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setControllerAdvice(new BasicExceptionHandling())
            .build();
  }

  @Test
  void should_get_no_curriculum_message() throws Exception {
    when(babyRepository.findByIdAndChwIdAndDeletedFalseAndApprovedTrue(100L, null))
        .thenReturn(Optional.of(new Baby()));
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/babies/100/lesson"))
        .andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  void should_get_has_not_started_lesson_message() throws Exception {
    when(babyRepository.findByIdAndChwIdAndDeletedFalse(100L, null))
        .thenReturn(Optional.of(Baby.builder().curriculum(new Curriculum()).build()));
    when(visitRepository.findByBabyIdAndNotStarted(100L))
        .thenReturn(Collections.singletonList(mock(VisitResultDTO.class)));
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/babies/100/lesson"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.detail")
                .value("Visit cannot be scheduled since there is an unfinished visit."));
  }

  @Test
  @WithMockUser
  public void should_get_app_babies() throws Exception {
    when(babyRepository.findAppBabyByChwIdAndName(
            SecurityUtils.getUserId(),
            "admin",
            PageRequest.of(0, 20, Sort.by(new Sort.Order(Sort.Direction.DESC, "createdAt")))))
        .thenReturn(Page.empty());

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/babies")
                .param("name", "admin")
                .param("pageNumber", "1")
                .param("pageSize", "1"))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  public void should_get_bad_baby_visitDateRange_when_baby_no_curriculum() throws Exception {
    Baby baby = new Baby();
    Optional<Baby> optionalBaby = Optional.of(baby);
    when(babyRepository.findByIdAndChwIdAndDeletedFalse(2L, SecurityUtils.getUserId()))
        .thenReturn(optionalBaby);
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/babies/{id}/visit-date-range", 2)
                .param("id", String.valueOf(2L)))
        .andDo(print())
        .andExpect(status().is4xxClientError());
  }

  @Test
  @WithMockUser
  public void should_get_baby_visitDateRange_success() throws Exception {
    Baby baby = new Baby();
    Optional<Baby> optionalBaby = Optional.of(baby);
    baby.setCurriculum(new Curriculum());
    Optional<List<LocalDate>> optionalLocalDateList =
        Optional.of(Collections.singletonList(LocalDate.now()));

    when(babyRepository.findByIdAndChwIdAndDeletedFalse(2L, SecurityUtils.getUserId()))
        .thenReturn(optionalBaby);

    when(lessonService.visitDateRange(baby, LocalDate.now())).thenReturn(optionalLocalDateList);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/babies/{id}/visit-date-range", 2)
                .param("id", String.valueOf(2L)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()));
  }

  @Test
  @WithMockUser
  public void should_get_babiesAvailableForCreateVisit() throws Exception {
    String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/babies/available-for-visit")
                .param("visitDate", dateStr))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()));
  }

  @Test
  @WithMockUser
  public void should_get_appBabyVisits() throws Exception {
    ArrayList<VisitResultDTO> visitResultDTOS = new ArrayList<>();
    when(visitRepository.findByBabyIdAndStarted(2L)).thenReturn(visitResultDTOS);
    when(visitRepository.findByBabyIdAndNotStarted(2L)).thenReturn(visitResultDTOS);
    when(visitRepository.findCountByBabyIdAndStatusAndRemarkIsNull(2L)).thenReturn(2);
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/babies/{id}/visits", 2L))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfNoRemark").value(2));
  }

  @Test
  @WithMockUser
  public void should_get_availableLesson_param_400() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/babies/{id}/lesson", 2L))
        .andDo(print())
        .andExpect(status().is4xxClientError());
  }

  @Test
  @WithMockUser
  public void should_get_availableLesson_success() throws Exception {
    Baby baby = new Baby();
    baby.setCurriculum(new Curriculum());
    Optional<Baby> optionalBaby = Optional.of(baby);
    AppLessonDTO appLessonDTO = new AppLessonDTO();
    appLessonDTO.setName("testAppLessonDTO");
    Optional<AppLessonDTO> optionalAppLessonDTO = Optional.of(appLessonDTO);

    when(babyRepository.findByIdAndChwIdAndDeletedFalse(2L, SecurityUtils.getUserId()))
        .thenReturn(optionalBaby);

    when(lessonService.findAvailable(
            baby, DateRange.checkBaseline(LocalDate.now(), LocalDateTime.now())))
        .thenReturn(optionalAppLessonDTO);

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/babies/{id}/lesson", 2L))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("testAppLessonDTO"));
  }

  @Test
  @WithMockUser
  public void should_create_babyFromApp() throws Exception {
    AppCreateBabyDTO appCreateBaby = new AppCreateBabyDTO();
    appCreateBaby.setBaby(new Baby());
    appCreateBaby.setCarers(new ArrayList<>());
    Baby returnBaby = new Baby();
    returnBaby.setName("returnBaby");
    returnBaby.setStage(BabyStage.BORN);
    returnBaby.setBirthday(
        LocalDate.parse("2020-02-02", DateTimeFormatter.ofPattern("yyyy-MM-dd")));

    when(babyService.createFromApp(
            appCreateBaby, Chw.builder().id(SecurityUtils.getUserId()).build()))
        .thenReturn(returnBaby);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/babies")
                .content(objectMapper.writeValueAsString(appCreateBaby))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("returnBaby"));
  }

  @Test
  @WithMockUser
  public void should_create_babyCarer() throws Exception {
    Carer carer = new Carer();
    carer.setName("carer");
    carer.setPhone("17805202360");
    carer.setFamilyTies(FamilyTies.BROTHER);
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/babies/{id}/carers", 2L)
                .content(objectMapper.writeValueAsString(carer))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  public void should_update_error_babyCarer_when_no_carer() throws Exception {
    Carer carer = new Carer();
    carer.setName("carer");
    carer.setPhone("17805202360");
    carer.setId(2L);
    AppCarerDTO carerDTO = new AppCarerDTO();
    carerDTO.setName("carer");
    carerDTO.setPhone("17805202360");
    carerDTO.setFamilyTies(FamilyTies.BROTHER);

    when(modelMapper.map(carerDTO, Carer.class)).thenReturn(carer);

    when(carerRepository.findOneByBabyIdAndMasterIsTrue(2L)).thenReturn(Optional.of(carer));

    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/babies/{id}/carers/{carerId}", 2L, 2L)
                .content(objectMapper.writeValueAsString(carerDTO))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  public void should_update_babyCarer_success() throws Exception {
    Carer carer = new Carer();
    carer.setName("carer");
    carer.setPhone("17805202360");
    AppCarerDTO carerDTO = new AppCarerDTO();
    carerDTO.setName("carer");
    carerDTO.setPhone("17805202360");
    carerDTO.setFamilyTies(FamilyTies.BROTHER);

    when(modelMapper.map(carerDTO, Carer.class)).thenReturn(carer);
    carer.setMaster(true);
    when(carerRepository.findOneByBabyIdAndMasterIsTrue(2L)).thenReturn(Optional.of(carer));

    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/babies/{id}/carers/{carerId}", 2L, 2L)
                .content(objectMapper.writeValueAsString(carerDTO))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  public void should_delete_babyCarer() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.delete("/api/babies/{id}/carers/{carerId}", 2L, 2L))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  public void should_close_babyAccountFromApp() throws Exception {
    CloseAccountReasonWrapper wrapper = new CloseAccountReasonWrapper();
    wrapper.setReason("reason");

    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/babies/{id}/close", 2L)
                .content(objectMapper.writeValueAsString(wrapper))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  public void should_update_babyBasicInfo() throws Exception {
    BabyWrapper wrapper = new BabyWrapper();
    wrapper.setName("wrapper");
    wrapper.setGender(Gender.MALE);
    wrapper.setStage(BabyStage.BORN);

    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/babies/{id}", 2L)
                .content(objectMapper.writeValueAsString(wrapper))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  public void should_update_babyRemark() throws Exception {
    RemarkWrapper wrapper = new RemarkWrapper();
    wrapper.setRemark("remark");

    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/babies/{id}/remark", 2L)
                .content(objectMapper.writeValueAsString(wrapper))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  public void should_update_babyAddress() throws Exception {
    AddressWrapper wrapper = new AddressWrapper();
    wrapper.setArea("area");
    wrapper.setLocation("location");

    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/babies/{id}/address", 2L)
                .content(objectMapper.writeValueAsString(wrapper))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  public void should_get_appBaby() throws Exception {
    Baby baby = new Baby();
    baby.setName("baby");
    baby.setBirthday(LocalDate.parse("2020-02-02", DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    BabyDetailDTO babyDetailDTO = new BabyDetailDTO();

    when(babyRepository.findByIdAndChwIdAndDeletedFalse(2L, SecurityUtils.getUserId()))
        .thenReturn(Optional.of(baby));

    when(modelMapper.map(baby, BabyDetailDTO.class)).thenReturn(babyDetailDTO);

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/babies/{id}", 2L))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()));
  }

  @Test
  @WithMockUser
  public void should_get_appBabyCarers() throws Exception {

    when(carerRepository.findByBabyIdAndBabyChwIdOrderByMasterDesc(2L, SecurityUtils.getUserId()))
        .thenReturn(new ArrayList<>());

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/babies/{id}/carers", 2L))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()));
  }
}
