package edu.stanford.fsi.reap.dto;

import edu.stanford.fsi.reap.entity.User;
import lombok.Data;

@Data
public class ChwUserDTO {
  private final User user;
  private final Long babyCount;

  public ChwUserDTO(User user, Long babyCount) {
    this.user = user;
    this.babyCount = babyCount;
  }

  /** 应完成 */
  private Integer shouldFinish;

  /** 已完成 */
  private Integer hasFinish;
}
