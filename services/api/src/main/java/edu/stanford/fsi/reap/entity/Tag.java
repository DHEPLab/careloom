package edu.stanford.fsi.reap.entity;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hookszhang
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Tag implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Size(max = 100)
  private String name;

  public Tag(String tagName) {
    this.name = tagName;
  }
}
