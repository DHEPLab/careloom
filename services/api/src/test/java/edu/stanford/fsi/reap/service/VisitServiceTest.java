package edu.stanford.fsi.reap.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.stanford.fsi.reap.dto.VisitDTO;
import edu.stanford.fsi.reap.dto.VisitDateDTO;
import edu.stanford.fsi.reap.entity.Visit;
import edu.stanford.fsi.reap.entity.enumerations.VisitStatus;
import edu.stanford.fsi.reap.repository.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class VisitServiceTest {

  private static VisitService service;

  @BeforeAll
  public static void beforeAll() {
    CarerRepository carerRepository = mock(CarerRepository.class);
    LessonService lessonService = mock(LessonService.class);
    VisitRepository visitRepository = mock(VisitRepository.class);
    ExcelService excelService = mock(ExcelService.class);
    ExportVisitRepository exportVisitRepository = mock(ExportVisitRepository.class);
    QuestionnaireRecordRepository questionnaireRecordRepository =
        mock(QuestionnaireRecordRepository.class);
    VisitReportService visitReportService = mock(VisitReportService.class);
    VisitPositionRecordRepository visitPositionRecordRepository =
        mock(VisitPositionRecordRepository.class);

    when(visitRepository.save(any()))
        .then(
            invocation -> {
              Visit visit = invocation.getArgument(0);
              if (visit.getId() == null) {
                visit.setId(1L);
              }
              return visit;
            });

    List<VisitDateDTO> dates =
        Arrays.asList(
            new VisitDateDTO() {
              @Override
              public Integer getYear() {
                return 2020;
              }

              @Override
              public Integer getMonth() {
                return 8;
              }

              @Override
              public Integer getDay() {
                return 20;
              }
            },
            new VisitDateDTO() {
              @Override
              public Integer getYear() {
                return 2020;
              }

              @Override
              public Integer getMonth() {
                return 10;
              }

              @Override
              public Integer getDay() {
                return 1;
              }
            });
    when(visitRepository.findDateByChwId(4L)).thenReturn(dates);

    service =
        new VisitService(
            carerRepository,
            lessonService,
            visitRepository,
            excelService,
            exportVisitRepository,
            questionnaireRecordRepository,
            visitReportService,
            visitPositionRecordRepository);
  }

  @Test
  public void should_create_visit() {
    VisitDTO dto =
        VisitDTO.builder()
            .babyId(2L)
            .lessonId(3L)
            .visitTime(LocalDateTime.of(2020, 10, 1, 10, 0))
            .build();
    Visit visit = service.create(dto, 4L);

    assertEquals(1L, visit.getId());
    assertEquals(2L, visit.getBaby().getId());
    assertEquals(3L, visit.getLesson().getId());
    assertEquals(4L, visit.getChw().getId());
    assertEquals(dto.getVisitTime().getNano(), visit.getVisitTime().getNano());
    assertEquals(2020, visit.getYear());
    assertEquals(10, visit.getMonth());
    assertEquals(1, visit.getDay());
    assertEquals(0, visit.getNextModuleIndex());
    assertEquals(VisitStatus.NOT_STARTED, visit.getStatus());
  }

  @Test
  public void should_return_calendar_marked_dates() {
    List<String> dates = service.markedDates(4L);
    assertArrayEquals(
        new String[] {
          "2020-08-20", "2020-10-01",
        },
        dates.toArray(new String[] {}));
  }

  @Test
  public void should_begin_visit() {
    Visit visit = Visit.builder().id(2L).status(VisitStatus.NOT_STARTED).build();
    service.begin(visit);
    assertEquals(2L, visit.getId());
    assertEquals(VisitStatus.UNDONE, visit.getStatus());
  }

  @Test
  public void should_done_visit_current_module() {
    Visit visit = Visit.builder().id(2L).nextModuleIndex(0).status(VisitStatus.UNDONE).build();
    service.moduleDone(visit);
    assertEquals(2L, visit.getId());
    assertEquals(1, visit.getNextModuleIndex());
  }

  @Test
  public void should_done_visit() {
    Visit visit = Visit.builder().id(2L).status(VisitStatus.UNDONE).build();
    service.done(visit);
    assertEquals(2L, visit.getId());
    assertEquals(VisitStatus.DONE, visit.getStatus());
  }
}
