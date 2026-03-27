package edu.stanford.fsi.reap.utils;

import edu.stanford.fsi.reap.entity.ExportLesson;
import edu.stanford.fsi.reap.entity.Lesson;
import edu.stanford.fsi.reap.entity.LessonSchedule;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class DateRange {

  /**
   * If it's pass 21:00, the baseline changes to tomorrow
   *
   * @deprecated do noting, This logic has been cancelled at Wed Nov 25 CST 2020
   * @param baseline
   * @param now
   * @return
   */
  @Deprecated
  public static LocalDate checkBaseline(LocalDate baseline, LocalDateTime now) {
    //    if (now.getHour() > 20) {
    //      baseline = baseline.plusDays(1);
    //    }
    return baseline;
  }

  /**
   * The daily deadline for creating home visits is 21:00
   *
   * @param visitTime
   * @param now
   * @return
   */
  public static boolean pastTodayDeadline(LocalDateTime visitTime, LocalDateTime now) {
    if (visitTime.getYear() == now.getYear()
        && visitTime.getMonthValue() == now.getMonthValue()
        && visitTime.getDayOfMonth() == now.getDayOfMonth()) {
      return now.getHour() > 20;
    }
    return false;
  }

  public static List<LocalDate> visit(
      LessonSchedule schedule, Lesson lesson, int daysOfBaby, LocalDate baseline) {
    int scheduleEnd = schedule.getEndOfApplicableDays();
    int lessonBegin = lesson.getStartOfApplicableDays();
    int lessonEnd = lesson.getEndOfApplicableDays();

    // default, lesson begin < days of baby <= lesson end
    int beginOffset = 0;
    int endOffset = lessonEnd - daysOfBaby;

    // days of baby < lesson begin < lesson end
    if (daysOfBaby < lessonBegin) {
      beginOffset = lessonBegin - daysOfBaby;
    }

    // lesson begin < lesson end < days of baby, end time offset lesson expiry date
    // and cannot exceed schedule validity period
    if (lessonEnd < daysOfBaby) {
      endOffset = lessonEnd - lessonBegin;

      if (scheduleEnd < daysOfBaby + endOffset) {
        endOffset = scheduleEnd - daysOfBaby;
      }
    }

    return offset(beginOffset, endOffset, baseline);
  }

  private static List<LocalDate> offset(int begin, int end, LocalDate baseline) {
    return Arrays.asList(baseline.plusDays(begin), baseline.plusDays(end));
  }

  /**
   * Determines whether the date is within range and contains equal to the begin or equal to the end
   *
   * @param range Date Range, Begin and End
   * @param date
   * @return
   */
  public static boolean includes(List<LocalDate> range, LocalDate date) {
    LocalDate begin = range.get(0);
    LocalDate end = range.get(1);
    return (begin.isBefore(date) || begin.isEqual(date))
        && (end.isAfter(date) || end.isEqual(date));
  }

  public static List<LocalDate> visit(
      LessonSchedule schedule, ExportLesson lesson, int daysOfBaby, LocalDate baseline) {
    int scheduleEnd = schedule.getEndOfApplicableDays();
    int lessonBegin = lesson.getStartOfApplicableDays();
    int lessonEnd = lesson.getEndOfApplicableDays();

    // default, lesson begin < days of baby <= lesson end
    int beginOffset = 0;
    int endOffset = lessonEnd - daysOfBaby;

    // days of baby < lesson begin < lesson end
    if (daysOfBaby < lessonBegin) {
      beginOffset = lessonBegin - daysOfBaby;
    }

    // lesson begin < lesson end < days of baby, end time offset lesson expiry date
    // and cannot exceed schedule validity period
    if (lessonEnd < daysOfBaby) {
      endOffset = lessonEnd - lessonBegin;

      if (scheduleEnd < daysOfBaby + endOffset) {
        endOffset = scheduleEnd - daysOfBaby;
      }
    }

    return offset(beginOffset, endOffset, baseline);
  }

  public static boolean contains(List<LocalDate> range, LocalDate visitDate) {
    LocalDate start = range.get(0);
    LocalDate end = range.get(1);
    return (start.isBefore(visitDate) || start.isEqual(visitDate))
        && (end.isAfter(visitDate) || end.isEqual(visitDate));
  }
}
