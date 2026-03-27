package edu.stanford.fsi.reap.web.rest.admin;

import edu.stanford.fsi.reap.entity.Baby;
import edu.stanford.fsi.reap.handler.BabyLocationHandler;
import edu.stanford.fsi.reap.repository.BabyRepository;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("init")
@Slf4j
public class InitResource {

  private final BabyRepository babyRepository;

  private final BabyLocationHandler babyLocationHandler;

  public InitResource(BabyRepository babyRepository, BabyLocationHandler babyLocationHandler) {
    this.babyRepository = babyRepository;
    this.babyLocationHandler = babyLocationHandler;
  }

  /** init baby location */
  @GetMapping("baby/location")
  public void initBabyLocation() throws InterruptedException {

    List<Baby> allBabies = babyRepository.findAllByDeleted(false);
    if (CollectionUtils.isEmpty(allBabies)) {
      return;
    }
    for (Baby baby : allBabies) {
      if (baby.getLatitude() == null || baby.getLongitude() == null) {
        try {
          String geoInfo =
              babyLocationHandler.confirmBabyLocation(baby.getArea(), baby.getLocation());
          if (!StringUtils.isEmpty(geoInfo)) {
            if (geoInfo.indexOf(",") > 0) {
              String[] splits = geoInfo.split(",");
              baby.setLongitude(Double.valueOf(splits[0]));
              baby.setLatitude(Double.valueOf(splits[1]));
              baby.setShowLocation(false);
              babyRepository.save(baby);
            }
            // 避免超过调用最大值
            TimeUnit.MILLISECONDS.sleep(30);
          }
        } catch (Exception e) {
          log.warn("确定宝宝地理位置信息失败，{}", e.getMessage());
        }
      }
    }
  }
}
