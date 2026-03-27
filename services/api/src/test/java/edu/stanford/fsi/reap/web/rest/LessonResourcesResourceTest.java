package edu.stanford.fsi.reap.web.rest;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.stanford.fsi.reap.dto.AppOfflineLessonDTO;
import edu.stanford.fsi.reap.dto.Updates;
import edu.stanford.fsi.reap.entity.Lesson;
import edu.stanford.fsi.reap.entity.Module;
import edu.stanford.fsi.reap.entity.enumerations.CurriculumBranch;
import edu.stanford.fsi.reap.repository.LessonRepository;
import edu.stanford.fsi.reap.repository.ModuleRepository;
import edu.stanford.fsi.reap.service.LessonService;
import edu.stanford.fsi.reap.service.ModuleService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.TimeZone;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@AutoConfigureMockMvc
class LessonResourcesResourceTest {

  private static final ModuleRepository moduleRepository = mock(ModuleRepository.class);
  private static final LessonRepository lessonRepository = mock(LessonRepository.class);
  private static final LessonService lessonService = mock(LessonService.class);
  @InjectMocks private static LessonResourcesResource lessonResourcesResource;
  private static MockMvc mockMvc;

  @BeforeAll
  public static void beforeAll() {

    TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("UTC")));

    lessonResourcesResource =
        new LessonResourcesResource(
            mock(ModuleService.class), moduleRepository, lessonRepository, lessonService);
    mockMvc = MockMvcBuilders.standaloneSetup(lessonResourcesResource).build();
  }

  @Test
  public void should_is_the_latest() {
    LocalDateTime moduleLastModified = LocalDateTime.of(2020, 1, 2, 10, 0, 0, 0);
    LocalDateTime lessonLastModified = LocalDateTime.of(2020, 1, 2, 10, 1, 0, 0);

    Module module = new Module();
    module.setLastModifiedAt(moduleLastModified);
    Lesson lesson = new Lesson();
    lesson.setLastModifiedAt(lessonLastModified);

    when(moduleRepository.findFirstByBranchAndPublishedTrueOrderByLastModifiedAtDesc(any()))
        .thenReturn(Optional.of(module));

    when(lessonRepository
            .findFirstByCurriculumBranchAndCurriculumPublishedTrueOrderByLastModifiedAtDesc(any()))
        .thenReturn(Optional.of(lesson));

    Updates updates = lessonResourcesResource.checkForUpdates(null);
    assertFalse(updates.isUpdated(), "Should be updated when lastUpdateAt is null");

    String lastUpdateAtString = "2020-01-02T07:21:58Z";
    ZonedDateTime lastUpdateAt = ZonedDateTime.parse(lastUpdateAtString);

    updates = lessonResourcesResource.checkForUpdates(lastUpdateAt);
    assertTrue(
        updates.isUpdated(),
        "Should not be updated when lastUpdateAt is after the latest modifications");
  }

  @Test
  public void should_have_update() {
    Module module = new Module();
    module.setLastModifiedAt(LocalDateTime.of(2024, 9, 11, 8, 0));
    Lesson lesson = new Lesson();
    lesson.setLastModifiedAt(LocalDateTime.of(2024, 9, 11, 8, 0));

    when(moduleRepository.findFirstByBranchAndPublishedTrueOrderByLastModifiedAtDesc(any()))
        .thenReturn(Optional.of(module));

    when(lessonRepository
            .findFirstByCurriculumBranchAndCurriculumPublishedTrueOrderByLastModifiedAtDesc(any()))
        .thenReturn(Optional.of(lesson));

    ZonedDateTime lastUpdateAt = ZonedDateTime.parse("2024-09-11T07:21:58Z");
    Updates updates = lessonResourcesResource.checkForUpdates(lastUpdateAt);
    System.out.println("Is updated: " + updates.isUpdated());

    assertTrue(updates.isUpdated());
  }

  @Test
  @WithMockUser
  public void should_check_forUpdates_isTheLatest() throws Exception {
    String lastUpdateAtString = "2020-10-10T12:12:12Z";
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/resources/check-for-updates")
                .param("lastUpdateAt", lastUpdateAtString))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.updated").value(false));
  }

  @Test
  @WithMockUser
  public void should_check_forUpdates_lastLesson_haveUpdate() throws Exception {
    Lesson lesson = new Lesson();
    lesson.setLastModifiedAt(LocalDateTime.now());
    when(lessonRepository
            .findFirstByCurriculumBranchAndCurriculumPublishedTrueOrderByLastModifiedAtDesc(
                CurriculumBranch.MASTER))
        .thenReturn(Optional.of(lesson));

    String lastUpdateAtString =
        ZonedDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/resources/check-for-updates")
                .param("lastUpdateAt", lastUpdateAtString))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.updated").value(true));
  }

  @Test
  @WithMockUser
  public void should_check_forUpdates_lastModule_haveUpdate() throws Exception {
    Lesson lesson = new Lesson();
    lesson.setLastModifiedAt(LocalDateTime.now());

    when(lessonRepository
            .findFirstByCurriculumBranchAndCurriculumPublishedTrueOrderByLastModifiedAtDesc(
                CurriculumBranch.MASTER))
        .thenReturn(Optional.of(lesson));

    Module module = new Module();
    module.setLastModifiedAt(LocalDateTime.now());

    when(moduleRepository.findFirstByBranchAndPublishedTrueOrderByLastModifiedAtDesc(
            CurriculumBranch.MASTER))
        .thenReturn(Optional.of(module));

    String lastUpdateAtString =
        ZonedDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/resources/check-for-updates")
                .param("lastUpdateAt", lastUpdateAtString))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.updated").value(true));
  }

  @Test
  @WithMockUser
  public void should_downloadModules() throws Exception {
    when(moduleRepository.findByBranchAndPublishedTrue(CurriculumBranch.MASTER))
        .thenReturn(new ArrayList<Module>());

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/resources/modules"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()));
  }

  @Test
  @WithMockUser
  public void should_downloadLessons() throws Exception {
    ArrayList<Lesson> lessons = new ArrayList<>();

    when(lessonRepository.findByCurriculumBranchAndCurriculumPublishedTrue(CurriculumBranch.MASTER))
        .thenReturn(lessons);

    ArrayList<Module> modules = new ArrayList<>();

    when(moduleRepository.findByBranchAndPublishedTrue(CurriculumBranch.MASTER))
        .thenReturn(modules);

    when(lessonService.appOfflineLessons(lessons, modules))
        .thenReturn(new ArrayList<AppOfflineLessonDTO>());

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/resources/lessons"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", notNullValue()));
  }
}
