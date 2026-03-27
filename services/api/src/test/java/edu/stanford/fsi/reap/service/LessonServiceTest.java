package edu.stanford.fsi.reap.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.stanford.fsi.reap.dto.AppOfflineLessonDTO;
import edu.stanford.fsi.reap.dto.VisitResultDTO;
import edu.stanford.fsi.reap.entity.*;
import edu.stanford.fsi.reap.entity.Module;
import edu.stanford.fsi.reap.entity.enumerations.BabyStage;
import edu.stanford.fsi.reap.entity.enumerations.VisitStatus;
import edu.stanford.fsi.reap.pojo.Domain;
import edu.stanford.fsi.reap.repository.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LessonServiceTest {

  private static LessonService service;

  @BeforeAll
  public static void beforeAll() {
    LessonRepository lessonRepository = mock(LessonRepository.class);
    LessonScheduleRepository lessonScheduleRepository = mock(LessonScheduleRepository.class);
    ModuleRepository moduleRepository = mock(ModuleRepository.class);
    VisitRepository visitRepository = mock(VisitRepository.class);
    QuestionnaireRepository questionnaireRepository = mock(QuestionnaireRepository.class);

    when(lessonScheduleRepository.findByCurriculumIdAndStage(1L, BabyStage.UNBORN))
        .thenReturn(mockEDCStageSchedules());
    when(lessonRepository.findByCurriculumIdAndStage(1L, BabyStage.UNBORN))
        .thenReturn(mockEDCStageLessons());
    when(lessonScheduleRepository.findByCurriculumIdAndStage(1L, BabyStage.BORN))
        .thenReturn(mockBirthStageSchedules());
    when(lessonRepository.findByCurriculumIdAndStage(1L, BabyStage.BORN))
        .thenReturn(mockBirthStageLessons());
    when(visitRepository.findByLessonIdAndBabyId(11L, 1L))
        .thenReturn(Collections.singletonList(Visit.builder().status(VisitStatus.DONE).build()));
    when(visitRepository.findByLessonIdAndBabyId(11L, 2L))
        .thenReturn(Collections.singletonList(Visit.builder().status(VisitStatus.EXPIRED).build()));
    when(visitRepository.findByBabyIdAndNotStarted(12L))
        .thenReturn(
            Collections.singletonList(
                new VisitResultDTO() {
                  @Override
                  public Long getId() {
                    return 12L;
                  }

                  @Override
                  public String getBabyName() {
                    return null;
                  }

                  @Override
                  public Boolean getBabyApproved() {
                    return true;
                  }

                  @Override
                  public String getLessonName() {
                    return null;
                  }

                  @Override
                  public LocalDateTime getVisitTime() {
                    return null;
                  }

                  @Override
                  public VisitStatus getStatus() {
                    return null;
                  }

                  @Override
                  public String getRemark() {
                    return null;
                  }
                }));

    service =
        new LessonService(
            lessonRepository,
            lessonScheduleRepository,
            moduleRepository,
            visitRepository,
            questionnaireRepository);
  }

  private static List<Lesson> mockEDCStageLessons() {
    return Arrays.asList(
        Lesson.builder()
            .id(1L)
            .number("E1")
            .startOfApplicableDays(61)
            .endOfApplicableDays(100)
            .build(),
        Lesson.builder()
            .id(2L)
            .number("E2")
            .startOfApplicableDays(100)
            .endOfApplicableDays(120)
            .build());
  }

  private static List<LessonSchedule> mockEDCStageSchedules() {
    return Collections.singletonList(
        LessonSchedule.builder()
            .stage(BabyStage.UNBORN)
            .startOfApplicableDays(61)
            .endOfApplicableDays(150)
            .lessons(Arrays.asList(new Domain("1", "E1"), new Domain("2", "E2")))
            .build());
  }

  private static List<LessonSchedule> mockBirthStageSchedules() {
    return Collections.singletonList(
        LessonSchedule.builder()
            .stage(BabyStage.BORN)
            .startOfApplicableDays(60)
            .endOfApplicableDays(90)
            .lessons(Arrays.asList(new Domain("11", "B1"), new Domain("22", "B2")))
            .build());
  }

  private static List<Lesson> mockBirthStageLessons() {
    return Arrays.asList(
        Lesson.builder().id(11L).number("B1").build(),
        Lesson.builder().id(22L).number("B2").build());
  }

  @Test
  public void should_match_lesson_for_edc_stage_baby() {
    Baby baby =
        Baby.builder()
            .stage(BabyStage.UNBORN)
            .edc(LocalDate.of(2020, 10, 1))
            .curriculum(Curriculum.builder().id(1L).build())
            .build();
    LocalDate baseline = LocalDate.of(2020, 5, 1);
    Optional<Lesson> lesson = service.match(baby, baseline);
    assertTrue(lesson.isPresent());
    assertEquals(1L, lesson.get().getId());

    baseline = LocalDate.of(2020, 4, 1);
    lesson = service.match(baby, baseline);
    assertTrue(lesson.isPresent());
    assertEquals(1L, lesson.get().getId());

    baseline = LocalDate.of(2020, 3, 1);
    lesson = service.match(baby, baseline);
    assertTrue(lesson.isPresent());

    baseline = LocalDate.of(2020, 2, 1);
    lesson = service.match(baby, baseline);
    assertFalse(lesson.isPresent());
  }

  @Test
  public void should_match_lesson_for_birth_stage_baby() {
    Baby baby =
        Baby.builder()
            .stage(BabyStage.BORN)
            .birthday(LocalDate.of(2020, 3, 1))
            .curriculum(Curriculum.builder().id(1L).build())
            .build();
    LocalDate baseline = LocalDate.of(2020, 5, 1);
    Optional<Lesson> lesson = service.match(baby, baseline);
    assertTrue(lesson.isPresent());
    assertEquals(11L, lesson.get().getId());

    baseline = LocalDate.of(2020, 1, 1);
    lesson = service.match(baby, baseline);
    assertFalse(lesson.isPresent());
  }

  @Test
  public void should_match_lesson_and_skip_have_been_done() {
    Baby baby =
        Baby.builder()
            .id(1L)
            .stage(BabyStage.BORN)
            .birthday(LocalDate.of(2020, 3, 1))
            .curriculum(Curriculum.builder().id(1L).build())
            .build();
    LocalDate baseline = LocalDate.of(2020, 5, 1);
    Optional<Lesson> lesson = service.match(baby, baseline);
    assertTrue(lesson.isPresent());
    assertEquals(22L, lesson.get().getId());
  }

  @Test
  public void should_match_lesson_and_continue_expired() {
    Baby baby =
        Baby.builder()
            .id(2L)
            .stage(BabyStage.BORN)
            .birthday(LocalDate.of(2020, 3, 1))
            .curriculum(Curriculum.builder().id(1L).build())
            .build();
    LocalDate baseline = LocalDate.of(2020, 5, 1);
    Optional<Lesson> lesson = service.match(baby, baseline);
    assertTrue(lesson.isPresent());
    assertEquals(11L, lesson.get().getId());
  }

  @Test
  public void should_return_visit_date_range() {
    Baby baby =
        Baby.builder()
            .stage(BabyStage.UNBORN)
            .edc(LocalDate.of(2020, 11, 1))
            .curriculum(Curriculum.builder().id(1L).build())
            .build();
    LocalDate baseline = LocalDate.of(2020, 5, 1);
    Optional<List<LocalDate>> localDates = service.visitDateRange(baby, baseline);
    assertTrue(localDates.isPresent());
    assertArrayEquals(
        new LocalDate[] {LocalDate.of(2020, 5, 1), LocalDate.of(2020, 5, 5)},
        localDates.get().toArray());
  }

  @Test
  public void should_return_app_offline_lessons() {
    Lesson lesson =
        Lesson.builder()
            .id(10L)
            .name("L1")
            .description("L11")
            .modules(Arrays.asList(new Domain("11", null), new Domain("12", null)))
            .build();
    lesson.setProjectId(-1L);
    List<Lesson> lessons = Collections.singletonList(lesson);
    List<Module> modules =
        Arrays.asList(
            Module.builder().id(11L).number("M11").name("MN11").build(),
            Module.builder().id(12L).number("M12").name("MN13").build());
    List<AppOfflineLessonDTO> offlineLessons = service.appOfflineLessons(lessons, modules);

    assertArrayEquals(
        Collections.singletonList(
                AppOfflineLessonDTO.builder()
                    .id(10L)
                    .name("L1")
                    .description("L11")
                    .modules(
                        Arrays.asList(
                            new AppOfflineLessonDTO.BasicModule(11L, "M11", "MN11"),
                            new AppOfflineLessonDTO.BasicModule(12L, "M12", "MN13")))
                    .build())
            .toArray(),
        offlineLessons.toArray());
  }

  @Test
  public void should_no_match_when_baby_have_not_start_visit() {
    Baby baby = Baby.builder().id(12L).build();
    Optional<Lesson> lesson = service.match(baby, LocalDate.now());
    assertFalse(lesson.isPresent());
  }
}
