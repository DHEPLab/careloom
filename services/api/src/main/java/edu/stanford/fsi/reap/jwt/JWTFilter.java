package edu.stanford.fsi.reap.jwt;

import edu.stanford.fsi.reap.pojo.SimpleUser;
import edu.stanford.fsi.reap.repository.UserRepository;
import edu.stanford.fsi.reap.security.CurrentUser;
import edu.stanford.fsi.reap.security.SecurityUtils;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Filters incoming requests and installs a Spring Security principal if a header corresponding to a
 * valid user is found.
 */
public class JWTFilter extends GenericFilterBean {

  private final TokenProvider tokenProvider;

  private final UserRepository userRepository;

  public JWTFilter(TokenProvider tokenProvider, UserRepository userRepository) {
    this.tokenProvider = tokenProvider;
    this.userRepository = userRepository;
  }

  @Override
  public void doFilter(
      ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {
    HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
    String jwt = resolveToken(httpServletRequest);

    if (!StringUtils.hasText(jwt) || !this.tokenProvider.validateToken(jwt)) {
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }

    Authentication authentication = this.tokenProvider.getAuthentication(jwt);
    CurrentUser user = SecurityUtils.getUser(authentication);
    Optional<SimpleUser> userOptional = userRepository.findSimpleById(user.getUserId());

    if (!userOptional.isPresent()) {
      logger.warn(
          "Invalid jwt token because the user does not exist, user id: " + user.getUserId());
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }

    if (user.issuedBeforeModifiedPassword(userOptional.get().getLastModifiedPasswordAt())) {
      logger.warn(
          "The password has changed since the jwt token was issued, user id: "
              + user.getUserId()
              + ", issued at: "
              + user.getIssuedAt()
              + ", last modified password at: "
              + userOptional.get().getLastModifiedPasswordAt());
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }

    SecurityContextHolder.getContext().setAuthentication(authentication);
    filterChain.doFilter(servletRequest, servletResponse);
  }

  private String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader(JWTConfigurer.AUTHORIZATION_HEADER);
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
