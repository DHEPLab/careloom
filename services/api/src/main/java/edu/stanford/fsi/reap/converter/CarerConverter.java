package edu.stanford.fsi.reap.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import edu.stanford.fsi.reap.entity.Carer;
import javax.persistence.AttributeConverter;

public class CarerConverter extends JsonObjectConverter<Carer>
    implements AttributeConverter<Carer, String> {

  @Override
  public TypeReference<Carer> typeReference() {
    return new TypeReference<Carer>() {};
  }
}
