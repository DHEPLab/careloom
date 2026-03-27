package edu.stanford.fsi.reap.web.rest;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.fsi.reap.dto.RemarkWrapper;
import edu.stanford.fsi.reap.dto.UpdateVisitStatusWrapper;
import edu.stanford.fsi.reap.dto.VisitDetailDTO;
import edu.stanford.fsi.reap.entity.Baby;
import edu.stanford.fsi.reap.entity.ExportLesson;
import edu.stanford.fsi.reap.entity.Lesson;
import edu.stanford.fsi.reap.entity.Visit;
import edu.stanford.fsi.reap.entity.enumerations.VisitStatus;
import edu.stanford.fsi.reap.repository.*;
import edu.stanford.fsi.reap.security.SecurityUtils;
import edu.stanford.fsi.reap.service.LessonService;
import edu.stanford.fsi.reap.service.VisitReportService;
import edu.stanford.fsi.reap.service.VisitService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@AutoConfigureMockMvc
class VisitResourceTest {

  @InjectMocks private static VisitResource resource;
  private static MockMvc mockMvc;
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static VisitRepository repository;
  private static LessonService lessonService;
  private static VisitService visitService;
  private static BabyRepository babyRepository;
  private static QuestionnaireRepository questionnaireRepository;
  private static QuestionnaireRecordRepository questionnaireRecordRepository;
  private static VisitReportService visitReportService;
  private static SysErrorLogRepository sysErrorLogRepository;
  private static LessonRepository lessonRepository;
  private static BabyUpdateInfoRepository babyUpdateInfoRepository;

  @BeforeAll
  public static void beforeAll() {
    repository = mock(VisitRepository.class);
    lessonService = mock(LessonService.class);
    visitService = mock(VisitService.class);
    babyRepository = mock(BabyRepository.class);
    questionnaireRepository = mock(QuestionnaireRepository.class);
    questionnaireRecordRepository = mock(QuestionnaireRecordRepository.class);
    visitReportService = mock(VisitReportService.class);
    sysErrorLogRepository = mock(SysErrorLogRepository.class);
    lessonRepository = mock(LessonRepository.class);
    babyUpdateInfoRepository = mock(BabyUpdateInfoRepository.class);
    when(repository.findByIdAndChwIdOrBabyChwId(1L, null))
        .thenReturn(Optional.ofNullable(Visit.builder().status(VisitStatus.DONE).build()));
    when(repository.findByIdAndChwIdOrBabyChwId(2L, null))
        .thenReturn(Optional.ofNullable(Visit.builder().status(VisitStatus.EXPIRED).build()));
    resource =
        new VisitResource(
            lessonService,
            visitService,
            repository,
            babyRepository,
            questionnaireRepository,
            sysErrorLogRepository,
            lessonRepository,
            babyUpdateInfoRepository);
    mockMvc = MockMvcBuilders.standaloneSetup(resource).build();
  }

  @Test
  @WithMockUser
  public void should_create_visit_latter() throws Exception {
    Map<String, Object> map = new HashMap<>();
    map.put("babyId", 2L);
    map.put("lessonId", 2L);
    map.put("visitTime", "2020-10-10T23:12:12");

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/visits")
                .content(objectMapper.writeValueAsString(map))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  public void should_create_visit_notfoundbaby() throws Exception {
    Map<String, Object> map = new HashMap<>();
    map.put("babyId", 2L);
    map.put("lessonId", 2L);
    map.put("visitTime", "2020-10-10T20:12:12");

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/visits")
                .content(objectMapper.writeValueAsString(map))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().is4xxClientError());
  }

  @Test
  @WithMockUser
  public void should_create_visit_success() throws Exception {
    Map<String, Object> map = new HashMap<>();
    map.put("babyId", 3L);
    map.put("lessonId", 3L);
    map.put("visitTime", LocalDateTime.of(LocalDate.now(), LocalTime.MAX));

    when(babyRepository.findByIdAndChwIdAndDeletedFalseAndApprovedTrue(
            3L, SecurityUtils.getUserId()))
        .thenReturn(Optional.of(new Baby()));

    Baby baby = Baby.builder().id(3L).build();
    Lesson lesson = Lesson.builder().id(3L).build();
    when(babyRepository.findById(3L)).thenReturn(Optional.of(baby));
    when(lessonRepository.findById(3L)).thenReturn(Optional.of(lesson));
    when(lessonService.visitDateRange(baby, lesson, LocalDate.now())).thenReturn(Optional.empty());

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/visits")
                .content(objectMapper.writeValueAsString(map))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print());
  }

  @Test
  @WithMockUser
  public void should_get_visitDateRange_success() throws Exception {
    Visit visit = new Visit();
    visit.setBaby(new Baby());
    visit.setLesson(new ExportLesson());

    when(repository.findById(3L)).thenReturn(Optional.of(visit));

    when(lessonService.visitDateRange(visit.getBaby(), visit.getLesson(), LocalDate.now()))
        .thenReturn(Optional.of(Collections.singletonList(LocalDate.now())));

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/visits/{id}/date-range", 3L))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()));
  }

  @Test
  @WithMockUser
  public void should_change_visitTime_notstart() throws Exception {
    Map<String, Object> map = new HashMap<>();
    map.put("visitTime", "2020-10-10T23:12:12");
    Visit visit = new Visit();
    visit.setStatus(VisitStatus.EXPIRED);

    when(repository.findByIdAndChwIdOrBabyChwId(2L, SecurityUtils.getUserId()))
        .thenReturn(Optional.of(visit));

    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/visits/{id}", 2L)
                .content(objectMapper.writeValueAsString(map))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  public void should_change_visitTime_success() throws Exception {
    Map<String, Object> map = new HashMap<>();
    map.put("visitTime", "2020-10-10T23:12:12");
    Visit visit = new Visit();
    visit.setStatus(VisitStatus.NOT_STARTED);

    when(repository.findByIdAndChwIdOrBabyChwId(2L, SecurityUtils.getUserId()))
        .thenReturn(Optional.of(visit));

    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/visits/{id}", 2L)
                .content(objectMapper.writeValueAsString(map))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @Disabled
  @WithMockUser
  public void should_update_visitStatus_readonly() throws Exception {
    UpdateVisitStatusWrapper wrapper = new UpdateVisitStatusWrapper();
    wrapper.setVisitStatus(VisitStatus.EXPIRED);
    Visit visit = new Visit();
    visit.setStatus(VisitStatus.EXPIRED);
    when(repository.findByIdAndChwIdOrBabyChwId(2L, SecurityUtils.getUserId()))
        .thenReturn(Optional.of(visit));

    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/visits/{id}/status", 2L)
                .content(objectMapper.writeValueAsString(wrapper))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  public void should_update_visitStatus_success() throws Exception {
    UpdateVisitStatusWrapper wrapper = new UpdateVisitStatusWrapper();
    wrapper.setVisitStatus(VisitStatus.EXPIRED);
    Visit visit = new Visit();
    visit.setStatus(VisitStatus.NOT_STARTED);
    when(repository.findByIdAndChwIdOrBabyChwId(2L, SecurityUtils.getUserId()))
        .thenReturn(Optional.of(visit));

    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/visits/{id}/status", 2L)
                .content(objectMapper.writeValueAsString(wrapper))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  public void should_add_remark_isdown() throws Exception {
    RemarkWrapper wrapper = new RemarkWrapper();
    wrapper.setRemark("remark");
    Visit visit = new Visit();
    visit.setStatus(VisitStatus.DONE);
    when(repository.findByIdAndChwIdOrBabyChwId(2L, SecurityUtils.getUserId()))
        .thenReturn(Optional.of(visit));

    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/visits/{id}/remark", 2L)
                .content(objectMapper.writeValueAsString(wrapper))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  public void should_add_remark_success() throws Exception {
    RemarkWrapper wrapper = new RemarkWrapper();
    wrapper.setRemark("remark");
    Visit visit = new Visit();
    visit.setStatus(VisitStatus.NOT_STARTED);
    when(repository.findByIdAndChwIdOrBabyChwId(2L, SecurityUtils.getUserId()))
        .thenReturn(Optional.of(visit));

    mockMvc
        .perform(
            MockMvcRequestBuilders.put("/api/visits/{id}/remark", 2L)
                .content(objectMapper.writeValueAsString(wrapper))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  public void should_get_calendarMarkedDates() throws Exception {
    when(visitService.markedDates(SecurityUtils.getUserId())).thenReturn(new ArrayList<>());

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/visits/marked-dates", 2L))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()));
  }

  @Test
  @WithMockUser
  public void should_get_visits() throws Exception {
    LocalDate date = LocalDate.now();
    when(repository.findByDateAndChwId(
            date.getYear(), date.getMonthValue(), date.getDayOfMonth(), SecurityUtils.getUserId()))
        .thenReturn(new ArrayList<>());

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/visits")
                .param("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()));
  }

  @Test
  @WithMockUser
  public void should_get_visit_param_400() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/visits/{id}", 2L))
        .andDo(print())
        .andExpect(status().is4xxClientError());
  }

  @Test
  @WithMockUser
  public void should_get_visit_success() throws Exception {
    when(visitService.findById(2L, SecurityUtils.getUserId()))
        .thenReturn(Optional.of(new VisitDetailDTO()));

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/visits/{id}", 2L))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()));
  }

  @Test
  @WithMockUser
  public void should_get_nextVisits_success() throws Exception {
    when(visitService.findNext(SecurityUtils.getUserId()))
        .thenReturn(Optional.of(new VisitDetailDTO()));

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/visits/next"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()));
  }
}
