package edu.stanford.fsi.reap.jwt;

import edu.stanford.fsi.reap.entity.User;
import edu.stanford.fsi.reap.repository.UserRepository;
import edu.stanford.fsi.reap.security.CurrentUser;
import io.jsonwebtoken.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class TokenProvider {

  private final Logger log = LoggerFactory.getLogger(TokenProvider.class);

  private static final String AUTHORITIES_KEY = "auth";

  @Value("${application.secret-key}")
  private String secretKey;

  @Value("${application.token-validity-day}")
  private long tokenValidityDay;

  private UserRepository userRepository;

  public TokenProvider(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public String createToken(Authentication authentication, boolean rememberMe) {
    String authorities =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

    CurrentUser principal = (CurrentUser) authentication.getPrincipal();

    Date now = new Date();
    Date validity = new Date(now.getTime() + this.tokenValidityDay * 24 * 3600 * 1000);

    return Jwts.builder()
        .setSubject(authentication.getName())
        .claim(AUTHORITIES_KEY, authorities)
        .claim("userId", principal.getUserId())
        .signWith(SignatureAlgorithm.HS512, secretKey)
        .setIssuedAt(now)
        .setExpiration(validity)
        .compact();
  }

  public Authentication getAuthentication(String token) {
    Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();

    Collection<? extends GrantedAuthority> authorities =
        Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

    Object userIdObject = claims.get("userId");
    Long userId = userIdObject == null ? null : Long.valueOf(claims.get("userId").toString());
    User loginUser = userRepository.findOneById(userId);
    if (loginUser == null) {
      throw new AuthorizationServiceException("用户不存在或已删除");
    }
    CurrentUser principal =
        new CurrentUser(
            claims.getSubject(),
            "",
            authorities,
            userId,
            claims.getIssuedAt(),
            loginUser.getProjectId());
    return new UsernamePasswordAuthenticationToken(principal, token, authorities);
  }

  public boolean validateToken(String authToken) {
    try {
      Jwts.parser().setSigningKey(secretKey).parseClaimsJws(authToken);
      return true;
    } catch (SignatureException e) {
      log.info("Invalid JWT signature.");
      log.trace("Invalid JWT signature trace: ", e);
    } catch (MalformedJwtException e) {
      log.info("Invalid JWT token.");
      log.trace("Invalid JWT token trace: ", e);
    } catch (ExpiredJwtException e) {
      log.info("Expired JWT token.");
      log.trace("Expired JWT token trace: ", e);
    } catch (UnsupportedJwtException e) {
      log.info("Unsupported JWT token.");
      log.trace("Unsupported JWT token trace: ", e);
    } catch (IllegalArgumentException e) {
      log.info("JWT token compact of handler are invalid.");
      log.trace("JWT token compact of handler are invalid trace: ", e);
    }
    return false;
  }
}
