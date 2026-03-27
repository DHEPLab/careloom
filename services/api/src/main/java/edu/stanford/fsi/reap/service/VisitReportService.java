package edu.stanford.fsi.reap.service;

import edu.stanford.fsi.reap.dto.ReportDTO;
import edu.stanford.fsi.reap.entity.Module;
import edu.stanford.fsi.reap.entity.Visit;
import edu.stanford.fsi.reap.entity.VisitReport;
import edu.stanford.fsi.reap.entity.enumerations.CurriculumBranch;
import edu.stanford.fsi.reap.pojo.Domain;
import edu.stanford.fsi.reap.pojo.VisitReportObjData;
import edu.stanford.fsi.reap.repository.CarerRepository;
import edu.stanford.fsi.reap.repository.ModuleRepository;
import edu.stanford.fsi.reap.repository.QuestionnaireRecordRepository;
import edu.stanford.fsi.reap.repository.VisitReportRepository;
import edu.stanford.fsi.reap.security.SecurityUtils;
import edu.stanford.fsi.reap.web.rest.errors.BadRequestAlertException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VisitReportService {

  private final VisitReportRepository visitReportRepository;
  private final CarerRepository carerRepository;
  private final ModuleRepository moduleRepository;
  private final QuestionnaireRecordRepository questionnaireRecordRepository;
  private final ExcelService excelService;

  public VisitReportService(
      VisitReportRepository visitReportRepository,
      CarerRepository carerRepository,
      ModuleRepository moduleRepository,
      QuestionnaireRecordRepository questionnaireRecordRepository,
      ExcelService excelService) {
    this.visitReportRepository = visitReportRepository;
    this.carerRepository = carerRepository;
    this.moduleRepository = moduleRepository;
    this.questionnaireRecordRepository = questionnaireRecordRepository;
    this.excelService = excelService;
  }

  /** 导出报表 Visit history */
  public byte[] report(ReportDTO reportDTO, String lang, String timezone) {
    if (!reportDTO.validDay()) throw new BadRequestAlertException("error.excel.report.invalidDate");
    Long projectId = SecurityUtils.getProjectId();
    List<VisitReportObjData> list =
        visitReportRepository
            .findByCreatedAtBetween(reportDTO.getStartTime(), reportDTO.getEndTime())
            .stream()
            .filter(
                target -> {
                  return target.getVisit().getProjectId().equals(projectId)
                      && target.getVisitReportObjData() != null;
                })
            .map(
                visitReport -> {
                  @NotNull VisitReportObjData visitReportObjData = visitReport.getVisitReportObjData();
                  visitReportObjData.setVisit(visitReport.getVisit());
                  return visitReportObjData;
                })
            .collect(Collectors.toList());
    // 遍历取出问卷内容记录，并根据titleNo进行排序
    sort(list);
    return excelService.generateVisitReportExcel(list, lang, timezone);
  }

  private void sort(List<VisitReportObjData> list) {
    for (VisitReportObjData visitReportObjData : list) {
      visitReportObjData.setQuestionnaireRecords(
          visitReportObjData.getQuestionnaireRecords().stream()
              .sorted(
                  (o1, o2) -> {
                    int titleNo1 = Integer.parseInt(o1.getTitleNo());
                    int titleNo2 = Integer.parseInt(o2.getTitleNo());
                    return titleNo1 - titleNo2;
                  })
              .collect(Collectors.toList()));
    }
  }

  /** 修改记录数据的家访备注 */
  public void updateVisitRemarkInfo(Visit visit) {
    log.info(
        "update report by visit remark , visit id is {}, remark is {}",
        visit.getId(),
        visit.getRemark());
    visitReportRepository
        .findByVisitId(visit.getId())
        .ifPresent(
            visitReport -> {
              visitReport.getVisitReportObjData().getVisit().setRemark(visit.getRemark());
              visitReportRepository.save(visitReport);
            });
  }

  /** 保存记录数据 */
  public VisitReport saveVisitReport(Visit visit) {
    return visitReportRepository.save(
        VisitReport.builder()
            .visitReportObjData(mapVisitReportObjData(visit))
            .visit(visit)
            .build());
  }

  private VisitReportObjData mapVisitReportObjData(Visit visit) {
    log.info(
        "write report by visit , id is {} , babyId is {} , lessonId is {},",
        visit.getId(),
        visit.getBaby().getId(),
        visit.getLesson().getId());

    return VisitReportObjData.builder()
        .visit(visit)
        .carers(carerRepository.findByBabyIdOrderByMasterDesc(visit.getBaby().getId()))
        .modules(mapModule(visit.getLesson().getModules()))
        .questionnaireRecords(
            questionnaireRecordRepository.findByVisitIdOrderByTitleNoAsc(visit.getId()))
        .build();
  }

  private List<Module> mapModule(List<Domain> domains) {
    Set<Long> allIdByDomains = domains.stream().map(Domain::longValue).collect(Collectors.toSet());
    return moduleRepository.findByBranchAndPublishedTrue(CurriculumBranch.MASTER).stream()
        .filter(module -> allIdByDomains.contains(module.getId()))
        .collect(Collectors.toList());
  }
}
