package edu.stanford.fsi.reap.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/** View Model object for storing a user's credentials. */
@Data
public class LoginDTO {

  @NotNull @Size(min = 1, max = 50)
  private String username;

  @NotNull @Size(min = 6, max = 64)
  private String password;

  private boolean rememberMe;
}
