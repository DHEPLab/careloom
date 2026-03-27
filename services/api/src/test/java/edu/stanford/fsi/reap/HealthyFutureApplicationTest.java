package edu.stanford.fsi.reap;

import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.stanford.fsi.reap.dto.ReportDTO;
import edu.stanford.fsi.reap.service.VisitReportService;
import java.time.LocalDate;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Disabled
public class HealthyFutureApplicationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired VisitReportService visitReportService;

  @Test
  public void test1() throws Exception {
    visitReportService.report(
        new ReportDTO(LocalDate.of(2018, 1, 1), LocalDate.now()), "en", "America/New_York");
  }

  @Test
  public void should_return_default_message() throws Exception {
    this.mockMvc
        .perform(get("/index.html"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("healthy future service is running!")));
  }
}
