package edu.stanford.fsi.reap.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import edu.stanford.fsi.reap.entity.Baby;
import javax.persistence.AttributeConverter;

public class BabyConverter extends JsonObjectConverter<Baby>
    implements AttributeConverter<Baby, String> {

  @Override
  public TypeReference<Baby> typeReference() {
    return new TypeReference<Baby>() {};
  }
}
