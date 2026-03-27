package edu.stanford.fsi.reap.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeUtil {
  private static final DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

  public static String toResponseString(ZonedDateTime zonedDateTime) {
    return zonedDateTime.format(formatter);
  }

  public static String getLocalDatetimeWithTimezone(LocalDateTime datetime, String timezone) {
    return ZonedDateTimeUtil.toResponseString(
        datetime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of(timezone)));
  }
}
