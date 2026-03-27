package edu.stanford.fsi.reap.dto;

import edu.stanford.fsi.reap.entity.Module;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModulePackage {

  private List<Module> modules;

  private List<String> media;
}
