package edu.stanford.fsi.reap.repository;

import edu.stanford.fsi.reap.entity.UnfilteredUser;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnfilteredUserRepository extends JpaRepository<UnfilteredUser, Long> {

  List<UnfilteredUser> findByRoleOrderByCreatedAtDesc(String role);
}
