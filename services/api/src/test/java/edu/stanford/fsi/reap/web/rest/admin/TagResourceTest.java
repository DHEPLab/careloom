package edu.stanford.fsi.reap.web.rest.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.stanford.fsi.reap.entity.Tag;
import edu.stanford.fsi.reap.repository.TagRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@AutoConfigureMockMvc
class TagResourceTest {

  @InjectMocks private static MockMvc mockMvc;
  private static TagRepository repository;
  private static final String url = "/admin/tags";

  @BeforeAll
  public static void beforeAll() {
    repository = mock(TagRepository.class);
    TagResource tagResource = new TagResource(repository);
    mockMvc = MockMvcBuilders.standaloneSetup(tagResource).build();
  }

  @Test
  @WithMockUser
  void getAllTags() throws Exception {
    List<Tag> list = new ArrayList<>();
    when(repository.findAll(Sort.by(Sort.Direction.DESC, "id"))).thenReturn(list);
    mockMvc.perform(MockMvcRequestBuilders.get(url)).andDo(print()).andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void deleteTag() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.delete(url + "/{id}", 1L))
        .andDo(print())
        .andExpect(status().isOk());

    verify(repository, times(1)).deleteById(1L);
  }
}
