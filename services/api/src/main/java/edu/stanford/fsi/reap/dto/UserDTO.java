package edu.stanford.fsi.reap.dto;

import edu.stanford.fsi.reap.entity.CommunityHouseWorker;
import edu.stanford.fsi.reap.security.AuthoritiesConstants;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * @author hookszhang
 */
@Data
public class UserDTO {

  private Long id;

  @Size(min = 2, max = 20)
  @NotNull private String username;

  @Size(min = 6, max = 32)
  @NotNull private String password;

  @NotNull @Size(min = 1, max = 50)
  private String realName;

  @NotNull private String phone;

  @NotNull private String role;

  private CommunityHouseWorker chw;

  public boolean roleChw() {
    return AuthoritiesConstants.CHW.equals(role);
  }

  public boolean chwIsEmpty() {
    return chw == null || StringUtils.isEmpty(chw.getIdentity());
  }
}
