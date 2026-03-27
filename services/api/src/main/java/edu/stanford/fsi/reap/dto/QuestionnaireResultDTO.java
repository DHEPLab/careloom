package edu.stanford.fsi.reap.dto;

import edu.stanford.fsi.reap.entity.enumerations.QuestionnaireBranch;

public interface QuestionnaireResultDTO {
  Long getId();

  String getName();

  QuestionnaireBranch getBranch();

  boolean isPublished();
}
