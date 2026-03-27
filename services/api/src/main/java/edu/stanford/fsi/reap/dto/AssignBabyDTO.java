package edu.stanford.fsi.reap.dto;

import edu.stanford.fsi.reap.entity.enumerations.Gender;

public interface AssignBabyDTO {
  Long getId();

  String getName();

  String getIdentity();

  Gender getGender();

  String getArea();

  String getMasterCarerName();

  String getMasterCarerPhone();
}
