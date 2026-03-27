package edu.stanford.fsi.reap.pojo;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple domain pojo. Establish a simple connection with domain
 *
 * @author hookszhang
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Domain implements Serializable {
  /** Domain Identity, Long type As string type storage is used to use MySQL json_search */
  private String value;

  private String label;

  public Long longValue() {
    return Long.valueOf(value);
  }
}
