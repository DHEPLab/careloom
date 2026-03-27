package edu.stanford.fsi.reap.pojo;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Question implements Serializable {

  private String type;
  private Object value;
}
