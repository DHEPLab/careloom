package edu.stanford.fsi.reap.service;

import com.google.common.collect.Lists;
import edu.stanford.fsi.reap.converter.StringListConverter;
import edu.stanford.fsi.reap.dto.ErrDTO;
import edu.stanford.fsi.reap.entity.*;
import edu.stanford.fsi.reap.entity.Module;
import edu.stanford.fsi.reap.entity.enumerations.*;
import edu.stanford.fsi.reap.pojo.Domain;
import edu.stanford.fsi.reap.pojo.VisitReportObjData;
import edu.stanford.fsi.reap.repository.*;
import edu.stanford.fsi.reap.security.SecurityUtils;
import edu.stanford.fsi.reap.utils.RegexConstant;
import edu.stanford.fsi.reap.utils.ZonedDateTimeUtil;
import edu.stanford.fsi.reap.web.rest.errors.BadRequestAlertException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class ExcelService {

  private final UserRepository userRepository;
  private final CarerRepository carerRepository;
  private final ModuleRepository moduleRepository;
  private final BabyRepository babyRepository;
  private final CommunityHouseWorkerRepository communityHouseWorkerRepository;
  private final PasswordEncoder passwordEncoder;
  private final TagService tagService;
  private final CommunityHouseWorkerRepository chwRepository;

  @Autowired private ResourceBundleMessageSource localSource;

  public ExcelService(
      UserRepository userRepository,
      CarerRepository carerRepository,
      ModuleRepository moduleRepository,
      BabyRepository babyRepository,
      CommunityHouseWorkerRepository communityHouseWorkerRepository,
      PasswordEncoder passwordEncoder,
      TagService tagService,
      CommunityHouseWorkerRepository chwRepository,
      ResourceBundleMessageSource localSource) {
    this.userRepository = userRepository;
    this.carerRepository = carerRepository;
    this.moduleRepository = moduleRepository;
    this.babyRepository = babyRepository;
    this.communityHouseWorkerRepository = communityHouseWorkerRepository;
    this.passwordEncoder = passwordEncoder;
    this.tagService = tagService;
    this.chwRepository = chwRepository;
    this.localSource = localSource;
  }

  /** Generate Visit Report */
  public byte[] generateVisitReportExcel(
      List<VisitReportObjData> visitReportObjData, String lang, String timezone) {
    return getVisitReport(visitReportObjData, this::mapByteArray, lang, timezone);
  }

  private byte[] getVisitReport(
      List<VisitReportObjData> visitReportObjData,
      Function<Workbook, byte[]> saveFileOrOther,
      String lang,
      String timezone) {
    try (InputStream inputStream =
            getTemplateResourceIO("static/excel/Healthy-Future-Report.xlsx");
        Workbook workBook = new XSSFWorkbook(inputStream)) {

      addContentRow(workBook, visitReportObjData, lang, timezone);

      return saveFileOrOther.apply(workBook);
    } catch (Exception e) {
      log.error("addContentRow ", e);
      throw new RuntimeException("error.excel.report.dataInvalid");
    }
  }

  private void addContentRow(
      Workbook workbook, List<VisitReportObjData> allData, String lang, String timezone) {
    Sheet sheet = workbook.getSheetAt(0);
    Locale locale = "zh".equals(lang) ? Locale.CHINESE : Locale.ENGLISH;

    int index = 1;
    for (VisitReportObjData itemRow : allData) {
      Row row = sheet.createRow(index);
      int cellInt = 0;

      row.createCell(cellInt++).setCellValue(itemRow.getVisit().getId());
      row.createCell(cellInt++)
          .setCellValue(
              ZonedDateTimeUtil.getLocalDatetimeWithTimezone(
                  itemRow.getVisit().getVisitTime(), timezone));

      if (itemRow.getVisit().getStartTime() != null) {
        row.createCell(cellInt++)
            .setCellValue(
                ZonedDateTimeUtil.getLocalDatetimeWithTimezone(
                    itemRow.getVisit().getStartTime(), timezone));
      } else {
        row.createCell(cellInt++).setCellValue("");
      }

      row.createCell(cellInt++)
          .setCellValue(
              itemRow.getVisit().getCompleteTime() == null
                  ? ""
                  : ZonedDateTimeUtil.getLocalDatetimeWithTimezone(
                      itemRow.getVisit().getCompleteTime(), timezone));
      row.createCell(cellInt++)
          .setCellValue(
              itemRow.getVisit().getStatus() == VisitStatus.UNDONE
                  ? "Unfinished"
                  : itemRow.getVisit().getStatus().toString());
      row.createCell(cellInt++)
          .setCellValue(
              getMessageByCondition(
                  itemRow.getVisit().getDeleted(), "report.yes", "report.no", locale));
      row.createCell(cellInt++)
          .setCellValue(
              itemRow.getVisit().getDistance() == null ? 0 : itemRow.getVisit().getDistance());
      row.createCell(cellInt++).setCellValue(itemRow.getVisit().getLesson().getId());
      row.createCell(cellInt++).setCellValue(itemRow.getVisit().getRemark());
      row.createCell(cellInt++).setCellValue(itemRow.getVisit().getLesson().getName());
      row.createCell(cellInt++).setCellValue(itemRow.getVisit().getLesson().getNumber());
      row.createCell(cellInt++).setCellValue(itemRow.getVisit().getLesson().getDescription());
      row.createCell(cellInt++)
          .setCellValue(
              itemRow.getVisit().getLesson().getStage() == BabyStage.BORN ? "Born" : "Unborn");
      row.createCell(cellInt++)
          .setCellValue(itemRow.getVisit().getLesson().getEndOfApplicableDays());
      row.createCell(cellInt++)
          .setCellValue(itemRow.getVisit().getLesson().getStartOfApplicableDays());

      Curriculum curriculum = itemRow.getVisit().getBaby().getCurriculum();
      if (curriculum != null) {
        row.createCell(cellInt++)
            .setCellValue(itemRow.getVisit().getBaby().getCurriculum().getName());
      } else {
        row.createCell(cellInt++).setCellValue("");
      }

      Module module;
      for (int i = 0; i < 7; i++) {
        if (itemRow.getModules() != null) {
          try {
            module = itemRow.getModules().get(i);
            if (module != null) {
              row.createCell(cellInt++)
                  .setCellValue(module.getNumber() + "/" + getTopicCN(module.getTopic(), locale));
            } else {
              row.createCell(cellInt++).setCellValue("");
            }
          } catch (IndexOutOfBoundsException e) {
            row.createCell(cellInt++).setCellValue("");
          }
        } else {
          row.createCell(cellInt++).setCellValue("");
        }
      }

      QuestionnaireRecord questionnaireRecord;
      for (int i = 0; i < 20; i++) {
        if (itemRow.getQuestionnaireRecords() != null) {
          try {
            questionnaireRecord = itemRow.getQuestionnaireRecords().get(i);
            if (questionnaireRecord != null) {
              row.createCell(cellInt++)
                  .setCellValue(
                      questionnaireRecord.getTitleNo() + "." + questionnaireRecord.getName());
              row.createCell(cellInt++).setCellValue(questionnaireRecord.getAnswer());
            } else {
              row.createCell(cellInt++).setCellValue("");
              row.createCell(cellInt++).setCellValue("");
            }
          } catch (IndexOutOfBoundsException e) {
            row.createCell(cellInt++).setCellValue("");
            row.createCell(cellInt++).setCellValue("");
          }
        } else {
          row.createCell(cellInt++).setCellValue("");
          row.createCell(cellInt++).setCellValue("");
        }
      }

      row.createCell(cellInt++).setCellValue(itemRow.getVisit().getChw().getId());
      row.createCell(cellInt++).setCellValue(itemRow.getVisit().getChw().getChw().getIdentity());
      row.createCell(cellInt++)
          .setCellValue(
              ZonedDateTimeUtil.getLocalDatetimeWithTimezone(
                  itemRow.getVisit().getCreatedAt(), timezone));
      row.createCell(cellInt++)
          .setCellValue(
              userRepository
                  .findOneByUsername(itemRow.getVisit().getCreatedBy())
                  .orElse(User.builder().build())
                  .getRealName());
      row.createCell(cellInt++)
          .setCellValue(
              ZonedDateTimeUtil.getLocalDatetimeWithTimezone(
                  itemRow.getVisit().getLastModifiedAt(), timezone));
      row.createCell(cellInt++).setCellValue(itemRow.getVisit().getLastModifiedBy());

      CommunityHouseWorker chw = itemRow.getVisit().getChw().getChw();
      if (chw != null) {
        row.createCell(cellInt++).setCellValue(itemRow.getVisit().getChw().getChw().getIdentity());

        User supervisor = chw.getSupervisor();
        if (supervisor != null) {
          row.createCell(cellInt++)
              .setCellValue(itemRow.getVisit().getChw().getChw().getSupervisor().getRealName());
        } else {
          row.createCell(cellInt++).setCellValue("");
        }

        if (itemRow.getVisit().getChw().getChw().getTags() != null) {
          row.createCell(cellInt++)
              .setCellValue(
                  new StringListConverter()
                      .convertToDatabaseColumn(itemRow.getVisit().getChw().getChw().getTags()));
        } else {
          row.createCell(cellInt++).setCellValue("");
        }

        row.createCell(cellInt++)
            .setCellValue(
                ZonedDateTimeUtil.getLocalDatetimeWithTimezone(
                    itemRow.getVisit().getChw().getChw().getCreatedAt(), timezone));
        row.createCell(cellInt++).setCellValue(itemRow.getVisit().getChw().getChw().getCreatedBy());
      } else {
        row.createCell(cellInt++).setCellValue("");
        row.createCell(cellInt++).setCellValue("");
        row.createCell(cellInt++).setCellValue("");
        row.createCell(cellInt++).setCellValue("");
        row.createCell(cellInt++).setCellValue("");
      }

      row.createCell(cellInt++).setCellValue(itemRow.getVisit().getBaby().getName());
      row.createCell(cellInt++).setCellValue(itemRow.getVisit().getBaby().getIdentity());
      row.createCell(cellInt++).setCellValue(itemRow.getVisit().getBaby().getGender().toString());
      row.createCell(cellInt++).setCellValue(itemRow.getVisit().getBaby().getLocation());
      row.createCell(cellInt++).setCellValue(itemRow.getVisit().getBaby().getRemark());
      row.createCell(cellInt++).setCellValue(itemRow.getVisit().getBaby().getArea());
      row.createCell(cellInt++)
          .setCellValue(
              itemRow.getVisit().getBaby().getStage() == BabyStage.BORN ? "Born" : "Unborn");

      if (itemRow.getVisit().getBaby().getEdc() != null) {
        row.createCell(cellInt++).setCellValue(itemRow.getVisit().getBaby().getEdc().toString());
      } else {
        row.createCell(cellInt++).setCellValue("");
      }

      if (itemRow.getVisit().getBaby().getBirthday() != null) {
        row.createCell(cellInt++)
            .setCellValue(itemRow.getVisit().getBaby().getBirthday().toString());
      } else {
        row.createCell(cellInt++).setCellValue("");
      }

      row.createCell(cellInt++)
          .setCellValue(itemRow.getVisit().getBaby().getAssistedFood() ? "YES" : "NO");

      if (itemRow.getVisit().getBaby().getFeedingPattern() != null) {
        row.createCell(cellInt++)
            .setCellValue(itemRow.getVisit().getBaby().getFeedingPattern().toString());
      } else {
        row.createCell(cellInt++).setCellValue("");
      }

      if (itemRow.getVisit().getBaby().getActionFromApp() != null) {
        row.createCell(cellInt++)
            .setCellValue(itemRow.getVisit().getBaby().getActionFromApp().toString());
      } else {
        row.createCell(cellInt++).setCellValue("");
      }

      row.createCell(cellInt++).setCellValue(itemRow.getVisit().getBaby().getApproved());
      row.createCell(cellInt++).setCellValue(itemRow.getVisit().getBaby().getCloseAccountReason());
      row.createCell(cellInt++)
          .setCellValue(
              ZonedDateTimeUtil.getLocalDatetimeWithTimezone(
                  itemRow.getVisit().getBaby().getCreatedAt(), timezone));
      ;
      row.createCell(cellInt++)
          .setCellValue(
              userRepository
                  .findOneByUsername(itemRow.getVisit().getBaby().getCreatedBy())
                  .orElse(User.builder().build())
                  .getRealName());

      Carer carer;
      boolean isNextToCell = false;
      if (itemRow.getCarers() != null) {
        carer = itemRow.getCarers().stream().filter(Carer::isMaster).findFirst().orElse(null);
        if (carer != null) {
          row.createCell(cellInt++)
              .setCellValue(
                  carer.getName()
                      + "/"
                      + carer.getPhone()
                      + "/"
                      + carer.getFamilyTies().toString()
                      + "/"
                      + localSource.getMessage("report.primaryCaregiver", null, locale)
                      + "/"
                      + carer.getOtherContact());
          itemRow.getCarers().remove(carer);
        } else {
          setCarers(itemRow, row, cellInt, 4, locale);
          isNextToCell = true;
        }
        if (!isNextToCell) {
          setCarers(itemRow, row, cellInt, 3, locale);
        }
      } else {
        for (int i = 0; i < 4; i++) row.createCell(cellInt++).setCellValue("");
      }

      index++;
    }
  }

  /** Baby Roster */
  public byte[] generateBabyRoster(List<Baby> babies, String timezone) {
    return getBabyReport(babies, this::mapByteArray, timezone);
  }

  private byte[] getBabyReport(
      List<Baby> babies, Function<Workbook, byte[]> saveFileOrOther, String timezone) {
    try (InputStream inputStream =
            getTemplateResourceIO("static/excel/Healthy-Future-Report-Baby.xlsx");
        Workbook workBook = new XSSFWorkbook(inputStream)) {

      addContentRowByBaby(workBook, babies, timezone);

      return saveFileOrOther.apply(workBook);
    } catch (Exception e) {
      log.error("addContentRow ", e);
      throw new RuntimeException("error.excel.report.dataInvalid");
    }
  }

  private void addContentRowByBaby(Workbook workbook, List<Baby> allData, String timezone) {
    Sheet sheet = workbook.getSheetAt(0);
    int index = 1;
    for (Baby itemRow : allData) {
      Row row = sheet.createRow(index);
      int cellInt = 0;

      row.createCell(cellInt++).setCellValue(itemRow.getId());
      row.createCell(cellInt++).setCellValue(itemRow.getIdentity());

      if (itemRow.isDeleted()) {
        row.createCell(cellInt++).setCellValue("Archived");
      } else {
        row.createCell(cellInt++).setCellValue("Enrolled");
      }

      row.createCell(cellInt++).setCellValue(itemRow.getCloseAccountReason());

      BabyStage babyStage = itemRow.getStage();
      if (babyStage.equals(BabyStage.UNBORN)) row.createCell(cellInt++).setCellValue("Unborn");
      else row.createCell(cellInt++).setCellValue("Born");

      row.createCell(cellInt++).setCellValue(itemRow.getName());
      row.createCell(cellInt++).setCellValue(itemRow.getGender().toString());

      LocalDate localDate = itemRow.getBirthday();
      if (localDate != null) {
        row.createCell(cellInt++).setCellValue(localDate.toString());
      } else {
        row.createCell(cellInt++).setCellValue("");
      }

      row.createCell(cellInt++).setCellValue(itemRow.getArea());
      row.createCell(cellInt++).setCellValue(itemRow.getLocation());

      Chw chw = itemRow.getChw();
      if (chw != null) {
        row.createCell(cellInt++).setCellValue(chw.getId());
        row.createCell(cellInt++).setCellValue(chw.getChw().getIdentity());
        row.createCell(cellInt++).setCellValue(chw.getRealName());
        row.createCell(cellInt++).setCellValue(chw.getPhone());
      } else {
        row.createCell(cellInt++).setCellValue("");
        row.createCell(cellInt++).setCellValue("");
        row.createCell(cellInt++).setCellValue("");
        row.createCell(cellInt++).setCellValue("");
      }

      List<Carer> carers = carerRepository.findByBabyIdOrderByMasterDesc(itemRow.getId());
      if (carers != null && carers.size() > 0) {
        for (int i = 0; i < 5; i++) {
          if (i >= carers.size()) {
            // There are no caregivers left
            for (int j = 0; j < 5; j++) row.createCell(cellInt++).setCellValue("");
          } else {
            // Groups of five
            Carer carer = carers.get(i);
            row.createCell(cellInt++).setCellValue(carer.isMaster());
            row.createCell(cellInt++).setCellValue(carer.getName());
            FamilyTies familyTies = carer.getFamilyTies();
            if (familyTies != null) {
              row.createCell(cellInt++).setCellValue(familyTies.toString());
            } else {
              row.createCell(cellInt++).setCellValue("");
            }
            row.createCell(cellInt++).setCellValue(carer.getPhone());
            row.createCell(cellInt++).setCellValue(carer.getOtherContact());
          }
        }
      } else {
        for (int i = 0; i < 5; i++) {
          for (int j = 0; j < 5; j++) row.createCell(cellInt++).setCellValue("");
        }
      }

      row.createCell(cellInt++).setCellValue(itemRow.getRemark());
      row.createCell(cellInt++)
          .setCellValue(itemRow.getEdc() == null ? "" : itemRow.getEdc().toString());
      row.createCell(cellInt++)
          .setCellValue(
              ZonedDateTimeUtil.getLocalDatetimeWithTimezone(itemRow.getCreatedAt(), timezone));
      row.createCell(cellInt++).setCellValue(itemRow.getCreatedBy());
      row.createCell(cellInt++).setCellValue(itemRow.getStage().toString());
      row.createCell(cellInt++).setCellValue(itemRow.getAssistedFood());

      FeedingPattern feedingPattern = itemRow.getFeedingPattern();
      if (feedingPattern != null) {
        row.createCell(cellInt++).setCellValue(feedingPattern.toString());
      } else {
        row.createCell(cellInt++).setCellValue("");
      }

      ActionFromApp actionFromApp = itemRow.getActionFromApp();
      if (actionFromApp != null) row.createCell(cellInt++).setCellValue(actionFromApp.toString());
      else row.createCell(cellInt++).setCellValue("");

      row.createCell(cellInt++).setCellValue(itemRow.getApproved());

      index++;
    }
  }

  /**
   * CHW
   *
   * @param chwList
   * @param timezone
   */
  public byte[] generateChwExcel(List<UnfilteredUser> chwList, String lang, String timezone) {
    return getChwReport(chwList, this::mapByteArray, lang, timezone);
  }

  private byte[] getChwReport(
      List<UnfilteredUser> chwList,
      Function<Workbook, byte[]> saveFileOrOther,
      String lang,
      String timezone) {
    try (InputStream inputStream =
            getTemplateResourceIO("static/excel/Healthy-Future-Report-CHW.xlsx");
        Workbook workBook = new XSSFWorkbook(inputStream)) {

      addContentRowByCommunityHouseWorkers(workBook, chwList, lang, timezone);

      return saveFileOrOther.apply(workBook);
    } catch (Exception e) {
      log.error("addContentRow ", e);
      throw new RuntimeException("error.excel.report.dataInvalid");
    }
  }

  private String getMessageByCondition(
      boolean condition, String trueCode, String falseCode, Locale locale) {
    return localSource.getMessage(condition ? trueCode : falseCode, null, locale);
  }

  private void addContentRowByCommunityHouseWorkers(
      Workbook workbook, List<UnfilteredUser> chwList, String lang, String timezone) {
    Locale locale = "zh".equals(lang) ? Locale.CHINESE : Locale.ENGLISH;

    Function<UnfilteredUser, String> getStatusString =
        u -> getMessageByCondition(u.isDeleted(), "status.deleted", "status.active", locale);

    Sheet sheet = workbook.getSheetAt(0);
    for (int i = 0; i < chwList.size(); i++) {
      UnfilteredUser user = chwList.get(i);
      CommunityHouseWorker chw = user.getChw();
      User supervisor = chw.getSupervisor();
      List<String> tags = user.getChw().getTags();

      Row row = sheet.createRow(i + 1);
      int column = 0;

      // CHW ID
      row.createCell(column++).setCellValue(chw.getIdentity());
      // CHW NAME
      row.createCell(column++).setCellValue(user.getRealName());
      // CHW Phone number
      row.createCell(column++).setCellValue(user.getPhone());
      // CHW Status
      row.createCell(column++).setCellValue(getStatusString.apply(user));
      // CHW System ID
      row.createCell(column++).setCellValue(user.getId());
      // Supervisor ID of this CHW
      Cell supervisorIdCell = row.createCell(column++);
      if (supervisor != null) {
        supervisorIdCell.setCellValue(supervisor.getId());
      }

      // Tag of this CHW's working area
      Cell tagCell = row.createCell(column++);
      if (tags != null) {
        tagCell.setCellValue(String.valueOf(tags));
      }

      // CHW created date
      row.createCell(column++)
          .setCellValue(
              ZonedDateTimeUtil.getLocalDatetimeWithTimezone(user.getCreatedAt(), timezone));
      // CHW created by who
      row.createCell(column).setCellValue(user.getCreatedBy());
    }
  }

  private void setCarers(VisitReportObjData itemRow, Row row, int cellInt, int num, Locale locale) {
    for (int i = 0; i < num; i++) {
      try {
        Carer carer = itemRow.getCarers().get(i);
        if (carer != null && !carer.isMaster()) {
          row.createCell(cellInt++)
              .setCellValue(
                  carer.getName()
                      + "/"
                      + carer.getPhone()
                      + "/"
                      + carer.getFamilyTies().toString()
                      + "/"
                      + localSource.getMessage("report.nonPrimaryCaregiver", null, locale)
                      + "/"
                      + carer.getOtherContact());
        } else {
          row.createCell(cellInt++).setCellValue("");
        }
      } catch (IndexOutOfBoundsException e) {
        row.createCell(cellInt++).setCellValue("");
      }
    }
  }

  private byte[] mapByteArray(Workbook workbook) {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      workbook.write(bos);
      return bos.toByteArray();
    } catch (IOException e) {
      log.error("work book to byte array ", e);
      throw new RuntimeException("work book to byte array " + e.getMessage());
    }
  }

  private InputStream getTemplateResourceIO(String url) {
    ClassPathResource classPathResource = new ClassPathResource(url);
    try {
      return classPathResource.getInputStream();
    } catch (Exception e) {
      log.error("Excel template load failed", e);
      throw new RuntimeException("Excel template load failed");
    }
  }

  public byte[] writeNotStartExcel(List<ExportVisit> visit, String lang, String timezone) {
    return getNotStartDataReport(visit, this::mapByteArray, lang, timezone);
  }

  private byte[] getNotStartDataReport(
      List<ExportVisit> visitReportObjData,
      Function<Workbook, byte[]> saveFileOrOther,
      String lang,
      String timezone) {
    try (InputStream inputStream =
            getTemplateResourceIO("static/excel/Healthy-Future-Report-Not-Finished-Visit.xlsx");
        Workbook workBook = new XSSFWorkbook(inputStream)) {

      addNotStartContentRow(workBook, visitReportObjData, lang, timezone);

      return saveFileOrOther.apply(workBook);
    } catch (Exception e) {
      log.error("addContentRow ", e);
      throw new RuntimeException("error.excel.report.dataInvalid");
    }
  }

  private void addNotStartContentRow(
      Workbook workbook, List<ExportVisit> allData, String lang, String timezone) {
    Sheet sheet = workbook.getSheetAt(0);
    Locale locale = "zh".equals(lang) ? Locale.CHINESE : Locale.ENGLISH;

    int index = 1;
    for (ExportVisit itemRow : allData) {
      Row row = sheet.createRow(index);
      int cellInt = 0;

      row.createCell(cellInt++).setCellValue(itemRow.getId());
      row.createCell(cellInt++)
          .setCellValue(
              ZonedDateTimeUtil.getLocalDatetimeWithTimezone(itemRow.getVisitTime(), timezone));

      if (itemRow.getStartTime() != null) {
        row.createCell(cellInt++)
            .setCellValue(
                ZonedDateTimeUtil.getLocalDatetimeWithTimezone(itemRow.getStartTime(), timezone));
      } else {
        row.createCell(cellInt++).setCellValue("");
      }

      row.createCell(cellInt++)
          .setCellValue(
              itemRow.getCompleteTime() == null
                  ? ""
                  : ZonedDateTimeUtil.getLocalDatetimeWithTimezone(
                      itemRow.getCompleteTime(), timezone));
      row.createCell(cellInt++)
          .setCellValue(
              itemRow.getStatus() == VisitStatus.UNDONE
                  ? "Unfinished"
                  : itemRow.getStatus().toString());
      row.createCell(cellInt++)
          .setCellValue(
              getMessageByCondition(itemRow.getDeleted(), "report.yes", "report.no", locale));
      row.createCell(cellInt++).setCellValue(itemRow.getLesson().getId());
      row.createCell(cellInt++).setCellValue(itemRow.getRemark());
      row.createCell(cellInt++).setCellValue(itemRow.getDeleteReason());
      row.createCell(cellInt++).setCellValue(itemRow.getLesson().getName());
      row.createCell(cellInt++).setCellValue(itemRow.getLesson().getNumber());
      row.createCell(cellInt++).setCellValue(itemRow.getLesson().getDescription());
      row.createCell(cellInt++)
          .setCellValue(itemRow.getLesson().getStage() == BabyStage.BORN ? "Born" : "Unborn");
      row.createCell(cellInt++).setCellValue(itemRow.getLesson().getEndOfApplicableDays());
      row.createCell(cellInt++).setCellValue(itemRow.getLesson().getStartOfApplicableDays());

      Curriculum curriculum = itemRow.getBaby().getCurriculum();
      if (curriculum != null) {
        row.createCell(cellInt++).setCellValue(itemRow.getBaby().getCurriculum().getName());
      } else {
        row.createCell(cellInt++).setCellValue("");
      }

      row.createCell(cellInt++).setCellValue(Integer.toString(itemRow.getNextModuleIndex() + 1));
      Domain module;
      for (int i = 0; i < 7; i++) {
        if (itemRow.getLesson().getModules() != null) {
          try {
            module = itemRow.getLesson().getModules().get(i);
            if (module != null) {
              Module module1 = moduleRepository.findById(Long.parseLong(module.getValue())).get();
              row.createCell(cellInt++)
                  .setCellValue(
                      module1.getNumber().toString()
                          + "/"
                          + getTopicCN(module1.getTopic(), locale));
            } else {
              row.createCell(cellInt++).setCellValue("");
            }
          } catch (IndexOutOfBoundsException e) {
            row.createCell(cellInt++).setCellValue("");
          }
        } else {
          row.createCell(cellInt++).setCellValue("");
        }
      }

      for (int i = 0; i < 20; i++) {
        row.createCell(cellInt++).setCellValue("");
        row.createCell(cellInt++).setCellValue("");
      }

      if (itemRow.getChw() != null) {
        row.createCell(cellInt++).setCellValue(itemRow.getChw().getId());
        row.createCell(cellInt++).setCellValue(itemRow.getChw().getChw().getIdentity());
      } else {
        row.createCell(cellInt++).setCellValue("");
        row.createCell(cellInt++).setCellValue("");
      }
      row.createCell(cellInt++)
          .setCellValue(
              ZonedDateTimeUtil.getLocalDatetimeWithTimezone(itemRow.getCreatedAt(), timezone));
      row.createCell(cellInt++)
          .setCellValue(
              userRepository
                  .findOneByUsername(itemRow.getCreatedBy())
                  .orElse(User.builder().build())
                  .getRealName());
      row.createCell(cellInt++)
          .setCellValue(
              ZonedDateTimeUtil.getLocalDatetimeWithTimezone(
                  itemRow.getLastModifiedAt(), timezone));
      row.createCell(cellInt++).setCellValue(itemRow.getLastModifiedBy());

      if (itemRow.getChw() != null) {
        CommunityHouseWorker chw = itemRow.getChw().getChw();
        row.createCell(cellInt++).setCellValue(itemRow.getChw().getChw().getIdentity());

        User supervisor = chw.getSupervisor();
        if (supervisor != null) {
          row.createCell(cellInt++)
              .setCellValue(itemRow.getChw().getChw().getSupervisor().getRealName());
        } else {
          row.createCell(cellInt++).setCellValue("");
        }

        if (itemRow.getChw().getChw().getTags() != null) {
          row.createCell(cellInt++)
              .setCellValue(
                  new StringListConverter()
                      .convertToDatabaseColumn(itemRow.getChw().getChw().getTags()));
        } else {
          row.createCell(cellInt++).setCellValue("");
        }

        row.createCell(cellInt++)
            .setCellValue(
                ZonedDateTimeUtil.getLocalDatetimeWithTimezone(
                    itemRow.getChw().getChw().getCreatedAt(), timezone));
        row.createCell(cellInt++).setCellValue(itemRow.getChw().getChw().getCreatedBy());
      } else {
        row.createCell(cellInt++).setCellValue("");
        row.createCell(cellInt++).setCellValue("");
        row.createCell(cellInt++).setCellValue("");
        row.createCell(cellInt++).setCellValue("");
        row.createCell(cellInt++).setCellValue("");
      }

      row.createCell(cellInt++).setCellValue(itemRow.getBaby().getName());
      row.createCell(cellInt++).setCellValue(itemRow.getBaby().getIdentity());
      row.createCell(cellInt++).setCellValue(itemRow.getBaby().getGender().toString());
      row.createCell(cellInt++).setCellValue(itemRow.getBaby().getLocation());
      row.createCell(cellInt++).setCellValue(itemRow.getBaby().getRemark());
      row.createCell(cellInt++).setCellValue(itemRow.getBaby().getArea());
      row.createCell(cellInt++)
          .setCellValue(itemRow.getBaby().getStage() == BabyStage.BORN ? "Born" : "Unborn");

      if (itemRow.getBaby().getEdc() != null) {
        row.createCell(cellInt++).setCellValue(itemRow.getBaby().getEdc().toString());
      } else {
        row.createCell(cellInt++).setCellValue("");
      }

      if (itemRow.getBaby().getBirthday() != null) {
        row.createCell(cellInt++).setCellValue(itemRow.getBaby().getBirthday().toString());
      } else {
        row.createCell(cellInt++).setCellValue("");
      }

      row.createCell(cellInt++).setCellValue(itemRow.getBaby().getAssistedFood() ? "YES" : "NO");

      if (itemRow.getBaby().getFeedingPattern() != null) {
        row.createCell(cellInt++).setCellValue(itemRow.getBaby().getFeedingPattern().toString());
      } else {
        row.createCell(cellInt++).setCellValue("");
      }

      if (itemRow.getBaby().getActionFromApp() != null) {
        row.createCell(cellInt++).setCellValue(itemRow.getBaby().getActionFromApp().toString());
      } else {
        row.createCell(cellInt++).setCellValue("");
      }

      row.createCell(cellInt++).setCellValue(itemRow.getBaby().getApproved());
      row.createCell(cellInt++).setCellValue(itemRow.getBaby().getCloseAccountReason());
      row.createCell(cellInt++)
          .setCellValue(
              ZonedDateTimeUtil.getLocalDatetimeWithTimezone(
                  itemRow.getBaby().getCreatedAt(), timezone));
      row.createCell(cellInt++)
          .setCellValue(
              userRepository
                  .findOneByUsername(itemRow.getBaby().getCreatedBy())
                  .orElse(User.builder().build())
                  .getRealName());

      Carer carer;
      boolean isNextToCell = false;
      List<Carer> carerList = carerRepository.findAllByBabyId(itemRow.getBaby().getId());
      if (carerList.size() != 0) {
        carer = carerList.stream().filter(Carer::isMaster).findFirst().orElse(null);
        if (carer != null) {
          row.createCell(cellInt++)
              .setCellValue(
                  carer.getName()
                      + "/"
                      + carer.getPhone()
                      + "/"
                      + carer.getFamilyTies().toString()
                      + "/"
                      + localSource.getMessage("report.primaryCaregiver", null, locale)
                      + "/"
                      + carer.getOtherContact());
          carerList.remove(carer);
        } else {
          setNotStartCarers(carerList, row, cellInt, 4, locale);
          isNextToCell = true;
        }
        if (!isNextToCell) {
          setNotStartCarers(carerList, row, cellInt, 3, locale);
        }
      } else {
        for (int i = 0; i < 4; i++) row.createCell(cellInt++).setCellValue("");
      }
      index++;
    }
  }

  private void setNotStartCarers(
      List<Carer> carerList, Row row, int cellInt, int num, Locale locale) {
    for (int i = 0; i < num; i++) {
      try {
        Carer carer = carerList.get(i);
        if (carer != null && !carer.isMaster()) {
          row.createCell(cellInt++)
              .setCellValue(
                  carer.getName()
                      + "/"
                      + carer.getPhone()
                      + "/"
                      + carer.getFamilyTies().toString()
                      + "/"
                      + localSource.getMessage("report.nonPrimaryCaregiver", null, locale)
                      + "/"
                      + carer.getOtherContact());
        } else {
          row.createCell(cellInt++).setCellValue("");
        }
      } catch (IndexOutOfBoundsException e) {
        row.createCell(cellInt++).setCellValue("");
      }
    }
  }

  private String getTopicCN(ModuleTopic topic, Locale locale) {
    switch (topic) {
      case MOTHER_NUTRITION:
        return localSource.getMessage("report.module.motherNutrition", null, locale);
      case BREASTFEEDING:
        return localSource.getMessage("report.module.breastfeeding", null, locale);
      case BABY_FOOD:
        return localSource.getMessage("report.module.babyFood", null, locale);
      case INFANT_INJURY_AND_PREVENTION:
        return localSource.getMessage("report.module.infantInjuryAndPrevention", null, locale);
      case CAREGIVER_MENTAL_HEALTH:
        return localSource.getMessage("report.module.caregiverMentalHealth", null, locale);
      case GOVERNMENT_SERVICES:
        return localSource.getMessage("report.module.governmentServices", null, locale);
      case KNOWLEDGE_ATTITUDE_TEST:
        return localSource.getMessage("report.module.knowledgeAttitudeTest", null, locale);
      default:
        return localSource.getMessage("report.module.other", null, locale);
    }
  }

  public void importBabyLocations(MultipartFile records) {
    try (Workbook workBook = new XSSFWorkbook(records.getInputStream())) {
      handleBabyRecordRow(workBook.getSheetAt(0));
    } catch (Exception e) {
      log.error("importContentRow ", e);
      throw new RuntimeException("Import failed, server error, data may be corrupted!");
    }
  }

  @Transactional
  public void importChws(MultipartFile records, String lang) {
    try (Workbook workbook = new XSSFWorkbook(records.getInputStream())) {
      handleChwRecordRow(workbook, lang);
    } catch (Exception e) {
      log.error("importContentRow ", e);
    }
  }

  private void handleBabyRecordRow(Sheet sheet) {
    int endRow = sheet.getLastRowNum() + 1;
    if (endRow <= 1) {
      return;
    }
    Set<Long> babyIds = new HashSet<>();
    Map<Long, List<Double>> babyLocationMap = new HashMap<>();
    for (int i = 1; i < endRow; i++) {
      Row row = sheet.getRow(i);
      if (row != null) {
        if (null != row.getCell(0) && null != row.getCell(1) && null != row.getCell(2)) {
          Long babyId = Long.valueOf(row.getCell(0).getStringCellValue());
          double longitude = row.getCell(1).getNumericCellValue();
          double latitude = row.getCell(2).getNumericCellValue();
          babyLocationMap.put(2L, Lists.newArrayList(longitude, latitude));
          babyIds.add(babyId);
        }
      }
    }
    List<Baby> selectBabies = babyRepository.findAllById(babyIds);
    if (!CollectionUtils.isEmpty(selectBabies)) {
      for (Baby baby : selectBabies) {
        if (babyLocationMap.containsKey(baby.getId())) {
          List<Double> locations = babyLocationMap.get(baby.getId());
          if (locations.size() == 2) {
            baby.setLongitude(locations.get(0));
            baby.setLatitude(locations.get(1));
            baby.setShowLocation(true);
            babyRepository.save(baby);
          }
        }
      }
    }
  }

  private ErrDTO getLocaleDTO(
      String name, Integer number, String code, Locale locale, Object... args) {
    ErrDTO errDTO = new ErrDTO();
    errDTO.setMatters(localSource.getMessage(code, args, locale));
    errDTO.setName(name);
    errDTO.setNumber(number);
    return errDTO;
  }

  private void handleChwRecordRow(Workbook workbook, String lang) {
    Sheet sheet = workbook.getSheetAt(0);
    FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

    int endRow = sheet.getLastRowNum() + 1;
    for (int i = 2; i < endRow - 1; i++) {
      if (StringUtils.isEmpty(sheet.getRow(i).getCell(0).getStringCellValue())) {
        break;
      }
      Row row = sheet.getRow(i);
      String realName = safeGetStringCellValue(row.getCell(0), evaluator);
      String identity = safeGetStringCellValue(row.getCell(1), evaluator);
      String tag = safeGetStringCellValue(row.getCell(2), evaluator);
      String phone = safeGetStringCellValue(row.getCell(3), evaluator);
      String username = safeGetStringCellValue(row.getCell(4), evaluator);
      String password = safeGetStringCellValue(row.getCell(5), evaluator);

      String[] split = tag.split(";");
      String encryptedPassword = passwordEncoder.encode(password);
      List<String> tags = Arrays.asList(split);

      if (!StringUtils.isEmpty(realName)
          && realName.matches(RegexConstant.NAME_REGEX)
          && !StringUtils.isEmpty(identity)
          && !communityHouseWorkerRepository.findFirstByIdentity(identity).isPresent()
          && !StringUtils.isEmpty(tag)
          && !StringUtils.isEmpty(phone)
          && phone.matches(RegexConstant.PHONE_REGEX)
          && !StringUtils.isEmpty(username)
          && !userRepository.findOneByUsername(username).isPresent()
          && !StringUtils.isEmpty(password)) {
        CommunityHouseWorker communityHouseWorker =
            new CommunityHouseWorker(null, identity, tags, null);
        communityHouseWorker.setProjectId(SecurityUtils.getProjectId());
        tagService.saveAll(tags);
        chwRepository.save(communityHouseWorker);
        User chw =
            new User(
                null,
                username,
                encryptedPassword,
                realName,
                phone,
                "ROLE_CHW",
                LocalDateTime.now(),
                communityHouseWorker);
        chw.setProjectId(SecurityUtils.getProjectId());
        userRepository.save(chw);
      }
    }
  }

  public Map<String, Object> checkChws(MultipartFile records, String lang) {
    try (Workbook workBook = new XSSFWorkbook(records.getInputStream())) {
      return checkChwRecordRow(workBook, lang);
    } catch (Exception e) {
      log.error("importContentRow ", e);
      throw new BadRequestAlertException("error.excel.dataInvalid");
    }
  }

  private Map<String, Object> checkChwRecordRow(Workbook workbook, String lang) {
    Sheet sheet = workbook.getSheetAt(0);
    FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
    Locale locale = "zh".equals(lang) ? Locale.CHINESE : Locale.ENGLISH;
    Map<String, Object> map = new HashMap<>();

    List<ErrDTO> errDTOS = new ArrayList<>();
    int endRow = sheet.getPhysicalNumberOfRows() + 1;
    if (endRow <= 1) {
      return map;
    }

    int a = 0;
    for (int i = 2; i < endRow - 1; i++) {
      Row row = sheet.getRow(i);
      if (row == null) {
        continue;
      }
      String realName = safeGetStringCellValue(row.getCell(0), evaluator);
      String identity = safeGetStringCellValue(row.getCell(1), evaluator);
      String tag = safeGetStringCellValue(row.getCell(2), evaluator);
      String phone = safeGetStringCellValue(row.getCell(3), evaluator);
      String username = safeGetStringCellValue(row.getCell(4), evaluator);
      String password = safeGetStringCellValue(row.getCell(5), evaluator);

      if (Stream.of(realName, identity, tag, phone, username, password)
          .allMatch(StringUtils::isEmpty)) {
        break;
      }

      a++;
      if (StringUtils.isEmpty(realName)) {
        errDTOS.add(getLocaleDTO(realName, (i - 1), "error.excel.chw.name", locale));
        continue;
      }
      if (!realName.matches(RegexConstant.NAME_REGEX)) {
        errDTOS.add(getLocaleDTO(realName, (i - 1), "error.excel.chw.name", locale));
        continue;
      }
      if (StringUtils.isEmpty(identity)) {
        errDTOS.add(getLocaleDTO(realName, (i - 1), "error.excel.chw.chwId", locale));
        continue;
      }
      if (communityHouseWorkerRepository.findFirstByIdentity(identity).isPresent()) {
        errDTOS.add(
            getLocaleDTO(realName, (i - 1), "error.excel.chw.chwIdExist", locale, identity));
        continue;
      }
      if (StringUtils.isEmpty(tag)) {
        errDTOS.add(getLocaleDTO(realName, (i - 1), "error.excel.chw.area", locale));
        continue;
      }
      String[] tags = tag.split(";");
      if (tags.length > 3) {
        errDTOS.add(getLocaleDTO(realName, (i - 1), "error.excel.chw.areaInvalid", locale));
        continue;
      }
      if (!Arrays.stream(tags).allMatch(part -> part.length() <= 100)) {
        errDTOS.add(getLocaleDTO(realName, (i - 1), "error.excel.chw.areaLengthInvalid", locale));
        continue;
      }
      if (StringUtils.isEmpty(phone)) {
        errDTOS.add(getLocaleDTO(realName, (i - 1), "error.excel.chw.phone", locale));
        continue;
      }
      if (!phone.matches(RegexConstant.PHONE_REGEX)) {
        errDTOS.add(getLocaleDTO(realName, (i - 1), "error.excel.chw.phoneInvalid", locale));
        continue;
      }
      if (StringUtils.isEmpty(username)) {
        errDTOS.add(getLocaleDTO(realName, (i - 1), "error.excel.chw.username", locale));
        continue;
      }
      if (userRepository.findOneByUsername(username).isPresent()) {
        errDTOS.add(
            getLocaleDTO(realName, (i - 1), "error.excel.chw.usernameExist", locale, username));
        continue;
      }

      if (StringUtils.isEmpty(password)) {
        errDTOS.add(getLocaleDTO(realName, (i - 1), "error.excel.chw.password", locale));
      }
    }
    map.put("errData", errDTOS);
    map.put("total", a);
    return map;
  }

  /**
   * Safely retrieves the string value of a cell, handling different cell types such as string,
   * numeric, boolean, and formula. This method is useful as `setCellType` is deprecated, and it
   * provides a way to extract cell values in a consistent string format.
   *
   * @param cell The Excel cell from which to retrieve the value.
   * @param evaluator The FormulaEvaluator used to evaluate formula cells.
   * @return The string representation of the cell's value. Returns an empty string if the cell is
   *     null or of an unsupported type.
   */
  private String safeGetStringCellValue(Cell cell, FormulaEvaluator evaluator) {
    if (cell == null) {
      return "";
    }

    DataFormatter formatter = new DataFormatter();

    switch (cell.getCellType()) {
      case STRING:
        return cell.getStringCellValue();

      case NUMERIC:
        return formatter.formatCellValue(cell);

      case BOOLEAN:
        return cell.getBooleanCellValue() ? "1.0" : "0.0";

      case FORMULA:
        return formatter.formatCellValue(cell, evaluator);

      default:
        return "";
    }
  }
}
