package edu.stanford.fsi.reap.task;

import edu.stanford.fsi.reap.entity.Visit;
import edu.stanford.fsi.reap.entity.enumerations.VisitStatus;
import edu.stanford.fsi.reap.repository.VisitRepository;
import edu.stanford.fsi.reap.service.VisitService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VisitStatusAutomaticallyExpiresTask {

  private final VisitService visitService;
  private final VisitRepository visitRepository;

  public VisitStatusAutomaticallyExpiresTask(
      VisitService visitService, VisitRepository visitRepository) {
    this.visitService = visitService;
    this.visitRepository = visitRepository;
  }

  @Scheduled(cron = "${application.cron.visit-expired}")
  public void action() {
    log.info("Modify the status of home visits. Those that have not started have expired");
    LocalDateTime now = LocalDateTime.now();
    List<Visit> visits =
        visitRepository.findByDateAndStatus(
            now.getYear(), now.getMonthValue(), now.getDayOfMonth(), VisitStatus.NOT_STARTED);
    visitService.expired(visits);

    log.info("Task finished! expired visit count: {}", visits.size());
  }
}
