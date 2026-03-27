package edu.stanford.fsi.reap.entity;

import edu.stanford.fsi.reap.converter.CarerConverter;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SQLDelete(sql = "UPDATE baby_modify_record SET deleted = true WHERE id = ?")
public class CarerModifyRecord extends AbstractNormalEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long userId;

  private Long carerId;

  @NotNull @Convert(converter = CarerConverter.class)
  private Carer oldCarerJson;

  @NotNull @Convert(converter = CarerConverter.class)
  private Carer newCarerJson;

  private String changedColumn;
}
