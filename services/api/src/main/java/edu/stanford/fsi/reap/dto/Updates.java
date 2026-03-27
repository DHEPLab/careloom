package edu.stanford.fsi.reap.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Updates {

  private boolean updated;

  private LocalDateTime lastModifiedAt;

  public static Updates isTheLatest() {
    return new Updates(false, null);
  }

  public static Updates haveUpdate(LocalDateTime lastModifiedAt) {
    return new Updates(true, lastModifiedAt);
  }
}
