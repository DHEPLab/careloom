package edu.stanford.fsi.reap.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import edu.stanford.fsi.reap.utils.ZonedDateTimeUtil;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class LocalDateTimeSerializer extends StdSerializer<LocalDateTime> {
  public LocalDateTimeSerializer() {
    super(LocalDateTime.class);
  }

  @Override
  public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    ZonedDateTime zonedDateTime = value.atZone(ZoneId.systemDefault());
    gen.writeString(ZonedDateTimeUtil.toResponseString(zonedDateTime));
  }
}
