package edu.stanford.fsi.reap.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import edu.stanford.fsi.reap.entity.Baby;
import edu.stanford.fsi.reap.entity.Carer;
import edu.stanford.fsi.reap.repository.CarerRepository;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CarerServiceTest {

  private static CarerService service;

  @BeforeAll
  public static void beforeAll() {
    CarerRepository repository = Mockito.mock(CarerRepository.class);
    Mockito.when(repository.save(any()))
        .then(
            invocation -> {
              Carer carer = invocation.getArgument(0);
              if (carer.getId() == null) {
                carer.setId(1L);
              }
              return carer;
            });
    service = new CarerService(repository);
  }

  @Test
  public void should_save_all_carers() {
    List<Carer> carers = Arrays.asList(new Carer(), new Carer());
    Baby baby = Baby.builder().id(2L).build();
    service.saveAll(carers, baby);
    assertEquals(1L, carers.get(0).getId());
    assertEquals(1L, carers.get(1).getId());
    assertEquals(baby, carers.get(0).getBaby());
    assertEquals(baby, carers.get(0).getBaby());
  }
}
