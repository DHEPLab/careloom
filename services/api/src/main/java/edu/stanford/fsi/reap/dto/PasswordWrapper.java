package edu.stanford.fsi.reap.dto;

import static edu.stanford.fsi.reap.config.Constants.PASSWORD_MAX_LENGTH;
import static edu.stanford.fsi.reap.config.Constants.PASSWORD_MIN_LENGTH;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordWrapper {
  @NotNull private String oldPassword;

  @NotNull @Size(min = PASSWORD_MIN_LENGTH, max = PASSWORD_MAX_LENGTH)
  private String password;
}
