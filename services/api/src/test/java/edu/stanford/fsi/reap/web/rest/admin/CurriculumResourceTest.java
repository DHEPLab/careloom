package edu.stanford.fsi.reap.web.rest.admin;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.fsi.reap.dto.CurriculumDTO;
import edu.stanford.fsi.reap.dto.CurriculumResultDTO;
import edu.stanford.fsi.reap.entity.Baby;
import edu.stanford.fsi.reap.entity.Curriculum;
import edu.stanford.fsi.reap.entity.Lesson;
import edu.stanford.fsi.reap.entity.LessonSchedule;
import edu.stanford.fsi.reap.entity.enumerations.BabyStage;
import edu.stanford.fsi.reap.pojo.Domain;
import edu.stanford.fsi.reap.repository.BabyRepository;
import edu.stanford.fsi.reap.repository.CurriculumRepository;
import edu.stanford.fsi.reap.service.BabyService;
import edu.stanford.fsi.reap.service.CurriculumService;
import java.util.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

@AutoConfigureMockMvc
class CurriculumResourceTest {

  @InjectMocks private static MockMvc mockMvc;
  private static final String url = "/admin/curriculums";
  private static ObjectMapper objectMapper;
  private static CurriculumService service;
  private static CurriculumRepository repository;
  private static BabyService babyService;
  private static BabyRepository babyRepository;

  @BeforeAll
  public static void beforeAll() {
    service = mock(CurriculumService.class);
    repository = mock(CurriculumRepository.class);
    babyRepository = mock(BabyRepository.class);
    babyService = mock(BabyService.class);

    CurriculumResource curriculumResource =
        new CurriculumResource(service, repository, babyRepository, babyService);
    mockMvc =
        MockMvcBuilders.standaloneSetup(curriculumResource)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setViewResolvers(
                new ViewResolver() {
                  @Override
                  public View resolveViewName(String viewName, Locale locale) throws Exception {
                    return new MappingJackson2JsonView();
                  }
                })
            .build();
    objectMapper = new ObjectMapper();
  }

  @Test
  @WithMockUser
  void publishCurriculum() throws Exception {
    Domain modules = new Domain();
    Lesson lesson =
        Lesson.builder()
            .number("test")
            .name("test")
            .description("test")
            .stage(BabyStage.UNBORN)
            .startOfApplicableDays(1)
            .endOfApplicableDays(10)
            .modules(Collections.singletonList(modules))
            .build();
    LessonSchedule lessonSchedule =
        LessonSchedule.builder()
            .name("test")
            .stage(BabyStage.UNBORN)
            .startOfApplicableDays(1)
            .endOfApplicableDays(10)
            .lessons(Collections.singletonList(modules))
            .build();
    CurriculumDTO curriculumDTO =
        CurriculumDTO.builder()
            .name("test")
            .description("test")
            .sessions(Collections.singletonList(lesson))
            .schedules(Collections.singletonList(lessonSchedule))
            .build();

    CurriculumResultDTO curriculumResultDTO = new CurriculumResultDTO();
    when(service.publish(curriculumDTO)).thenReturn(Optional.of(curriculumResultDTO));

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(url)
                .content(objectMapper.writeValueAsString(curriculumDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void saveCurriculumDraft() throws Exception {
    Domain modules = new Domain();
    Lesson lesson =
        Lesson.builder()
            .number("test")
            .name("test")
            .description("test")
            .stage(BabyStage.UNBORN)
            .startOfApplicableDays(1)
            .endOfApplicableDays(10)
            .modules(Collections.singletonList(modules))
            .build();
    LessonSchedule lessonSchedule =
        LessonSchedule.builder()
            .name("test")
            .stage(BabyStage.UNBORN)
            .startOfApplicableDays(1)
            .endOfApplicableDays(10)
            .lessons(Collections.singletonList(modules))
            .build();
    CurriculumDTO curriculumDTO =
        CurriculumDTO.builder()
            .name("test")
            .description("test")
            .sessions(Collections.singletonList(lesson))
            .schedules(Collections.singletonList(lessonSchedule))
            .build();

    CurriculumResultDTO curriculumResultDTO = new CurriculumResultDTO();
    when(service.draft(curriculumDTO)).thenReturn(Optional.of(curriculumResultDTO));

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(url + "/draft")
                .content(objectMapper.writeValueAsString(curriculumDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void getCurriculums() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get(url).param("search", "test"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void getCurriculum() throws Exception {
    CurriculumResultDTO curriculumResultDTO = new CurriculumResultDTO();

    when(service.findById(1L)).thenReturn(Optional.of(curriculumResultDTO));
    when(repository.findFirstBySourceId(1L))
        .thenReturn(Optional.ofNullable(Curriculum.builder().build()));

    mockMvc
        .perform(MockMvcRequestBuilders.get(url + "/{id}", 1L))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void deleteCurriculum() throws Exception {
    Curriculum curriculum = Curriculum.builder().build();
    when(repository.findById(1L)).thenReturn(Optional.ofNullable(curriculum));
    mockMvc
        .perform(MockMvcRequestBuilders.delete(url + "/{id}", 1L))
        .andDo(print())
        .andExpect(status().isOk());
    assert curriculum != null;
    verify(service, times(1)).delete(curriculum);
  }

  @Test
  @WithMockUser
  void getBabiesByCurriculumId() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get(url + "/{id}/babies", 1L))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void getCurriculumNotAssignedBabies() throws Exception {

    PageRequest paginacao = PageRequest.of(1, 10);
    List<Baby> centrosDeCustos = Arrays.asList(new Baby(), new Baby());
    Page<Baby> pagedResponse = new PageImpl<>(centrosDeCustos, paginacao, centrosDeCustos.size());

    when(babyRepository.findByCurriculumIdIsNotAndSearchAndOrderByIdDesc(
            eq(1L), eq("test"), anyLong(), any()))
        .thenReturn(pagedResponse);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(url + "/{id}/not_assigned_babies", 1L)
                .param("search", "test"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void assignCurriculumToBabies() throws Exception {
    Long[] longs = new Long[] {2L, 3L};

    Curriculum curriculum = Curriculum.builder().build();
    when(repository.findById(1L)).thenReturn(Optional.ofNullable(curriculum));

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(url + "/{id}/babies", 1L)
                .content(objectMapper.writeValueAsString(longs))
                .characterEncoding("UTF-8")
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());

    verify(babyService, times(1)).assignCurriculum(curriculum, longs);
  }
}
