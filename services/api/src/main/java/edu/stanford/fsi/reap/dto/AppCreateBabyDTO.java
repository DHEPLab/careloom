package edu.stanford.fsi.reap.dto;

import edu.stanford.fsi.reap.entity.Baby;
import edu.stanford.fsi.reap.entity.Carer;
import java.util.List;
import lombok.Data;

/**
 * @author hookszhang
 */
@Data
public class AppCreateBabyDTO {
  private Baby baby;
  private List<Carer> carers;
}
