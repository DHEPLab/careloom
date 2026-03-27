package edu.stanford.fsi.reap.dto;

import edu.stanford.fsi.reap.entity.User;
import lombok.Data;

@Data
public class AdminDTO {
  private Long id;

  private String name;

  private String password;

  private Long projectId;

  public static AdminDTO transferTo(User user) {
    AdminDTO adminDTO = new AdminDTO();
    adminDTO.setId(user.getId());
    adminDTO.setName(user.getUsername());
    adminDTO.setPassword(user.getUsername());
    adminDTO.setProjectId(user.getProjectId());
    return adminDTO;
  }
}
