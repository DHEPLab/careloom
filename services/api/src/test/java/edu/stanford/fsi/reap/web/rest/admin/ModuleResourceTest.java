package edu.stanford.fsi.reap.web.rest.admin;

import static edu.stanford.fsi.reap.entity.enumerations.CurriculumBranch.DRAFT;
import static edu.stanford.fsi.reap.entity.enumerations.CurriculumBranch.MASTER;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.fsi.reap.entity.Module;
import edu.stanford.fsi.reap.entity.enumerations.CurriculumBranch;
import edu.stanford.fsi.reap.entity.enumerations.ModuleTopic;
import edu.stanford.fsi.reap.pojo.Component;
import edu.stanford.fsi.reap.repository.LessonRepository;
import edu.stanford.fsi.reap.repository.ModuleRepository;
import edu.stanford.fsi.reap.service.ModuleService;
import edu.stanford.fsi.reap.web.rest.errors.BadRequestAlertException;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

@AutoConfigureMockMvc
class ModuleResourceTest {

  @InjectMocks private static MockMvc mockMvc;
  public static ModuleResource resource;
  public static ModuleRepository repository;
  private static ModuleService service;
  private static LessonRepository lessonRepository;
  private static final String url = "/admin/modules";
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeAll
  public static void beforeAll() {
    repository = mock(ModuleRepository.class);
    service = mock(ModuleService.class);
    lessonRepository = mock(LessonRepository.class);
    when(repository.findByNumberAndBranch("M1", CurriculumBranch.MASTER))
        .thenReturn(Optional.of(Module.builder().id(1L).versionKey("VERSION_KEY").build()));
    when(repository.findById(2L))
        .thenReturn(
            Optional.of(Module.builder().id(2L).versionKey("VERSION_KEY").branch(DRAFT).build()));
    resource =
        new ModuleResource(
            lessonRepository, repository, mock(ModuleService.class), new ModelMapper());
    mockMvc =
        MockMvcBuilders.standaloneSetup(resource)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setViewResolvers(
                new ViewResolver() {
                  @Override
                  public View resolveViewName(String viewName, Locale locale) throws Exception {
                    return new MappingJackson2JsonView();
                  }
                })
            .build();
  }

  @Test
  @WithMockUser
  void publishModule() throws Exception {
    Component component = new Component();
    Module module =
        Module.builder()
            .name("test")
            .number("test")
            .description("test")
            .topic(ModuleTopic.BABY_FOOD)
            .versionKey("test")
            .components(Collections.singletonList(component))
            .build();

    when(repository.findByNumberAndBranch(module.getNumber(), MASTER))
        .thenReturn(Optional.of(module));
    when(repository.findById(1L)).thenReturn(Optional.of(module));
    when(service.cleanUpTheExtraPageFooter(module.getComponents()))
        .thenReturn(module.getComponents());
    when(service.publish(module)).thenReturn(module);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(module))
                .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertTrue(result.getResolvedException() instanceof BadRequestAlertException));

    module.setId(1L);
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(module))
                .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void saveModuleDraft() throws Exception {
    Component component = new Component();
    Module module =
        Module.builder()
            .name("test")
            .number("test")
            .description("test")
            .topic(ModuleTopic.BABY_FOOD)
            .versionKey("test")
            .components(Collections.singletonList(component))
            .build();

    when(repository.findByNumberAndBranch(module.getNumber(), MASTER))
        .thenReturn(Optional.of(new Module()));

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(url + "/draft")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(module))
                .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertTrue(result.getResolvedException() instanceof BadRequestAlertException));

    when(service.cleanUpTheExtraPageFooter(module.getComponents()))
        .thenReturn(module.getComponents());
    when(service.draft(module)).thenReturn(module);
    module.setId(1L);
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(url + "/draft")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(module))
                .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void getModules() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(url)
                .param("search", "test")
                .param("published", String.valueOf(false)))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void getModule() throws Exception {
    Component component = new Component();
    Module module =
        Module.builder()
            .id(1L)
            .name("test")
            .number("test")
            .description("test")
            .branch(MASTER)
            .topic(ModuleTopic.BABY_FOOD)
            .versionKey("test")
            .components(Collections.singletonList(component))
            .build();

    when(repository.findById(module.getId())).thenReturn(Optional.of(module));
    when(repository.findOneByVersionKeyAndBranch(module.getVersionKey(), DRAFT))
        .thenReturn(Optional.of(module));

    try {
      mockMvc
          .perform(MockMvcRequestBuilders.get(url + "/{id}", 1L).characterEncoding("UTF-8"))
          .andDo(print())
          .andExpect(status().isBadRequest());
    } catch (Exception e) {
    } finally {
      module.setBranch(DRAFT);
      mockMvc
          .perform(MockMvcRequestBuilders.get(url + "/{id}", 1L).characterEncoding("UTF-8"))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1L));
    }
  }

  @Test
  @WithMockUser
  void deleteModule() throws Exception {
    Component component = new Component();
    Module module =
        Module.builder()
            .id(1L)
            .published(true)
            .name("test")
            .number("test")
            .description("test")
            .branch(MASTER)
            .topic(ModuleTopic.BABY_FOOD)
            .versionKey("test")
            .components(Collections.singletonList(component))
            .build();

    when(repository.findById(1L)).thenReturn(Optional.of(module));

    when(lessonRepository.countByModuleId(1L)).thenReturn(2L);

    mockMvc
        .perform(MockMvcRequestBuilders.delete(url + "/{id}", 1L).characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(
            result ->
                assertTrue(result.getResolvedException() instanceof BadRequestAlertException));

    module.setBranch(DRAFT);
    mockMvc
        .perform(MockMvcRequestBuilders.delete(url + "/{id}", 1L).characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isOk());

    verify(repository, times(1)).deleteById(1L);
  }
}
