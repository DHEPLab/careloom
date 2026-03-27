package edu.stanford.fsi.reap.utils;

import edu.stanford.fsi.reap.entity.Lesson;
import edu.stanford.fsi.reap.entity.LessonSchedule;
import java.util.List;
import java.util.stream.Collectors;

public class Diff {
  public static List<Lesson> deletedLessons(List<Lesson> newLessons, List<Lesson> oldLessons) {
    return oldLessons.stream()
        .filter(oldLesson -> newLessons.stream().noneMatch(oldLesson::equalsId))
        .collect(Collectors.toList());
  }

  public static List<LessonSchedule> deletedLessonSchedules(
      List<LessonSchedule> newSchedules, List<LessonSchedule> oldSchedules) {
    return oldSchedules.stream()
        .filter(oldSchedule -> newSchedules.stream().noneMatch(oldSchedule::equalsId))
        .collect(Collectors.toList());
  }
}
