package edu.stanford.fsi.reap.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class JsonObjectConverter<T> {

  private final ObjectMapper objectMapper = new ObjectMapper();

  public JsonObjectConverter() {
    JavaTimeModule javaTimeModule = new JavaTimeModule();
    javaTimeModule.addSerializer(LocalDateTime.class, LocalDateTimeSerializer.INSTANCE);
    javaTimeModule.addDeserializer(LocalDateTime.class, LocalDateTimeDeserializer.INSTANCE);
    javaTimeModule.addSerializer(LocalDate.class, LocalDateSerializer.INSTANCE);
    javaTimeModule.addDeserializer(LocalDate.class, LocalDateDeserializer.INSTANCE);
    objectMapper.registerModule(javaTimeModule);
  }

  public abstract TypeReference<T> typeReference();

  public String convertToDatabaseColumn(T attribute) {
    try {
      return objectMapper.writeValueAsString(attribute);
    } catch (JsonProcessingException e) {
      log.error("Unexpected encoding object: {}", attribute, e);
      return null;
    }
  }

  public T convertToEntityAttribute(String dbData) {
    try {
      if (dbData == null || dbData.isEmpty()) {
        return null;
      }
      return objectMapper.readValue(dbData, typeReference());
    } catch (IOException e) {
      log.error("Unexpected IOEx decoding json from database: {}", dbData, e);
      return null;
    }
  }
}
