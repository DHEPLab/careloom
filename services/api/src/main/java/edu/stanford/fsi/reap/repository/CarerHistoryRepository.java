package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.entity.CarerHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarerHistoryRepository extends JpaRepository<CarerHistory, Long> {

  @Override
  CarerHistory save(CarerHistory entity);

  @Override
  void deleteById(Long id);
}
