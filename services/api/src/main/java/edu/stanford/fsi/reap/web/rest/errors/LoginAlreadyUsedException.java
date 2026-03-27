package edu.stanford.fsi.reap.web.rest.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class LoginAlreadyUsedException extends BadRequestAlertException {

  private static final String ENTITY_NAME = "userManagement";
  private static final String ERROR_KEY = "userexists";

  public LoginAlreadyUsedException(String login) {
    super("error.userManagement.userExists", ENTITY_NAME, ERROR_KEY, login);
  }
}
