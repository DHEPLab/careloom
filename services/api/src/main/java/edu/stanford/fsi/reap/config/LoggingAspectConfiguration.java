package edu.stanford.fsi.reap.config;

import edu.stanford.fsi.reap.logging.LoggingAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class LoggingAspectConfiguration {

  @Bean
  public LoggingAspect loggingAspect() {
    return new LoggingAspect();
  }
}
