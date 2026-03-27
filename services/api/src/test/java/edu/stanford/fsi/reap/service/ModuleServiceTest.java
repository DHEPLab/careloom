package edu.stanford.fsi.reap.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.stanford.fsi.reap.entity.Module;
import edu.stanford.fsi.reap.entity.enumerations.CurriculumBranch;
import edu.stanford.fsi.reap.entity.enumerations.ModuleComponentType;
import edu.stanford.fsi.reap.pojo.Component;
import edu.stanford.fsi.reap.repository.ModuleRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ModuleServiceTest {

  private static ModuleService moduleService;

  private static final String VERSION_KEY = "default_version_key";

  @BeforeAll
  public static void beforeAll() {
    ModuleRepository moduleRepository = mock(ModuleRepository.class);
    Optional<Module> publishedModule =
        Optional.of(
            Module.builder()
                .id(1L)
                .branch(CurriculumBranch.MASTER)
                .number("N1")
                .versionKey(VERSION_KEY)
                .published(true)
                .build());

    Optional<Module> draftModule =
        Optional.of(
            Module.builder()
                .id(2L)
                .number("N2")
                .branch(CurriculumBranch.DRAFT)
                .versionKey(VERSION_KEY)
                .published(false)
                .build());

    Optional<Module> masterUnpublishedModule =
        Optional.of(
            Module.builder()
                .id(3L)
                .number("N3")
                .branch(CurriculumBranch.MASTER)
                .versionKey(VERSION_KEY)
                .published(false)
                .build());

    when(moduleRepository.findById(1L)).thenReturn(publishedModule);
    when(moduleRepository.findById(2L)).thenReturn(draftModule);
    when(moduleRepository.findById(3L)).thenReturn(masterUnpublishedModule);
    when(moduleRepository.findOneByVersionKeyAndBranch(VERSION_KEY, CurriculumBranch.MASTER))
        .thenReturn(publishedModule);

    when(moduleRepository.save(any()))
        .then(
            invocation -> {
              Module module = (Module) invocation.getArguments()[0];
              if (module.getId() == null) {
                module.setId(3L);
              }
              return module;
            });

    moduleService = new ModuleService(moduleRepository, mock(LessonService.class));
  }

  @Test
  public void should_create_master_branch_and_unpublished() {
    Module module = Module.builder().name("Draft").build();
    module = moduleService.draft(module);
    assertEquals(CurriculumBranch.MASTER, module.getBranch());
    assertFalse(module.isPublished());
  }

  @Test
  public void should_create_draft_branch_from_master() {
    Module module = Module.builder().id(1L).name("Draft").build();
    module = moduleService.draft(module);
    assertEquals(CurriculumBranch.DRAFT, module.getBranch());
    assertFalse(module.isPublished());
    assertEquals(VERSION_KEY, module.getVersionKey());
  }

  @Test
  public void should_create_module_and_generate_32bit_version_key() {
    Module module = Module.builder().name("New Module").build();
    moduleService.publish(module);
    assertEquals(3L, module.getId());
    assertNotNull(module.getVersionKey());
    assertEquals(32, module.getVersionKey().length());
  }

  @Test
  public void should_create_master_branch_and_publish() {
    Module module = Module.builder().name("Publish").build();
    module = moduleService.publish(module);
    assertEquals(CurriculumBranch.MASTER, module.getBranch());
    assertTrue(module.isPublished());
  }

  @Test
  public void should_update_module_and_set_original_version_key() {
    Module module = Module.builder().id(1L).name("New Module").number("N1").build();
    module = moduleService.publish(module);
    assertEquals(1L, module.getId());
    assertNotNull(module.getVersionKey());
    assertEquals(VERSION_KEY, module.getVersionKey());
    assertTrue(module.isPublished());
  }

  @Test
  public void should_publish_draft_module() {
    Module module =
        Module.builder()
            .id(2L)
            .versionKey(VERSION_KEY)
            .number("N2")
            .branch(CurriculumBranch.DRAFT)
            .build();
    module = moduleService.publish(module);
    assertEquals(1L, module.getId());
    assertEquals(VERSION_KEY, module.getVersionKey());
    assertTrue(module.isPublished());
  }

  @Test
  public void should_publish_master_branch_module() {
    Module module =
        Module.builder()
            .id(3L)
            .versionKey(VERSION_KEY)
            .number("N1")
            .branch(CurriculumBranch.MASTER)
            .build();
    module = moduleService.publish(module);
    assertEquals(3L, module.getId());
    assertEquals(VERSION_KEY, module.getVersionKey());
    assertTrue(module.isPublished());
  }

  @Test
  public void should_return_component_media() {
    List<Component> components =
        Arrays.asList(
            new Component(
                ModuleComponentType.Media, 1L, new Component.Media("Picture", "/1.jpg", null)),
            new Component(
                ModuleComponentType.Media, 2L, new Component.Media("Video", "/1.mp4", null)),
            new Component(
                ModuleComponentType.Switch,
                3L,
                new Component.Switch(
                    null,
                    Arrays.asList(
                        new Component.Case(
                            4L,
                            null,
                            null,
                            Arrays.asList(
                                new Component(
                                    ModuleComponentType.Media,
                                    11L,
                                    new Component.Media("Picture", "/11.jpg", null)),
                                new Component(
                                    ModuleComponentType.Media,
                                    22L,
                                    new Component.Media("Video", "/11.mp4", null)))),
                        new Component.Case(
                            4L,
                            null,
                            null,
                            Arrays.asList(
                                new Component(
                                    ModuleComponentType.Media,
                                    111L,
                                    new Component.Media("Picture", "/111.jpg", null)),
                                new Component(
                                    ModuleComponentType.Media,
                                    222L,
                                    new Component.Media("Video", "/111.mp4", null))))))));

    assertArrayEquals(
        new String[] {"/1.jpg", "/1.mp4", "/11.jpg", "/11.mp4", "/111.jpg", "/111.mp4"},
        moduleService.media(components).toArray());
  }

  @Test
  public void should_clean_up_the_extra_page_footer() {
    List<Component> components =
        Arrays.asList(
            new Component(ModuleComponentType.PageFooter, 1L, null),
            new Component(ModuleComponentType.PageFooter, 100L, null),
            new Component(
                ModuleComponentType.Media, 1L, new Component.Media("Picture", "/1.jpg", null)),
            new Component(ModuleComponentType.PageFooter, 11L, null),
            new Component(ModuleComponentType.PageFooter, 12L, null),
            new Component(ModuleComponentType.PageFooter, 13L, null),
            new Component(ModuleComponentType.Switch, 2L, null),
            new Component(ModuleComponentType.PageFooter, 2L, null),
            new Component(
                ModuleComponentType.Media, 4L, new Component.Media("Video", "/1.mp4", null)),
            new Component(ModuleComponentType.PageFooter, 3L, null),
            new Component(ModuleComponentType.PageFooter, 300L, null));
    List<Component> list = moduleService.cleanUpTheExtraPageFooter(components);
    assertArrayEquals(
        Arrays.asList(
                new Component(
                    ModuleComponentType.Media, 1L, new Component.Media("Picture", "/1.jpg", null)),
                new Component(ModuleComponentType.PageFooter, 11L, null),
                new Component(ModuleComponentType.Switch, 2L, null),
                new Component(
                    ModuleComponentType.Media, 4L, new Component.Media("Video", "/1.mp4", null)))
            .toArray(),
        list.toArray());
  }
}
