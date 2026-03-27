package edu.stanford.fsi.reap.converter;

import static org.junit.jupiter.api.Assertions.*;

import edu.stanford.fsi.reap.pojo.Domain;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class DomainListConverterTest {

  private final DomainListConverter converter = new DomainListConverter();

  @Test
  void should_convert_json_array_to_domain_list() {
    List<Domain> domains =
        converter.convertToEntityAttribute("[{\"value\":\"36\",\"label\":\"MuQinYingYang001\"}]");
    assertEquals(Collections.singletonList(new Domain("36", "MuQinYingYang001")), domains);
  }

  @Test
  void should_convert_domain_list_to_json_array() {
    String json =
        converter.convertToDatabaseColumn(
            Collections.singletonList(new Domain("36", "MuQinYingYang001")));
    assertEquals("[{\"value\":\"36\",\"label\":\"MuQinYingYang001\"}]", json);
  }
}
