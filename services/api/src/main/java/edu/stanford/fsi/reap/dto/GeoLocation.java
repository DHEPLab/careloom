package edu.stanford.fsi.reap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeoLocation {
  private String name;
  private double lat;
  private double lng;
}
