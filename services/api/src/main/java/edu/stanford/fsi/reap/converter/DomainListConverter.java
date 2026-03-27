package edu.stanford.fsi.reap.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import edu.stanford.fsi.reap.pojo.Domain;
import java.util.List;
import javax.persistence.AttributeConverter;

public class DomainListConverter extends JsonArrayConverter<Domain>
    implements AttributeConverter<List<Domain>, String> {

  @Override
  public TypeReference<List<Domain>> typeReference() {
    return new TypeReference<List<Domain>>() {};
  }
}
