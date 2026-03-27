package edu.stanford.fsi.reap.service;

import edu.stanford.fsi.reap.entity.Baby;
import edu.stanford.fsi.reap.entity.Carer;
import edu.stanford.fsi.reap.repository.CarerRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hookszhang
 */
@Service
@Transactional
public class CarerService {

  public final CarerRepository repository;

  public CarerService(CarerRepository repository) {
    this.repository = repository;
  }

  /**
   * A baby can only have one master carer if the baby does not have a master carer, set the current
   * carer as master
   *
   * @param carer Carer
   */
  public Carer save(Carer carer) {
    Optional<Carer> master = repository.findOneByBabyIdAndMasterIsTrue(carer.getBaby().getId());

    if (carer.isMaster()) {
      master.ifPresent(
          m -> {
            m.setMaster(false);
            repository.save(m);
          });
    } else if (!master.isPresent()) {
      carer.setMaster(true);
    }

    return repository.save(carer);
  }

  public void saveAll(List<Carer> carers, Baby baby) {
    carers.forEach(
        carer -> {
          carer.setId(null);
          carer.setBaby(baby);
          repository.save(carer);
        });
  }
}
