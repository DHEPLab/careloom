package edu.stanford.fsi.reap.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import javax.persistence.AttributeConverter;

public class LongListConverter extends JsonArrayConverter<Long>
    implements AttributeConverter<List<Long>, String> {

  @Override
  public TypeReference<List<Long>> typeReference() {
    return new TypeReference<List<Long>>() {};
  }
}
