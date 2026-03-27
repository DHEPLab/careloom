package edu.stanford.fsi.reap.config;

import com.fasterxml.classmate.TypeResolver;
import io.swagger.models.auth.In;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.util.AntPathMatcher;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author hookszhang
 */
@Configuration
@EnableSwagger2
@Profile({"local", "dev"})
public class SwaggerConfig {

  private static final Contact DEFAULT_CONTACT =
      new Contact("Hooks Zhang", "", "hookszhang@gmail.com");
  private static final ApiInfo DEFAULT_API_INFO =
      new ApiInfo(
          "Healthy Future Documentation",
          "",
          "0.0.1",
          "",
          DEFAULT_CONTACT,
          "",
          "",
          new ArrayList<>());

  @Bean
  public Docket api(TypeResolver typeResolver) {
    return new Docket(DocumentationType.SWAGGER_2)
        .securitySchemes(
            Collections.singletonList(
                new ApiKey("JWT Access", HttpHeaders.AUTHORIZATION, In.HEADER.name())))
        .securityContexts(Collections.singletonList(securityContext()))
        .select()
        .apis(RequestHandlerSelectors.basePackage("edu.stanford.fsi.reap.web.rest"))
        .paths(PathSelectors.any())
        .build()
        .apiInfo(DEFAULT_API_INFO);
  }

  private SecurityContext securityContext() {
    AntPathMatcher matcher = new AntPathMatcher();
    return SecurityContext.builder()
        .securityReferences(defaultAuth())
        .forPaths(
            input -> {
              assert input != null;
              if (matcher.match("/api/authenticate", input)) return false;
              return !matcher.match("/admin/authenticate", input);
            })
        .build();
  }

  List<SecurityReference> defaultAuth() {
    AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
    AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
    authorizationScopes[0] = authorizationScope;
    return Collections.singletonList(new SecurityReference("JWT Access", authorizationScopes));
  }
}
