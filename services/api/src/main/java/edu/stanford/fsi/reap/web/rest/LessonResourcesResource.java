package edu.stanford.fsi.reap.web.rest;

import edu.stanford.fsi.reap.dto.AppOfflineLessonDTO;
import edu.stanford.fsi.reap.dto.ModulePackage;
import edu.stanford.fsi.reap.dto.Updates;
import edu.stanford.fsi.reap.entity.Lesson;
import edu.stanford.fsi.reap.entity.Module;
import edu.stanford.fsi.reap.entity.enumerations.CurriculumBranch;
import edu.stanford.fsi.reap.repository.LessonRepository;
import edu.stanford.fsi.reap.repository.ModuleRepository;
import edu.stanford.fsi.reap.security.SecurityUtils;
import edu.stanford.fsi.reap.service.LessonService;
import edu.stanford.fsi.reap.service.ModuleService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resources")
public class LessonResourcesResource {

  private final ModuleService moduleService;
  private final ModuleRepository moduleRepository;
  private final LessonRepository lessonRepository;
  private final LessonService lessonService;

  public LessonResourcesResource(
      ModuleService moduleService,
      ModuleRepository moduleRepository,
      LessonRepository lessonRepository,
      LessonService lessonService) {
    this.moduleService = moduleService;
    this.moduleRepository = moduleRepository;
    this.lessonRepository = lessonRepository;
    this.lessonService = lessonService;
  }

  @GetMapping("/check-for-updates")
  public Updates checkForUpdates(
      @RequestParam("lastUpdateAt") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          ZonedDateTime lastUpdateAt) {
    if (lastUpdateAt == null) return Updates.isTheLatest();

    // [Temporary fix] Converts UTC time to the system's default time zone
    LocalDateTime localLastUpdateAt =
        lastUpdateAt.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();

    Optional<Lesson> lastLesson =
        lessonRepository
            .findFirstByCurriculumBranchAndCurriculumPublishedTrueOrderByLastModifiedAtDesc(
                CurriculumBranch.MASTER);
    Optional<Module> lastModule =
        moduleRepository.findFirstByBranchAndPublishedTrueOrderByLastModifiedAtDesc(
            CurriculumBranch.MASTER);

    if (lastLesson.isPresent() && lastLesson.get().getLastModifiedAt().isAfter(localLastUpdateAt)) {
      return Updates.haveUpdate(lastLesson.get().getLastModifiedAt());
    }

    if (lastModule.isPresent() && lastModule.get().getLastModifiedAt().isAfter(localLastUpdateAt)) {
      return Updates.haveUpdate(lastModule.get().getLastModifiedAt());
    }

    return Updates.isTheLatest();
  }

  @GetMapping("/modules")
  public ModulePackage downloadModules() {
    List<Module> modules = moduleRepository.findByBranchAndPublishedTrue(CurriculumBranch.MASTER);
    List<String> media = new ArrayList<>();
    Long projectId = SecurityUtils.getProjectId();
    modules.stream()
        .filter(
            target -> {
              return target.getProjectId() != null && target.getProjectId().equals(projectId);
            })
        .forEach(module -> media.addAll(moduleService.media(module.getComponents())));
    return new ModulePackage(modules, media);
  }

  @GetMapping("/lessons")
  public List<AppOfflineLessonDTO> downloadLessons() {
    List<Lesson> lessons =
        lessonRepository.findByCurriculumBranchAndCurriculumPublishedTrue(CurriculumBranch.MASTER);
    List<Module> modules = moduleRepository.findByBranchAndPublishedTrue(CurriculumBranch.MASTER);
    return lessonService.appOfflineLessons(lessons, modules);
  }
}
