package edu.stanford.fsi.reap.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class JsonArrayConverter<T> {

  private final ObjectMapper objectMapper = new ObjectMapper();

  public abstract TypeReference<List<T>> typeReference();

  public String convertToDatabaseColumn(List<T> attribute) {
    try {
      return objectMapper.writeValueAsString(attribute);
    } catch (JsonProcessingException e) {
      log.error("Unexpected encoding object: {}", attribute);
      return null;
    }
  }

  public List<T> convertToEntityAttribute(String dbData) {
    try {
      if (dbData == null || dbData.isEmpty()) {
        return null;
      }
      return objectMapper.readValue(dbData, typeReference());
    } catch (IOException ex) {
      log.error("Unexpected IOEx decoding json from database: {}", dbData);
      return null;
    }
  }
}
