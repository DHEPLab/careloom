package edu.stanford.fsi.reap.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Object to return as body in JWT Authentication. */
public class JWTToken {

  private String idToken;

  public JWTToken(String idToken) {
    this.idToken = idToken;
  }

  @JsonProperty("idToken")
  String getIdToken() {
    return idToken;
  }

  void setIdToken(String idToken) {
    this.idToken = idToken;
  }
}
