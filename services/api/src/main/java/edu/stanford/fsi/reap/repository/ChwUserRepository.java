package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.entity.Chw;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** InterfaceName: ChwRepository Description: author: huangwenxing 2021-4-30 15:57 */
@Repository
public interface ChwUserRepository extends JpaRepository<Chw, Long> {
  @Override
  Chw save(Chw entity);

  @Override
  void deleteById(Long id);
}
