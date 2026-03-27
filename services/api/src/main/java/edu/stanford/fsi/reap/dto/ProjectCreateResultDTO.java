package edu.stanford.fsi.reap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCreateResultDTO {

  private Long projectId;

  private String username;

  private String password;
}
