package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

  @Override
  Project save(Project entity);

  @Override
  void deleteById(Long id);

  long countByName(String name);

  long countByCode(String code);
}
