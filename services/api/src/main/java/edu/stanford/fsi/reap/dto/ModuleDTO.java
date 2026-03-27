package edu.stanford.fsi.reap.dto;

import edu.stanford.fsi.reap.entity.enumerations.CurriculumBranch;
import edu.stanford.fsi.reap.entity.enumerations.ModuleTopic;

public interface ModuleDTO {

  Long getId();

  String getNumber();

  String getName();

  ModuleTopic getTopic();

  CurriculumBranch getBranch();

  boolean isPublished();
}
