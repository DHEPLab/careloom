package edu.stanford.fsi.reap.config;

import edu.stanford.fsi.reap.jwt.JWTConfigurer;
import edu.stanford.fsi.reap.jwt.TokenProvider;
import edu.stanford.fsi.reap.repository.UserRepository;
import edu.stanford.fsi.reap.security.AuthoritiesConstants;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

/**
 * @author hookszhang
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class JWTSecurityConfig extends WebSecurityConfigurerAdapter {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  private final UserDetailsService userDetailsService;
  private final AuthenticationManagerBuilder authenticationManagerBuilder;
  private final TokenProvider tokenProvider;
  private final SecurityProblemSupport problemSupport;
  private final UserRepository userRepository;

  public JWTSecurityConfig(
      UserDetailsService userDetailsService,
      AuthenticationManagerBuilder authenticationManagerBuilder,
      TokenProvider tokenProvider,
      SecurityProblemSupport problemSupport,
      UserRepository userRepository) {
    this.userDetailsService = userDetailsService;
    this.authenticationManagerBuilder = authenticationManagerBuilder;
    this.tokenProvider = tokenProvider;
    this.problemSupport = problemSupport;
    this.userRepository = userRepository;
  }

  @PostConstruct
  public void init() {
    try {
      authenticationManagerBuilder
          .userDetailsService(userDetailsService)
          .passwordEncoder(passwordEncoder());
    } catch (Exception e) {
      throw new BeanInitializationException("Security configuration failed", e);
    }
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.exceptionHandling()
        .authenticationEntryPoint(problemSupport)
        .accessDeniedHandler(problemSupport)
        .and()
        .csrf()
        .disable()
        .headers()
        .frameOptions()
        .disable()
        .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authorizeRequests()
        .antMatchers(
            "/api/authenticate",
            "/admin/authenticate",
            "/api/files/**",
            "/api/visits/notStartVisit",
            "/init/baby/location") // "/admin/babies/**" "/admin/report/**"
        .permitAll()
        .antMatchers("/api/account/**")
        .hasAnyAuthority(
            AuthoritiesConstants.ADMIN,
            AuthoritiesConstants.SUPERVISOR,
            AuthoritiesConstants.CHW,
            AuthoritiesConstants.SUPER_ADMIN)
        .antMatchers("/api/**")
        .hasAnyAuthority(AuthoritiesConstants.CHW)
        .antMatchers(
            "/admin/modules/**",
            "/admin/curriculums/**",
            "/admin/files/**",
            "/admin/users/admin",
            "/admin/users/supervisor/**")
        .hasAnyAuthority(AuthoritiesConstants.ADMIN, AuthoritiesConstants.SUPER_ADMIN)
        .antMatchers("/admin/**")
        .hasAnyAuthority(
            AuthoritiesConstants.ADMIN,
            AuthoritiesConstants.SUPERVISOR,
            AuthoritiesConstants.SUPER_ADMIN)
        .and()
        .apply(securityConfigurerAdapter());
  }

  private JWTConfigurer securityConfigurerAdapter() {
    return new JWTConfigurer(tokenProvider, userRepository);
  }
}
