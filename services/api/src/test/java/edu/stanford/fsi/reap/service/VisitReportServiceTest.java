package edu.stanford.fsi.reap.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.stanford.fsi.reap.entity.*;
import edu.stanford.fsi.reap.pojo.VisitReportObjData;
import edu.stanford.fsi.reap.repository.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class VisitReportServiceTest {

  private static VisitReportService visitReportService;
  private static ExcelService excelService;

  @BeforeAll
  public static void beforeAll() throws JsonProcessingException {
    VisitReportRepository visitReportRepository = mock(VisitReportRepository.class);
    CarerRepository carerRepository = mock(CarerRepository.class);
    ModuleRepository moduleRepository = mock(ModuleRepository.class);
    QuestionnaireRecordRepository questionnaireRecordRepository =
        mock(QuestionnaireRecordRepository.class);
    excelService = mock(ExcelService.class);

    visitReportService =
        new VisitReportService(
            visitReportRepository,
            carerRepository,
            moduleRepository,
            questionnaireRecordRepository,
            VisitReportServiceTest.excelService);
  }

  @Test
  public void should_sort() {
    List<VisitReportObjData> expectedList =
        Collections.singletonList(
            new VisitReportObjData(
                null,
                null,
                null,
                Arrays.asList(
                    new QuestionnaireRecord(1L, "张三1", "answer1", "1", null),
                    new QuestionnaireRecord(2L, "张三2", "answer2", "2", null),
                    new QuestionnaireRecord(3L, "张三3", "answer3", "11", null),
                    new QuestionnaireRecord(4L, "张三4", "answer4", "12", null))));
    List<VisitReportObjData> list =
        Collections.singletonList(
            new VisitReportObjData(
                null,
                null,
                null,
                Arrays.asList(
                    new QuestionnaireRecord(3L, "张三3", "answer3", "11", null),
                    new QuestionnaireRecord(2L, "张三2", "answer2", "2", null),
                    new QuestionnaireRecord(4L, "张三4", "answer4", "12", null),
                    new QuestionnaireRecord(1L, "张三1", "answer1", "1", null))));

    try {
      Method sort = visitReportService.getClass().getDeclaredMethod("sort", List.class);
      sort.setAccessible(true);
      sort.invoke(visitReportService, list);
    } catch (Exception e) {
      e.printStackTrace();
    }
    assertEquals(expectedList, list);
  }
}
