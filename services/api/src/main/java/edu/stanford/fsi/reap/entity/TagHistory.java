package edu.stanford.fsi.reap.entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
public class TagHistory implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Size(max = 100)
  private String name;

  private Long historyId;

  public TagHistory(String tagName) {
    this.name = tagName;
  }
}
