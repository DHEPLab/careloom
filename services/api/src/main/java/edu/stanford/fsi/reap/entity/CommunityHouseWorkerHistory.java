package edu.stanford.fsi.reap.entity;

import edu.stanford.fsi.reap.converter.StringListConverter;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * @author hookszhang
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Where(clause = AbstractHistoryEntity.SKIP_DELETED_CLAUSE)
@SQLDelete(sql = "UPDATE community_house_worker_history SET deleted = true WHERE id = ?")
public class CommunityHouseWorkerHistory extends AbstractHistoryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 50)
  @NotNull private String identity;

  @Column(columnDefinition = "json")
  @Convert(converter = StringListConverter.class)
  private List<String> tags;

  @ManyToOne private User supervisor;

  private Long historyId;
}
