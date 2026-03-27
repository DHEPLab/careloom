package edu.stanford.fsi.reap.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.fsi.reap.entity.Visit;
import edu.stanford.fsi.reap.pojo.VisitReportObjData;
import java.time.LocalDateTime;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

public class VisitReportObjDataConverterTest {

  private final VisitReportObjDataConverter converter = new VisitReportObjDataConverter();
  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * 多一个字段，少一个字段，会不影响，转换 private final String jsonByDb =
   * "{\"visit\":{\"id\":1,\"visitTime\":null,\"year\":null,\"month\":null,\"day\":null,\"baby\":null,\"lesson\":null,\"nextModuleIndex\":0,\"status\":\"NOT_STARTED\",\"chw\":null,\"startTime\":null,\"completeTime\":null,\"remark\":null},\"carers\":null,\"modules\":null}";
   */
  @Test
  void should_converter_by_str_add_field() throws JsonProcessingException {
    // 多一个不行
    String json =
        "{\"visit\":{\"ids\":1,\"id\":1,\"visitTime\":null,\"year\":null,\"month\":null,\"day\":null,\"baby\":null,\"lesson\":null,\"nextModuleIndex\":0,\"status\":\"NOT_STARTED\",\"chw\":null,\"startTime\":null,\"completeTime\":null,\"remark\":null},\"carers\":null,\"modules\":null}";
    VisitReportObjData visitReportObjData = converter.convertToEntityAttribute(json);
    assertNotEquals(objectMapper.readValue(json, HashMap.class), visitReportObjData);
  }

  @Test
  void should_converter_by_str_remover_field() {
    // 少一个
    String json =
        "{\"visit\":{\"visitTime\":null,\"year\":null,\"month\":1,\"day\":null,\"baby\":null,\"lesson\":null,\"nextModuleIndex\":0,\"status\":\"NOT_STARTED\",\"chw\":null,\"startTime\":null,\"completeTime\":null,\"remark\":null},\"carers\":null,\"modules\":null}";
    VisitReportObjData visitReportObjData = converter.convertToEntityAttribute(json);
    assertEquals(1, visitReportObjData.getVisit().getMonth());
  }

  @Test
  void should_converter() throws JsonProcessingException {
    String json =
        "{\"visit\":{\"id\":1,\"visitTime\":null,\"year\":null,\"month\":null,\"day\":null,\"baby\":null,\"lesson\":null,\"nextModuleIndex\":0,\"status\":\"NOT_STARTED\",\"chw\":null,\"startTime\":null,\"completeTime\":null,\"remark\":null},\"carers\":null,\"modules\":null}";
    VisitReportObjData visitReportObjData = converter.convertToEntityAttribute(json);
    VisitReportObjData objData = objectMapper.readValue(json, VisitReportObjData.class);
    assertEquals(objData, visitReportObjData);
  }

  @Test
  void should_converter_LocalDateTime_to_string_LocalDateTime_to_obj() {
    VisitReportObjData objData =
        VisitReportObjData.builder()
            .visit(Visit.builder().visitTime(LocalDateTime.now()).build())
            .build();
    String json = converter.convertToDatabaseColumn(objData);

    VisitReportObjData visitReportObjData = converter.convertToEntityAttribute(json);
    assertEquals(objData, visitReportObjData);
  }

  @Test
  void should_converter_LocalDate() throws JsonProcessingException {
    Visit visit = Visit.builder().build();
    String json = objectMapper.writeValueAsString(visit);

    Visit objData = objectMapper.readValue(json, Visit.class);
    assertEquals(objData, visit);
  }
}
