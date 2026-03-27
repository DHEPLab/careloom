package edu.stanford.fsi.reap.entity;

import edu.stanford.fsi.reap.converter.StringListConverter;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

/**
 * @author hookszhang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SQLDelete(sql = "UPDATE community_house_worker SET deleted = true WHERE id = ?")
public class CommunityHouseWorker extends AbstractAuditingEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 50)
  @NotNull private String identity;

  @Column(columnDefinition = "json")
  @Convert(converter = StringListConverter.class)
  private List<String> tags;

  @ManyToOne private User supervisor;
}
