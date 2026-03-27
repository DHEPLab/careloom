package edu.stanford.fsi.reap.web.rest.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.fsi.reap.dto.CarerDTO;
import edu.stanford.fsi.reap.entity.Baby;
import edu.stanford.fsi.reap.entity.Carer;
import edu.stanford.fsi.reap.entity.enumerations.BabyStage;
import edu.stanford.fsi.reap.entity.enumerations.FamilyTies;
import edu.stanford.fsi.reap.entity.enumerations.Gender;
import edu.stanford.fsi.reap.repository.CarerModifyRecordRepository;
import edu.stanford.fsi.reap.repository.CarerRepository;
import edu.stanford.fsi.reap.repository.UserRepository;
import edu.stanford.fsi.reap.service.CarerModifyRecordService;
import edu.stanford.fsi.reap.service.CarerService;
import edu.stanford.fsi.reap.web.rest.errors.BadRequestAlertException;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@AutoConfigureMockMvc
class CarerResourceTest {

  @InjectMocks private static CarerResource carerResource;
  private static CarerRepository repository;
  private static CarerService service;
  private static MockMvc mockMvc;
  private static ObjectMapper objectMapper;
  private static final String url = "/admin";
  private static CarerModifyRecordRepository carerModifyRecordRepository;
  private static CarerModifyRecordService carerModifyRecordService;
  private static UserRepository userRepository;

  @BeforeAll
  public static void beforeAll() {
    repository = mock(CarerRepository.class);
    service = mock(CarerService.class);
    ModelMapper modelMapper = new ModelMapper();
    carerModifyRecordRepository = mock(CarerModifyRecordRepository.class);
    carerModifyRecordService = mock(CarerModifyRecordService.class);

    carerResource =
        new CarerResource(
            repository,
            service,
            modelMapper,
            carerModifyRecordRepository,
            userRepository,
            carerModifyRecordService);
    mockMvc = MockMvcBuilders.standaloneSetup(carerResource).build();

    objectMapper = new ObjectMapper();
  }

  @Test
  @WithMockUser
  void createCarer() throws Exception {
    Baby baby =
        Baby.builder()
            .id(1L)
            .name("test1")
            .gender(Gender.MALE)
            .stage(BabyStage.BORN)
            .area("上海")
            .location("静安")
            .build();

    CarerDTO carerDTO = new CarerDTO();
    carerDTO.setName("test");
    carerDTO.setPhone("13217499804");
    carerDTO.setOtherContact("wechat");
    carerDTO.setFamilyTies(FamilyTies.MOTHER);
    carerDTO.setBaby(baby);

    when(service.save(any()))
        .thenAnswer(
            obj -> {
              Carer carer = obj.getArgument(0);
              carer.setId(1L);
              return carer;
            });

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(url + "/carers")
                .content(objectMapper.writeValueAsString(carerDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("1"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("test"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.phone").value("13217499804"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.otherContact").value("wechat"))
        .andReturn();
  }

  @Test
  @WithMockUser
  void updateCarer() throws Exception {
    Baby baby =
        Baby.builder()
            .id(1L)
            .name("test1")
            .gender(Gender.MALE)
            .stage(BabyStage.BORN)
            .area("上海")
            .location("静安")
            .build();

    CarerDTO carerDTO = new CarerDTO();
    carerDTO.setName("test");
    carerDTO.setPhone("13217499804");
    carerDTO.setOtherContact("wechat");
    carerDTO.setFamilyTies(FamilyTies.MOTHER);
    carerDTO.setBaby(baby);

    when(repository.findOneByBabyIdAndMasterIsTrue(baby.getId()))
        .thenReturn(Optional.ofNullable(Carer.builder().id(1L).build()));

    /*
        mockMvc.perform(MockMvcRequestBuilders
                .put(url+"/carers/{id}",1L)
                .content(objectMapper.writeValueAsString(carerDTO))
                .characterEncoding("UTF-8")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof BadRequestAlertException));
    */

    try {
      mockMvc
          .perform(
              MockMvcRequestBuilders.put(url + "/carers/{id}", 1L)
                  .content(objectMapper.writeValueAsString(carerDTO))
                  .characterEncoding("UTF-8")
                  .contentType(MediaType.APPLICATION_JSON))
          .andDo(print());
    } catch (Exception e) {
      assert Objects.equals(((BadRequestAlertException) e.getCause()).getDetail(), "请至少设置一个主看护人");
    } finally {
      /*      when(service.save(any())).thenAnswer(i -> {
        Carer carer=i.getArgument(0);
        carer.setId(1L);
        return carer;
      });*/

      carerDTO.setMaster(true);
      mockMvc
          .perform(
              MockMvcRequestBuilders.put(url + "/carers/{id}", 1L)
                  .content(objectMapper.writeValueAsString(carerDTO))
                  .characterEncoding("UTF-8")
                  .contentType(MediaType.APPLICATION_JSON))
          .andDo(print())
          .andExpect(status().isOk());
    }
  }

  @Test
  @WithMockUser
  void deleteCarer() throws Exception {
    when(repository.findById(1L))
        .thenReturn(
            Optional.ofNullable(
                Carer.builder().phone("13217499804").name("text").master(true).build()));

    try {
      mockMvc.perform(MockMvcRequestBuilders.delete(url + "/carers/{id}", 1L)).andDo(print());
    } catch (Exception e) {
      assert Objects.equals(
          ((BadRequestAlertException) e.getCause()).getDetail(), "主看护人不可删除，请更换主看护人后进行此操作");
    } finally {
      when(repository.findById(1L))
          .thenReturn(
              Optional.ofNullable(
                  Carer.builder().phone("13217499804").name("text").master(false).build()));

      mockMvc
          .perform(MockMvcRequestBuilders.delete(url + "/carers/{id}", 1L))
          .andDo(print())
          .andExpect(status().isOk());

      verify(repository, times(1)).deleteById(any());
    }
  }
}
