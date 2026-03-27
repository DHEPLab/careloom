package edu.stanford.fsi.reap.web.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.stanford.fsi.reap.service.FileService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@AutoConfigureMockMvc
class FileResourceTest {

  @InjectMocks private static FileResource resource;
  private static FileService fileService;
  private static MockMvc mockMvc;

  @BeforeAll
  public static void beforeAll() {
    fileService = mock(FileService.class);
    resource = new FileResource(fileService);
    mockMvc = MockMvcBuilders.standaloneSetup(resource).build();
  }

  @Test
  @WithMockUser
  public void should_redirectToOss() throws Exception {
    when(fileService.generatePresignedUrlForDownload(any())).thenReturn("http://test");

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/files/{filename}", "filename"))
        .andDo(print())
        .andExpect(status().is3xxRedirection());
  }
}
