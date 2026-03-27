package edu.stanford.fsi.reap.web.rest;

import edu.stanford.fsi.reap.dto.JWTToken;
import edu.stanford.fsi.reap.dto.LoginDTO;
import edu.stanford.fsi.reap.jwt.JWTConfigurer;
import edu.stanford.fsi.reap.jwt.TokenProvider;
import edu.stanford.fsi.reap.security.AuthoritiesConstants;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hookszhang
 */
@RestController
@RequestMapping
@Slf4j
public class AccountJWTResource {

  private final TokenProvider tokenProvider;

  private final AuthenticationManager authenticationManager;

  public AccountJWTResource(
      TokenProvider tokenProvider, AuthenticationManager authenticationManager) {
    this.tokenProvider = tokenProvider;
    this.authenticationManager = authenticationManager;
  }

  @PostMapping("/api/authenticate")
  public ResponseEntity<JWTToken> appSignIn(@RequestBody LoginDTO loginDTO) {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());

    Authentication authentication = this.authenticationManager.authenticate(authenticationToken);
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    if (!authorities.contains(new SimpleGrantedAuthority(AuthoritiesConstants.CHW))) {
      throw new BadCredentialsException("Bad Authority");
    }

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = tokenProvider.createToken(authentication, loginDTO.isRememberMe());
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(JWTConfigurer.AUTHORIZATION_HEADER, "Bearer " + jwt);
    return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.OK);
  }

  @PostMapping("/admin/authenticate")
  public ResponseEntity<JWTToken> adminSignIn(@RequestBody LoginDTO loginDTO) {
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());

    Authentication authentication = this.authenticationManager.authenticate(authenticationToken);
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    if (authorities.contains(new SimpleGrantedAuthority(AuthoritiesConstants.CHW))) {
      throw new BadCredentialsException("Bad Authority");
    }

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = tokenProvider.createToken(authentication, loginDTO.isRememberMe());
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(JWTConfigurer.AUTHORIZATION_HEADER, "Bearer " + jwt);
    return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.OK);
  }
}
