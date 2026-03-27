package edu.stanford.fsi.reap.entity;

import edu.stanford.fsi.reap.converter.VisitReportObjDataConverter;
import edu.stanford.fsi.reap.pojo.VisitReportObjData;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.SQLDeleteAll;
import org.hibernate.annotations.Where;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Where(clause = AbstractNormalEntity.SKIP_DELETED_CLAUSE)
@SQLDeleteAll(sql = "UPDATE visit_report SET delete = true WHERE id = ?")
public class VisitReport extends AbstractNormalEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull @Convert(converter = VisitReportObjDataConverter.class)
  private VisitReportObjData visitReportObjData;

  @NotNull @OneToOne private Visit visit;
}
