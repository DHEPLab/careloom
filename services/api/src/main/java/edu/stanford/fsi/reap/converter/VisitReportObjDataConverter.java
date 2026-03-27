package edu.stanford.fsi.reap.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import edu.stanford.fsi.reap.pojo.VisitReportObjData;
import javax.persistence.AttributeConverter;

public class VisitReportObjDataConverter extends JsonObjectConverter<VisitReportObjData>
    implements AttributeConverter<VisitReportObjData, String> {

  @Override
  public TypeReference<VisitReportObjData> typeReference() {
    return new TypeReference<VisitReportObjData>() {};
  }
}
