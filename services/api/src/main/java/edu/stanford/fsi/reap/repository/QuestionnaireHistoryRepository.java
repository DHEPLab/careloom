package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.entity.QuestionnaireHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionnaireHistoryRepository extends JpaRepository<QuestionnaireHistory, Long> {
  @Override
  QuestionnaireHistory save(QuestionnaireHistory entity);

  @Override
  void deleteById(Long id);
}
