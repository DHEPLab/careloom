package edu.stanford.fsi.reap.dto;

import edu.stanford.fsi.reap.entity.Baby;
import edu.stanford.fsi.reap.entity.Carer;
import edu.stanford.fsi.reap.entity.Curriculum;
import edu.stanford.fsi.reap.entity.enumerations.BabyStage;
import edu.stanford.fsi.reap.entity.enumerations.Gender;
import edu.stanford.fsi.reap.utils.BabyAge;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

/**
 * @author hookszhang
 */
@Data
public class AppBabyDTO {

  private List<Carer> allCarerList;
  private NextShouldVisitDTO nextShouldVisitDTO;
  private Curriculum curriculum;
  private LocalDate edc;
  private LocalDate birthday;
  private String remark;

  private String carerName;
  private String carerPhone;
  private int months;
  private int days;

  private final Long id;
  private final String name;
  private final String identity;
  private final Gender gender;
  private final BabyStage stage;
  private final String area;
  private final String location;
  private final boolean approved;
  private final boolean pastEdc;
  private Double longitude;
  private Double latitude;
  private Boolean showLocation;

  public AppBabyDTO(Baby baby, Carer masterCarer) {
    this.id = baby.getId();
    this.name = baby.getName();
    this.identity = baby.getIdentity();
    this.gender = baby.getGender();
    this.stage = baby.getStage();
    this.area = baby.getArea();
    this.location = baby.getLocation();
    this.approved = baby.getApproved();
    this.latitude = baby.getLatitude();
    this.longitude = baby.getLongitude();
    this.showLocation = baby.getShowLocation();
    this.pastEdc = BabyAge.pastEdc(baby.getStage(), baby.getEdc(), LocalDate.now());
    if (masterCarer != null) {
      carerName = masterCarer.getName();
      carerPhone = masterCarer.getPhone();
    }
    computeAge(stage, baby.getEdc(), baby.getBirthday());
  }

  public AppBabyDTO(
      Long id,
      String name,
      String identity,
      Gender gender,
      BabyStage stage,
      LocalDate edc,
      LocalDate birth,
      String area,
      String location,
      boolean approved,
      String carerName,
      String carerPhone,
      Curriculum curriculum,
      String remark) {
    this.id = id;
    this.name = name;
    this.identity = identity;
    this.gender = gender;
    this.stage = stage;
    this.area = area;
    this.location = location;
    this.carerName = carerName;
    this.carerPhone = carerPhone;
    this.approved = approved;
    this.curriculum = curriculum;
    this.edc = edc;
    this.birthday = birth;
    this.remark = remark;
    this.pastEdc = BabyAge.pastEdc(stage, edc, LocalDate.now());

    computeAge(stage, edc, birth);
  }

  public AppBabyDTO(
      Long id,
      String name,
      String identity,
      Gender gender,
      BabyStage stage,
      LocalDate edc,
      LocalDate birth,
      String area,
      String location,
      boolean approved,
      String remark) {
    this.id = id;
    this.name = name;
    this.identity = identity;
    this.gender = gender;
    this.stage = stage;
    this.area = area;
    this.location = location;
    this.approved = approved;
    this.edc = edc;
    this.birthday = birth;
    this.remark = remark;
    this.pastEdc = BabyAge.pastEdc(stage, edc, LocalDate.now());

    computeAge(stage, edc, birth);
  }

  private void computeAge(BabyStage stage, LocalDate edc, LocalDate birthday) {
    Baby baby = Baby.builder().stage(stage).edc(edc).birthday(birthday).build();
    this.months = BabyAge.months(baby, LocalDate.now());
    this.days = BabyAge.days(baby, LocalDate.now());
  }

  public void checkNextShouldVisitDTO() {
    if (nextShouldVisitDTO == null)
      nextShouldVisitDTO = NextShouldVisitDTO.builder().visitDateRange(null).lesson(null).build();
  }
}
