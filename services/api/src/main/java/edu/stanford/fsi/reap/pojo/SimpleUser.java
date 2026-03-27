package edu.stanford.fsi.reap.pojo;

import java.time.LocalDateTime;

/**
 * @author hookszhang
 */
public interface SimpleUser {
  Long getId();

  LocalDateTime getLastModifiedPasswordAt();
}
