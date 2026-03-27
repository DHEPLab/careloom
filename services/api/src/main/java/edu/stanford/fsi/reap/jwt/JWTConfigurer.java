package edu.stanford.fsi.reap.jwt;

import edu.stanford.fsi.reap.repository.UserRepository;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class JWTConfigurer
    extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

  public static final String AUTHORIZATION_HEADER = "Authorization";

  private final TokenProvider tokenProvider;

  private final UserRepository userRepository;

  public JWTConfigurer(TokenProvider tokenProvider, UserRepository userRepository) {
    this.tokenProvider = tokenProvider;
    this.userRepository = userRepository;
  }

  @Override
  public void configure(HttpSecurity http) {
    JWTFilter customFilter = new JWTFilter(tokenProvider, userRepository);
    http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
  }
}
