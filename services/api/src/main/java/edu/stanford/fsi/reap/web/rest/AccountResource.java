package edu.stanford.fsi.reap.web.rest;

import edu.stanford.fsi.reap.dto.PasswordWrapper;
import edu.stanford.fsi.reap.dto.ProfileWrapper;
import edu.stanford.fsi.reap.entity.User;
import edu.stanford.fsi.reap.repository.UserRepository;
import edu.stanford.fsi.reap.service.UserService;
import edu.stanford.fsi.reap.web.rest.errors.BadRequestAlertException;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * @author hookszhang
 */
@RequestMapping("/api/account")
@RestController
public class AccountResource {

  private final UserService userService;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AccountResource(
      UserService userService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userService = userService;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @GetMapping("encode/{psw}")
  public String generatePswEncode(@PathVariable("psw") String psw) {
    return passwordEncoder.encode(psw);
  }

  @GetMapping("/profile")
  public ResponseEntity<User> profile() {
    return ResponseEntity.ok(userService.getCurrentLogin());
  }

  @PutMapping("/profile")
  public void changeProfile(@Valid @RequestBody ProfileWrapper profileWrapper) {
    userService
        .getCurrentUser()
        .ifPresent(
            user -> {
              user.setRealName(profileWrapper.getRealName());
              user.setPhone(profileWrapper.getPhone());
              userRepository.save(user);
            });
  }

  @PutMapping("/password")
  public void changeAccountPassword(@Valid @RequestBody PasswordWrapper passwordWrapper) {
    String passwordHash = userService.getCurrentLogin().getPassword();
    if (!passwordEncoder.matches(passwordWrapper.getOldPassword(), passwordHash)) {
      throw new BadRequestAlertException("error.wrong.old.password");
    }
    userService.changePassword(passwordWrapper.getPassword());
  }
}
