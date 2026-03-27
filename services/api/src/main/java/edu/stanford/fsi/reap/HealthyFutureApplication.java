package edu.stanford.fsi.reap;

import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableJpaRepositories
@EnableJpaAuditing
@EnableScheduling
public class HealthyFutureApplication {

  public static void main(String[] args) throws UnknownHostException {
    SpringApplication app = new SpringApplication(HealthyFutureApplication.class);
    Environment env = app.run(args).getEnvironment();
    String protocol = "http";
    log.info(
        "\n----------------------------------------------------------\n\t"
            + "Application '{}' is running now! Access URLs:\n\t"
            + "Local: \t\t{}://localhost:{}\n\t"
            + "External: \t{}://{}:{}\n\t"
            + "Profile(s): \t{}\n----------------------------------------------------------",
        env.getProperty("spring.application.name"),
        protocol,
        env.getProperty("server.port"),
        protocol,
        InetAddress.getLocalHost().getHostAddress(),
        env.getProperty("server.port"),
        env.getActiveProfiles());
  }
}
