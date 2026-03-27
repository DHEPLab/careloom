package edu.stanford.fsi.reap.entity;

import edu.stanford.fsi.reap.converter.BabyConverter;
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
public class BabyModifyRecord extends AbstractNormalEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long userId;

  private Long babyId;

  @NotNull @Convert(converter = BabyConverter.class)
  private Baby oldBabyJson;

  @NotNull @Convert(converter = BabyConverter.class)
  private Baby newBabyJson;

  private String changedColumn;

  private Boolean approved;
}
