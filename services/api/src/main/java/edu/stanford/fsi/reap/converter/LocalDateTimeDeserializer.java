package edu.stanford.fsi.reap.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {
  public LocalDateTimeDeserializer() {
    super(LocalDateTime.class);
  }

  @Override
  public LocalDateTime deserialize(
      JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
    String datetimeString = jsonParser.getText();
    OffsetDateTime offsetDateTime = OffsetDateTime.parse(datetimeString);
    ZonedDateTime zonedDateTime = offsetDateTime.atZoneSameInstant(ZoneId.systemDefault());
    return zonedDateTime.toLocalDateTime();
  }
}
