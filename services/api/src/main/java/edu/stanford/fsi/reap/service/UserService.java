package edu.stanford.fsi.reap.service;

import edu.stanford.fsi.reap.dto.AdminDTO;
import edu.stanford.fsi.reap.entity.CommunityHouseWorker;
import edu.stanford.fsi.reap.entity.User;
import edu.stanford.fsi.reap.repository.CommunityHouseWorkerRepository;
import edu.stanford.fsi.reap.repository.UserRepository;
import edu.stanford.fsi.reap.security.SecurityUtils;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hookszhang
 */
@Slf4j
@Service
@Transactional
public class UserService {

  private final UserRepository userRepository;
  private final CommunityHouseWorkerRepository chwRepository;
  private final PasswordEncoder passwordEncoder;
  private final BabyService babyService;
  private final TagService tagService;

  public UserService(
      UserRepository userRepository,
      CommunityHouseWorkerRepository chwRepository,
      PasswordEncoder passwordEncoder,
      BabyService babyService,
      TagService tagService) {
    this.userRepository = userRepository;
    this.chwRepository = chwRepository;
    this.passwordEncoder = passwordEncoder;
    this.babyService = babyService;
    this.tagService = tagService;
  }

  public AdminDTO getAdminUser(Long projectId) {
    List<User> users = userRepository.findByProjectId(projectId);
    if (users.isEmpty()) {
      return null;
    } else {
      return AdminDTO.transferTo(users.get(0));
    }
  }

  public User save(User user) {
    saveChw(user);
    String encryptedPassword = passwordEncoder.encode(user.getPassword());
    user.setPassword(encryptedPassword);
    return userRepository.save(user);
  }

  public User update(User user) {
    saveChw(user);
    return userRepository.save(user);
  }

  private void saveChw(User user) {
    if (user.roleChw()) {
      tagService.saveAll(user.getChw().getTags());
      CommunityHouseWorker communityHouseWorker = user.getChw();
      chwRepository.save(communityHouseWorker);
    }
  }

  @Transactional(readOnly = true)
  public Optional<User> getCurrentUser() {
    return userRepository.findOneByUsername(SecurityUtils.getUsername());
  }

  @Transactional(readOnly = true)
  public User getCurrentLogin() {
    Optional<User> optional = getCurrentUser();
    if (!optional.isPresent()) {
      throw new RuntimeException("user not logged in, username: " + SecurityUtils.getUsername());
    }

    return optional.get();
  }

  public void changePassword(String password) {
    getCurrentUser().ifPresent(user -> changePassword(user, password));
  }

  public void changePassword(Long id, String password) {
    userRepository.findById(id).ifPresent(user -> changePassword(user, password));
  }

  private void changePassword(User user, String newPassword) {
    String encryptedPassword = passwordEncoder.encode(newPassword);
    user.setPassword(encryptedPassword);
    user.setLastModifiedPasswordAt(LocalDateTime.now());
    userRepository.save(user);
    log.info("Changed password for User: {}", user);
  }

  public void resetPassword(User user, String defaultPassword) {
    String encryptedPassword = passwordEncoder.encode(defaultPassword);
    user.setPassword(encryptedPassword);
    userRepository.save(user);
    log.info("Reset password for User: {}", user);
  }

  public void assignChwToSupervisor(User supervisor, Long[] chwIds) {
    Arrays.stream(chwIds)
        .forEach(
            chwId -> {
              userRepository
                  .findById(chwId)
                  .ifPresent(
                      user -> {
                        if (!user.roleChw()) return;

                        CommunityHouseWorker chw = user.getChw();

                        if (chw == null) {
                          log.warn("user chw {} not have relation chw ", user.getId());
                          return;
                        }

                        chw.setSupervisor(supervisor);
                        chwRepository.save(chw);
                      });
            });
  }

  public void releaseChwSupervisor(User chwUser) {
    CommunityHouseWorker chw = chwUser.getChw();
    if (chw.getProjectId() == null) {
      chwUser.setProjectId(chwUser.getProjectId());
    }
    chw.setSupervisor(null);
    chwRepository.save(chw);
  }

  public void deleteChwUserAndTurnOverBabies(Long id, Long takeOverUserId) {
    userRepository
        .findById(id)
        .ifPresent(
            user -> {
              babyService.turnOverChwAllBabiesToOtherChw(id, takeOverUserId);
              userRepository.deleteById(id);
              chwRepository.deleteById(user.getChw().getId());
              log.info("Close account chw user id: {}, take over user id: {}", id, takeOverUserId);
            });
  }

  public void deleteSupervisorAndReleaseAllChw(Long id) {
    userRepository
        .findById(id)
        .ifPresent(
            supervisor -> {
              List<CommunityHouseWorker> chwList = chwRepository.findBySupervisorId(id);
              chwList.forEach(
                  chw -> {
                    chw.setSupervisor(null);
                    chwRepository.save(chw);
                  });
              userRepository.deleteById(id);
              log.info(
                  "Close account supervisor user id: {}, number of release chw: {}",
                  id,
                  chwList.size());
            });
  }
}
