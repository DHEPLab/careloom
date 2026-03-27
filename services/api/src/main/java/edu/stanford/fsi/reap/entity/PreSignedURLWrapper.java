package edu.stanford.fsi.reap.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PreSignedURLWrapper {
  private final String url;
}
