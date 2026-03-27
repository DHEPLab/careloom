package edu.stanford.fsi.reap.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import edu.stanford.fsi.reap.pojo.Component;
import java.util.List;
import javax.persistence.AttributeConverter;

public class ComponentListConverter extends JsonArrayConverter<Component>
    implements AttributeConverter<List<Component>, String> {

  @Override
  public TypeReference<List<Component>> typeReference() {
    return new TypeReference<List<Component>>() {};
  }
}
