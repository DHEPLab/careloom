package edu.stanford.fsi.reap.web.rest;

import edu.stanford.fsi.reap.entity.ErrorLog;
import edu.stanford.fsi.reap.entity.enumerations.ErrorLogType;
import edu.stanford.fsi.reap.repository.SysErrorLogRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/log")
public class SysErrorLogResource {

  private final SysErrorLogRepository sysErrorLogRepository;

  public SysErrorLogResource(SysErrorLogRepository sysErrorLogRepository) {
    this.sysErrorLogRepository = sysErrorLogRepository;
  }

  @GetMapping("/{babyId}")
  public ResponseEntity<List<ErrorLog>> getErrorLog(@PathVariable Long babyId) {
    return ResponseEntity.ok(
        sysErrorLogRepository.findByTypeAndTypeId(ErrorLogType.APP_CREATE_VISIT, babyId));
  }

  @DeleteMapping("/{id}")
  public void deleteErrorLogById(@PathVariable Long id) {
    sysErrorLogRepository.deleteById(id);
  }
}
