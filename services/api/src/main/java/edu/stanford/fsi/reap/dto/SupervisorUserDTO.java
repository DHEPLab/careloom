package edu.stanford.fsi.reap.dto;

import edu.stanford.fsi.reap.entity.User;
import lombok.Getter;

@Getter
public class SupervisorUserDTO {
  private final User user;
  private final Long chwCount;

  public SupervisorUserDTO(User user, Long chwCount) {
    this.user = user;
    this.chwCount = chwCount;
  }
}
