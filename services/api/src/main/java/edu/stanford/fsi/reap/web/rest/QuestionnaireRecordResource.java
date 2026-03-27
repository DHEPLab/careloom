package edu.stanford.fsi.reap.web.rest;

import edu.stanford.fsi.reap.repository.QuestionnaireRecordRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/record")
public class QuestionnaireRecordResource {

  private final QuestionnaireRecordRepository repository;

  public QuestionnaireRecordResource(QuestionnaireRecordRepository repository) {
    this.repository = repository;
  }
}
