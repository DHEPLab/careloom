package edu.stanford.fsi.reap.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import edu.stanford.fsi.reap.entity.Lesson;
import edu.stanford.fsi.reap.entity.LessonSchedule;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class DiffTest {

  @Test
  public void should_diff_deleted_lesson() {
    List<Lesson> deleted =
        Diff.deletedLessons(
            Arrays.asList(Lesson.builder().id(1L).build(), Lesson.builder().id(null).build()),
            Arrays.asList(Lesson.builder().id(1L).build(), Lesson.builder().id(2L).build()));
    assertArrayEquals(new Lesson[] {Lesson.builder().id(2L).build()}, deleted.toArray());
  }

  @Test
  public void should_diff_deleted_schedule() {
    List<LessonSchedule> deleted =
        Diff.deletedLessonSchedules(
            Arrays.asList(
                LessonSchedule.builder().id(1L).build(), LessonSchedule.builder().id(null).build()),
            Arrays.asList(
                LessonSchedule.builder().id(1L).build(), LessonSchedule.builder().id(2L).build()));
    assertArrayEquals(
        new LessonSchedule[] {LessonSchedule.builder().id(2L).build()}, deleted.toArray());
  }
}
