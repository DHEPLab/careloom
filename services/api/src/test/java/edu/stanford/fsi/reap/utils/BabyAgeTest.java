package edu.stanford.fsi.reap.utils;

import static org.junit.jupiter.api.Assertions.*;

import edu.stanford.fsi.reap.entity.enumerations.BabyStage;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class BabyAgeTest {

  @Test
  void should_beyond_days_of_pregnancy() {
    LocalDate edc = LocalDate.of(2020, 1, 1);
    LocalDate baseline = LocalDate.of(2020, 1, 2);
    int days = BabyAge.daysOfPregnancy(baseline, edc);
    assertEquals(281, days);
  }

  @Test
  public void should_compute_baby_days_of_pregnancy() {
    LocalDate edc = LocalDate.of(2020, 1, 3);
    LocalDate baseline = LocalDate.of(2020, 1, 2);
    int days = BabyAge.daysOfPregnancy(baseline, edc);
    assertEquals(279, days);

    edc = LocalDate.of(2020, 10, 2);
    baseline = LocalDate.of(2020, 1, 2);
    days = BabyAge.daysOfPregnancy(baseline, edc);
    assertEquals(6, days);

    edc = LocalDate.of(2020, 10, 2);
    baseline = LocalDate.of(2020, 1, 12);
    days = BabyAge.daysOfPregnancy(baseline, edc);
    assertEquals(16, days);

    edc = LocalDate.of(2020, 1, 1);
    baseline = LocalDate.of(2019, 12, 30);
    days = BabyAge.daysOfPregnancy(baseline, edc);
    assertEquals(278, days);
  }

  @Test
  public void should_compute_baby_days_of_birth() {
    LocalDate birthday = LocalDate.of(2020, 1, 2);
    LocalDate baseline = LocalDate.of(2020, 1, 1);
    int days = BabyAge.daysOfBirth(baseline, birthday);
    assertEquals(1, days);

    birthday = LocalDate.of(2020, 1, 1);
    baseline = LocalDate.of(2020, 2, 2);
    days = BabyAge.daysOfBirth(baseline, birthday);
    assertEquals(32, days);

    birthday = LocalDate.of(2019, 12, 30);
    baseline = LocalDate.of(2020, 2, 2);
    days = BabyAge.daysOfBirth(baseline, birthday);
    assertEquals(34, days);
  }

  @Test
  public void should_compute_baby_months_of_pregnancy() {
    LocalDate edc = LocalDate.of(2020, 1, 1);
    LocalDate baseline = LocalDate.of(2020, 1, 2);
    int month = BabyAge.monthsOfPregnancy(baseline, edc);
    assertEquals(11, month);

    edc = LocalDate.of(2020, 1, 3);
    baseline = LocalDate.of(2020, 1, 2);
    month = BabyAge.monthsOfPregnancy(baseline, edc);
    assertEquals(10, month);

    edc = LocalDate.of(2020, 10, 2);
    baseline = LocalDate.of(2020, 1, 2);
    month = BabyAge.monthsOfPregnancy(baseline, edc);
    assertEquals(1, month);

    edc = LocalDate.of(2020, 10, 2);
    baseline = LocalDate.of(2020, 1, 12);
    month = BabyAge.monthsOfPregnancy(baseline, edc);
    assertEquals(2, month);

    edc = LocalDate.of(2020, 1, 1);
    baseline = LocalDate.of(2019, 12, 30);
    month = BabyAge.monthsOfPregnancy(baseline, edc);
    assertEquals(10, month);
  }

  @Test
  public void should_compute_baby_months_of_birth() {
    LocalDate birthday = LocalDate.of(2020, 1, 2);
    LocalDate baseline = LocalDate.of(2020, 1, 1);
    int month = BabyAge.monthsOfBirth(baseline, birthday);
    assertEquals(1, month);

    birthday = LocalDate.of(2020, 1, 1);
    baseline = LocalDate.of(2020, 2, 2);
    month = BabyAge.monthsOfBirth(baseline, birthday);
    assertEquals(2, month);

    birthday = LocalDate.of(2019, 12, 30);
    baseline = LocalDate.of(2020, 2, 2);
    month = BabyAge.monthsOfBirth(baseline, birthday);
    assertEquals(2, month);
  }

  @Test
  public void should_past_edc() {
    assertTrue(
        BabyAge.pastEdc(BabyStage.UNBORN, LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 2)));
  }

  @Test
  public void should_not_past_edc() {
    assertFalse(
        BabyAge.pastEdc(BabyStage.BORN, LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 2)));
    assertFalse(
        BabyAge.pastEdc(BabyStage.UNBORN, LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 1)));
  }
}
