package edu.stanford.fsi.reap.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import javax.persistence.AttributeConverter;

public class StringListConverter extends JsonArrayConverter<String>
    implements AttributeConverter<List<String>, String> {

  @Override
  public TypeReference<List<String>> typeReference() {
    return new TypeReference<List<String>>() {};
  }
}
