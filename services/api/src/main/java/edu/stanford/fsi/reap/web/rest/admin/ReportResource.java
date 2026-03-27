package edu.stanford.fsi.reap.web.rest.admin;

import edu.stanford.fsi.reap.dto.ReportDTO;
import edu.stanford.fsi.reap.service.BabyService;
import edu.stanford.fsi.reap.service.CommunityHouseWorkerService;
import edu.stanford.fsi.reap.service.VisitReportService;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/report")
public class ReportResource {

  private final VisitReportService visitReportService;
  private final BabyService babyService;
  private final CommunityHouseWorkerService communityHouseWorkerService;

  public ReportResource(
      VisitReportService visitReportService,
      BabyService babyRosterReport,
      CommunityHouseWorkerService communityHouseWorkerService) {
    this.visitReportService = visitReportService;
    this.babyService = babyRosterReport;
    this.communityHouseWorkerService = communityHouseWorkerService;
  }

  @GetMapping
  public ResponseEntity<byte[]> visitReportExcel(
      @Valid ReportDTO reportDTO,
      @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "en") String lang,
      @RequestHeader(value = "X-Timezone", defaultValue = "Etc/UTC") String timezone) {
    byte[] ret = visitReportService.report(reportDTO, lang, timezone);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=" + UUID.randomUUID().toString() + ".xlsx")
        .body(ret);
  }

  @GetMapping("/babyRosterReport")
  public ResponseEntity<byte[]> babyRosterReport(
      @RequestHeader(value = "X-Timezone", defaultValue = "Etc/UTC") String timezone) {
    byte[] ret = babyService.babyRosterReport(timezone);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=" + UUID.randomUUID().toString() + ".xlsx")
        .body(ret);
  }

  @GetMapping("/chwReport")
  public ResponseEntity<byte[]> chwReport(
      @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "en") String lang,
      @RequestHeader(value = "X-Timezone", defaultValue = "Etc/UTC") String timezone) {
    byte[] ret = communityHouseWorkerService.chwReport(lang, timezone);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=" + UUID.randomUUID().toString() + ".xlsx")
        .body(ret);
  }
}
