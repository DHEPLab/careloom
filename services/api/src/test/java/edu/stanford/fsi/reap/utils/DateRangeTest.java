package edu.stanford.fsi.reap.utils;

import static org.junit.jupiter.api.Assertions.*;

import edu.stanford.fsi.reap.entity.Lesson;
import edu.stanford.fsi.reap.entity.LessonSchedule;
import edu.stanford.fsi.reap.entity.enumerations.BabyStage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class DateRangeTest {

  @Test
  public void should_return_edc_stage_visit_date_range() {
    LessonSchedule schedule =
        LessonSchedule.builder()
            .stage(BabyStage.UNBORN)
            .startOfApplicableDays(270)
            .endOfApplicableDays(280)
            .build();
    Lesson lesson = Lesson.builder().startOfApplicableDays(221).endOfApplicableDays(250).build();
    LocalDate baseline = LocalDate.of(2020, 7, 24);
    List<LocalDate> localDates =
        DateRange.visit(
            schedule,
            lesson,
            BabyAge.daysOfPregnancy(baseline, LocalDate.of(2020, 8, 11)),
            baseline);
    assertArrayEquals(
        new LocalDate[] {LocalDate.of(2020, 7, 24), LocalDate.of(2020, 8, 11)},
        localDates.toArray());
  }

  @Test
  public void should_return_visit_date_range() {
    LessonSchedule schedule =
        LessonSchedule.builder()
            .stage(BabyStage.BORN)
            .startOfApplicableDays(90)
            .endOfApplicableDays(150)
            .build();
    Lesson lesson = Lesson.builder().startOfApplicableDays(110).endOfApplicableDays(120).build();
    LocalDate baseline = LocalDate.of(2020, 1, 20);
    List<LocalDate> localDates = DateRange.visit(schedule, lesson, 115, baseline);
    assertArrayEquals(
        new LocalDate[] {LocalDate.of(2020, 1, 20), LocalDate.of(2020, 1, 25)},
        localDates.toArray());

    localDates = DateRange.visit(schedule, lesson, 100, baseline);
    assertArrayEquals(
        new LocalDate[] {LocalDate.of(2020, 1, 30), LocalDate.of(2020, 2, 9)},
        localDates.toArray());

    localDates = DateRange.visit(schedule, lesson, 122, baseline);
    assertArrayEquals(
        new LocalDate[] {LocalDate.of(2020, 1, 20), LocalDate.of(2020, 1, 30)},
        localDates.toArray());

    localDates = DateRange.visit(schedule, lesson, 145, baseline);
    assertArrayEquals(
        new LocalDate[] {LocalDate.of(2020, 1, 20), LocalDate.of(2020, 1, 25)},
        localDates.toArray());
  }

  @Test
  public void should_includes() {
    List<LocalDate> range = Arrays.asList(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 3));
    assertTrue(DateRange.includes(range, LocalDate.of(2020, 1, 1)));
    assertTrue(DateRange.includes(range, LocalDate.of(2020, 1, 2)));
    assertTrue(DateRange.includes(range, LocalDate.of(2020, 1, 3)));
  }

  @Test
  public void should_not_includes() {
    List<LocalDate> range = Arrays.asList(LocalDate.of(2020, 1, 2), LocalDate.of(2020, 1, 3));
    assertFalse(DateRange.includes(range, LocalDate.of(2020, 1, 1)));
    assertFalse(DateRange.includes(range, LocalDate.of(2020, 1, 4)));
  }

  @Test
  @Disabled
  public void should_change_baseline_to_tomorrow() {
    LocalDate baseline = LocalDate.of(2020, 1, 2);
    assertEquals(
        LocalDate.of(2020, 1, 3),
        DateRange.checkBaseline(baseline, LocalDateTime.of(2020, 1, 2, 21, 0)));
  }

  @Test
  @Disabled
  public void should_keep_baseline() {
    LocalDate baseline = LocalDate.of(2020, 1, 2);
    assertEquals(
        LocalDate.of(2020, 1, 2),
        DateRange.checkBaseline(baseline, LocalDateTime.of(2020, 1, 2, 20, 59)));
  }

  @Test
  public void should_pass_today_deadline() {
    assertTrue(
        DateRange.pastTodayDeadline(
            LocalDateTime.of(2020, 1, 2, 20, 59), LocalDateTime.of(2020, 1, 2, 21, 0)));
  }

  @Test
  public void should_not_pass_today_deadline() {
    assertFalse(
        DateRange.pastTodayDeadline(
            LocalDateTime.of(2020, 1, 2, 20, 59), LocalDateTime.of(2020, 1, 2, 20, 59)));
    assertFalse(
        DateRange.pastTodayDeadline(
            LocalDateTime.of(2020, 1, 3, 20, 59), LocalDateTime.of(2020, 1, 2, 20, 59)));
  }

  @Test
  public void should_date_range_contain_visit_date() {
    List<LocalDate> range =
        Arrays.asList(LocalDate.parse("2021-05-13"), LocalDate.parse("2021-05-29"));
    assertTrue(DateRange.contains(range, LocalDate.parse("2021-05-15")));
    assertTrue(DateRange.contains(range, LocalDate.parse("2021-05-13")));
    assertTrue(DateRange.contains(range, LocalDate.parse("2021-05-29")));
  }
}
