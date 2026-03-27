package edu.stanford.fsi.reap.dto;

import edu.stanford.fsi.reap.entity.enumerations.ActionFromApp;
import edu.stanford.fsi.reap.entity.enumerations.Gender;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hookszhang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBabyDTO {

  private Long id;

  private String identity;

  private String name;

  private Gender gender;

  private String area;

  private String chw;

  private Integer visitCount;

  private String currentLessonName;

  private Double longitude;

  private Double latitude;

  private Boolean showLocation;

  private ActionFromApp actionFromApp;

  private LocalDateTime lastModifiedAt;

  private LocalDateTime createdAt;

  private Boolean deleted;

  //  Long getId();
  //
  //  String getIdentity();
  //
  //  String getName();
  //
  //  Gender getGender();
  //
  //  String getArea();
  //
  //  String getChw();
  //  Long getChwId();
  //
  //  Integer getVisitCount();
  //
  //  String getCurrentLessonName();
  //
  //  ActionFromApp getActionFromApp();
  //
  //  LocalDateTime getLastModifiedAt();
  //  LocalDateTime getCreatedAt();
  //
  //  Boolean getDeleted();
}
