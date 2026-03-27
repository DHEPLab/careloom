package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.entity.QuestionnaireRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionnaireRecordRepository extends JpaRepository<QuestionnaireRecord, Long> {

  @Override
  QuestionnaireRecord save(QuestionnaireRecord entity);

  @Override
  void deleteById(Long id);

  List<QuestionnaireRecord> findByVisitIdOrderByTitleNoAsc(Long visitId);
}
