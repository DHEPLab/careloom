package edu.stanford.fsi.reap.utils;

import edu.stanford.fsi.reap.entity.Baby;
import edu.stanford.fsi.reap.entity.enumerations.BabyStage;
import java.time.LocalDate;

/**
 * @author hookszhang
 */
public class BabyAge {

  /** The day of month */
  public static final int DAY_OF_MONTH = 30;

  /** The day of year */
  public static final int DAY_OF_YEAR = 365;

  /** Total pregnancy cycle */
  public static final int DAY_OF_PREGNANCY = 280;

  public static final int DAY_OF_FIRST_MONTH_FOR_STAGE_EDC = 10;

  public static int dayOfOffset(BabyStage stage) {
    if (!BabyStage.UNBORN.equals(stage)) {
      return 0;
    }
    return DAY_OF_MONTH - DAY_OF_FIRST_MONTH_FOR_STAGE_EDC;
  }

  /**
   * Compute the number of days of pregnancy
   *
   * @param baseline now
   * @param edc expected date of confinement
   * @return The number of days of pregnancy
   */
  public static int daysOfPregnancy(LocalDate baseline, LocalDate edc) {
    int intervalYears = edc.getYear() - baseline.getYear();
    int intervalDays = edc.getDayOfYear() - baseline.getDayOfYear() + (intervalYears * DAY_OF_YEAR);
    int dayOfPregnancy = DAY_OF_PREGNANCY - intervalDays;

    if (dayOfPregnancy < 0) {
      return 1;
    }

    return dayOfPregnancy;
  }

  /**
   * Compute the number of days of birth
   *
   * @param baseline now
   * @param birthday baby birthday
   * @return the number of months of birth, min 1
   */
  public static int daysOfBirth(LocalDate baseline, LocalDate birthday) {
    if (baseline.isBefore(birthday)) return 1;

    int intervalYears = baseline.getYear() - birthday.getYear();
    return baseline.getDayOfYear() - birthday.getDayOfYear() + (intervalYears * DAY_OF_YEAR);
  }

  public static int monthsOfBirth(LocalDate baseline, LocalDate birthday) {
    int days = daysOfBirth(baseline, birthday);
    int months = days / DAY_OF_MONTH;
    if (days % DAY_OF_MONTH > 0) {
      months++;
    }
    return months;
  }

  public static int monthsOfPregnancy(LocalDate baseline, LocalDate edc) {
    int days = daysOfPregnancy(baseline, edc);
    if (days <= 10) {
      // Less than a month
      return 1;
    } else {
      // The first month only counts as 10 days because the total pregnancy cycle is 280 days
      days -= 10;
    }

    int months = days / DAY_OF_MONTH;
    if ((days % DAY_OF_MONTH) > 0) {
      months++;
    }

    // add first month
    return months + 1;
  }

  public static int months(Baby baby, LocalDate baseline) {
    if (BabyStage.UNBORN.equals(baby.getStage())) {
      return monthsOfPregnancy(baseline, baby.getEdc());
    }
    return monthsOfBirth(baseline, baby.getBirthday());
  }

  public static int days(Baby baby, LocalDate baseline) {
    if (BabyStage.UNBORN.equals(baby.getStage())) {
      return daysOfPregnancy(baseline, baby.getEdc());
    }
    return daysOfBirth(baseline, baby.getBirthday());
  }

  public static boolean pastEdc(BabyStage stage, LocalDate edc, LocalDate now) {
    if (BabyStage.BORN.equals(stage)) return false;
    return edc.isBefore(now);
  }
}
