package edu.stanford.fsi.reap.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import edu.stanford.fsi.reap.pojo.Question;
import java.util.List;
import javax.persistence.AttributeConverter;

public class QuestionConverter extends JsonArrayConverter<Question>
    implements AttributeConverter<List<Question>, String> {

  @Override
  public TypeReference<List<Question>> typeReference() {
    return new TypeReference<List<Question>>() {};
  }
}
