package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.dto.QuestionnaireResultDTO;
import edu.stanford.fsi.reap.entity.Questionnaire;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {

  @Override
  Questionnaire save(Questionnaire entity);

  @Override
  void deleteById(Long id);

  Optional<Questionnaire> findBySourceId(Long sourceId);

  @Query(
      value =
          "SELECT q.id as id, q.name as name, q.branch as branch, q.published as published FROM"
              + " Questionnaire q WHERE q.source is null and q.projectId=?2 and q.name like"
              + " concat('%', ?1, '%') ORDER BY q.createdAt DESC")
  Page<QuestionnaireResultDTO> findBySearch(String name, Long projectId, Pageable pageable);

  @Query(
      value =
          "SELECT q.id as id, q.name as name, q.branch as branch, q.published as published FROM"
              + " Questionnaire q WHERE q.published = true ")
  List<QuestionnaireResultDTO> findAllDto();
}
