package edu.stanford.fsi.reap.pojo;

import edu.stanford.fsi.reap.entity.Carer;
import edu.stanford.fsi.reap.entity.Module;
import edu.stanford.fsi.reap.entity.QuestionnaireRecord;
import edu.stanford.fsi.reap.entity.Visit;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VisitReportObjData implements Serializable {

  /** visit -> one baby / chw / lesson -> curriculum */
  private Visit visit;

  private List<Carer> carers;
  private List<Module> modules;
  private List<QuestionnaireRecord> questionnaireRecords;
}
