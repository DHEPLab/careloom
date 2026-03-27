package edu.stanford.fsi.reap.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.stanford.fsi.reap.dto.CurriculumDTO;
import edu.stanford.fsi.reap.dto.CurriculumResultDTO;
import edu.stanford.fsi.reap.entity.Curriculum;
import edu.stanford.fsi.reap.entity.Lesson;
import edu.stanford.fsi.reap.entity.LessonSchedule;
import edu.stanford.fsi.reap.entity.enumerations.CurriculumBranch;
import edu.stanford.fsi.reap.pojo.Domain;
import edu.stanford.fsi.reap.repository.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CurriculumServiceTest {

  private static CurriculumService service;
  private static final CurriculumRepository repository = mock(CurriculumRepository.class);

  private static final Long MASTER_BRANCH_UNPUBLISHED_CURRICULUM_ID = 2L;
  private static final Long MASTER_BRANCH_UNPUBLISHED_CURRICULUM_ID_2 = 22L;
  private static final Long MASTER_BRANCH_PUBLISHED_CURRICULUM_ID = 3L;
  private static final Long DRAFT_BRANCH_CURRICULUM_ID = 4L;
  private static final Long NEW_CURRICULUM_ID = 100L;
  private static final Long NEW_LESSON_ID = 200L;
  private static final Long NEW_SCHEDULE_ID = 300L;
  private static final String NAME = "NAME";
  private static final String DESCRIPTION = "DESCRIPTION";

  @BeforeAll
  public static void beforeAll() {
    LessonRepository lessonRepository = mock(LessonRepository.class);
    LessonScheduleRepository scheduleRepository = mock(LessonScheduleRepository.class);
    VisitRepository visitRepository = mock(VisitRepository.class);

    when(repository.findById(MASTER_BRANCH_UNPUBLISHED_CURRICULUM_ID))
        .thenReturn(
            Optional.of(
                Curriculum.builder()
                    .id(MASTER_BRANCH_UNPUBLISHED_CURRICULUM_ID)
                    .branch(CurriculumBranch.MASTER)
                    .published(false)
                    .build()));

    when(repository.findById(MASTER_BRANCH_UNPUBLISHED_CURRICULUM_ID_2))
        .thenReturn(
            Optional.of(
                Curriculum.builder()
                    .id(MASTER_BRANCH_UNPUBLISHED_CURRICULUM_ID_2)
                    .branch(CurriculumBranch.MASTER)
                    .published(false)
                    .build()));

    when(repository.findById(MASTER_BRANCH_PUBLISHED_CURRICULUM_ID))
        .thenReturn(
            Optional.of(
                Curriculum.builder()
                    .id(MASTER_BRANCH_PUBLISHED_CURRICULUM_ID)
                    .branch(CurriculumBranch.MASTER)
                    .published(true)
                    .build()));

    when(repository.findById(DRAFT_BRANCH_CURRICULUM_ID))
        .thenReturn(
            Optional.of(
                Curriculum.builder()
                    .id(DRAFT_BRANCH_CURRICULUM_ID)
                    .branch(CurriculumBranch.DRAFT)
                    .source(
                        Curriculum.builder()
                            .id(MASTER_BRANCH_PUBLISHED_CURRICULUM_ID)
                            .branch(CurriculumBranch.MASTER)
                            .published(true)
                            .build())
                    .published(false)
                    .build()));

    when(repository.save(any()))
        .then(
            invocation -> {
              Curriculum curriculum = invocation.getArgument(0);
              if (curriculum.getId() == null) {
                curriculum.setId(NEW_CURRICULUM_ID);
              }
              return curriculum;
            });

    when(lessonRepository.save(any()))
        .then(
            invocation -> {
              Lesson lesson = invocation.getArgument(0);
              if (lesson.getId() == null) {
                lesson.setId(NEW_LESSON_ID);
              }
              return lesson;
            });

    when(lessonRepository.findById(2L))
        .thenReturn(
            Optional.of(
                Lesson.builder()
                    .id(2L)
                    .number("L1")
                    .source(Lesson.builder().id(22L).build())
                    .build()));

    when(scheduleRepository.findById(3L))
        .thenReturn(
            Optional.of(
                LessonSchedule.builder()
                    .id(3L)
                    .lessons(Collections.singletonList(new Domain(null, "L1")))
                    .source(LessonSchedule.builder().id(33L).build())
                    .build()));

    when(scheduleRepository.save(any()))
        .then(
            invocation -> {
              LessonSchedule schedule = invocation.getArgument(0);
              if (schedule.getId() == null) {
                schedule.setId(NEW_SCHEDULE_ID);
              }
              return schedule;
            });

    service =
        new CurriculumService(
            repository,
            visitRepository,
            lessonRepository,
            scheduleRepository,
            mock(BabyRepository.class));
  }

  @Test
  public void should_filter_invalid_lessons() {
    Lesson lesson = Lesson.builder().number("L1").build();
    LessonSchedule schedule =
        LessonSchedule.builder()
            .lessons(Arrays.asList(new Domain(null, "L1"), new Domain(null, "invalid")))
            .build();
    CurriculumDTO curriculumDTO =
        CurriculumDTO.builder()
            .name(NAME)
            .description(DESCRIPTION)
            .sessions(Collections.singletonList(lesson))
            .schedules(Collections.singletonList(schedule))
            .build();
    Optional<CurriculumResultDTO> optional = service.draft(curriculumDTO);
    assertTrue(optional.isPresent());
    CurriculumResultDTO result = optional.get();
    assertEquals(1, result.getSchedules().size());
  }

  @Test
  public void should_create_master_branch_and_unpublished() {
    Lesson lesson = Lesson.builder().number("L1").build();
    LessonSchedule schedule =
        LessonSchedule.builder().lessons(Collections.singletonList(new Domain(null, "L1"))).build();
    CurriculumDTO curriculumDTO =
        CurriculumDTO.builder()
            .name(NAME)
            .description(DESCRIPTION)
            .sessions(Collections.singletonList(lesson))
            .schedules(Collections.singletonList(schedule))
            .build();
    Optional<CurriculumResultDTO> optional = service.draft(curriculumDTO);
    assertTrue(optional.isPresent());
    CurriculumResultDTO result = optional.get();
    assertEquals(CurriculumBranch.MASTER, result.getBranch());
    assertFalse(result.isPublished());

    assertEquals(NEW_CURRICULUM_ID, result.getId());
    assertEquals(NAME, result.getName());
    assertEquals(DESCRIPTION, result.getDescription());
    assertNull(result.getSourceId());

    assertEquals(1, result.getSessions().size());
    assertEquals(NEW_LESSON_ID, result.getSessions().get(0).getId());
    assertEquals(NEW_CURRICULUM_ID, result.getSessions().get(0).getCurriculum().getId());

    assertEquals(1, result.getSchedules().size());
    assertEquals(NEW_SCHEDULE_ID, result.getSchedules().get(0).getId());
    assertEquals(NEW_CURRICULUM_ID, result.getSchedules().get(0).getCurriculum().getId());
  }

  @Test
  public void should_update_master_branch_and_unpublished() {
    CurriculumDTO curriculumDTO =
        CurriculumDTO.builder()
            .id(MASTER_BRANCH_UNPUBLISHED_CURRICULUM_ID)
            .name(NAME)
            .description(DESCRIPTION)
            .sessions(Collections.singletonList(Lesson.builder().number("L1").build()))
            .schedules(
                Collections.singletonList(
                    LessonSchedule.builder()
                        .lessons(Collections.singletonList(new Domain(null, "L1")))
                        .build()))
            .build();

    Optional<CurriculumResultDTO> optional = service.draft(curriculumDTO);
    assertTrue(optional.isPresent());
    CurriculumResultDTO result = optional.get();
    assertEquals(MASTER_BRANCH_UNPUBLISHED_CURRICULUM_ID, result.getId());
    assertEquals(CurriculumBranch.MASTER, result.getBranch());
    assertFalse(result.isPublished());
  }

  @Test
  public void should_create_master_branch_and_publish() {
    LessonSchedule schedule =
        LessonSchedule.builder()
            .lessons(Arrays.asList(new Domain(null, "L2"), new Domain(null, "L1")))
            .build();
    CurriculumDTO curriculumDTO =
        CurriculumDTO.builder()
            .name(NAME)
            .description(DESCRIPTION)
            .sessions(
                Arrays.asList(
                    Lesson.builder().number("L1").startOfApplicableDays(10).build(),
                    Lesson.builder().number("L2").startOfApplicableDays(20).build()))
            .schedules(Collections.singletonList(schedule))
            .build();
    Optional<CurriculumResultDTO> optional = service.publish(curriculumDTO);
    assertTrue(optional.isPresent());
    CurriculumResultDTO result = optional.get();
    assertEquals(CurriculumBranch.MASTER, result.getBranch());
    assertTrue(result.isPublished());

    assertEquals(NEW_CURRICULUM_ID, result.getId());
    assertEquals(NAME, result.getName());
    assertEquals(DESCRIPTION, result.getDescription());
    assertNull(result.getSourceId());

    assertEquals(2, result.getSessions().size());
    assertEquals(NEW_LESSON_ID, result.getSessions().get(0).getId());
    assertEquals(NEW_CURRICULUM_ID, result.getSessions().get(0).getCurriculum().getId());

    assertEquals(1, result.getSchedules().size());
    assertEquals(NEW_SCHEDULE_ID, result.getSchedules().get(0).getId());
    assertEquals(NEW_CURRICULUM_ID, result.getSchedules().get(0).getCurriculum().getId());

    assertEquals("L1", result.getSchedules().get(0).getLessons().get(0).getLabel());
    assertEquals("L2", result.getSchedules().get(0).getLessons().get(1).getLabel());
  }

  @Test
  public void should_update_master_branch_and_publish() {
    CurriculumDTO curriculumDTO =
        CurriculumDTO.builder()
            .id(MASTER_BRANCH_UNPUBLISHED_CURRICULUM_ID_2)
            .name(NAME)
            .description(DESCRIPTION)
            .sessions(Collections.singletonList(Lesson.builder().number("L1").build()))
            .schedules(
                Collections.singletonList(
                    LessonSchedule.builder()
                        .lessons(Collections.singletonList(new Domain(null, "L1")))
                        .build()))
            .build();

    Optional<CurriculumResultDTO> optional = service.publish(curriculumDTO);
    assertTrue(optional.isPresent());
    CurriculumResultDTO result = optional.get();
    assertEquals(MASTER_BRANCH_UNPUBLISHED_CURRICULUM_ID_2, result.getId());
    assertEquals(CurriculumBranch.MASTER, result.getBranch());
    assertTrue(result.isPublished());
  }

  @Test
  public void should_create_draft_branch_and_unpublished() {
    CurriculumDTO curriculumDTO =
        CurriculumDTO.builder()
            .id(MASTER_BRANCH_PUBLISHED_CURRICULUM_ID)
            .name(NAME)
            .description(DESCRIPTION)
            .sessions(Collections.singletonList(Lesson.builder().id(2L).number("L1").build()))
            .schedules(
                Collections.singletonList(
                    LessonSchedule.builder()
                        .id(2L)
                        .lessons(Collections.singletonList(new Domain(null, "L1")))
                        .build()))
            .build();

    Optional<CurriculumResultDTO> optional = service.draft(curriculumDTO);
    assertTrue(optional.isPresent());
    CurriculumResultDTO result = optional.get();
    assertEquals(NEW_CURRICULUM_ID, result.getId());
    assertEquals(CurriculumBranch.DRAFT, result.getBranch());
    assertEquals(MASTER_BRANCH_PUBLISHED_CURRICULUM_ID, result.getSourceId());
    assertEquals(NEW_LESSON_ID, result.getSessions().get(0).getId());
    assertEquals(2L, result.getSessions().get(0).getSource().getId());
    assertEquals(NEW_SCHEDULE_ID, result.getSchedules().get(0).getId());
    assertFalse(result.isPublished());
  }

  @Test
  public void should_publish_draft_branch() {
    CurriculumDTO curriculumDTO =
        CurriculumDTO.builder()
            .id(DRAFT_BRANCH_CURRICULUM_ID)
            .name(NAME)
            .description(DESCRIPTION)
            .sessions(Collections.singletonList(Lesson.builder().id(2L).number("L1").build()))
            .schedules(
                Collections.singletonList(
                    LessonSchedule.builder()
                        .id(3L)
                        .lessons(Collections.singletonList(new Domain(null, "L1")))
                        .build()))
            .build();

    Optional<CurriculumResultDTO> optional = service.publish(curriculumDTO);
    assertTrue(optional.isPresent());
    CurriculumResultDTO result = optional.get();
    assertEquals(MASTER_BRANCH_PUBLISHED_CURRICULUM_ID, result.getId());
    assertEquals(CurriculumBranch.MASTER, result.getBranch());
    assertTrue(result.isPublished());

    assertEquals(22L, result.getSessions().get(0).getId());
    assertNull(result.getSessions().get(0).getSource());

    assertEquals(33L, result.getSchedules().get(0).getId());
    assertNull(result.getSchedules().get(0).getSource());
  }
}
