package edu.stanford.fsi.reap.web.rest.admin;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import edu.stanford.fsi.reap.config.Constants;
import edu.stanford.fsi.reap.dto.*;
import edu.stanford.fsi.reap.entity.Baby;
import edu.stanford.fsi.reap.entity.CommunityHouseWorker;
import edu.stanford.fsi.reap.entity.User;
import edu.stanford.fsi.reap.entity.enumerations.VisitStatus;
import edu.stanford.fsi.reap.repository.*;
import edu.stanford.fsi.reap.security.AuthoritiesConstants;
import edu.stanford.fsi.reap.security.SecurityUtils;
import edu.stanford.fsi.reap.service.BabyService;
import edu.stanford.fsi.reap.service.ExcelService;
import edu.stanford.fsi.reap.service.UserService;
import edu.stanford.fsi.reap.web.rest.errors.BadRequestAlertException;
import edu.stanford.fsi.reap.web.rest.errors.LoginAlreadyUsedException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/users")
@Slf4j
public class UserResource {

  private final BabyService babyService;
  private final BabyRepository babyRepository;
  private final UserService userService;
  private final UserRepository userRepository;
  private final CommunityHouseWorkerRepository chwRepository;
  private final ModelMapper modelMapper;
  private final ChwUserRepository chwUserRepository;
  private final VisitRepository visitRepository;
  private final ExcelService excelService;

  public UserResource(
      BabyService babyService,
      BabyRepository babyRepository,
      UserService userService,
      UserRepository userRepository,
      CommunityHouseWorkerRepository chwRepository,
      ModelMapper modelMapper,
      ChwUserRepository chwUserRepository,
      VisitRepository visitRepository,
      ExcelService excelService) {
    this.babyService = babyService;
    this.babyRepository = babyRepository;
    this.userService = userService;
    this.userRepository = userRepository;
    this.chwRepository = chwRepository;
    this.modelMapper = modelMapper;
    this.chwUserRepository = chwUserRepository;
    this.visitRepository = visitRepository;
    this.excelService = excelService;
  }

  @GetMapping("/project/{id}")
  public ResponseEntity<AdminDTO> getAdminUser(@PathVariable("id") Long projectId) {
    return ResponseEntity.ok(userService.getAdminUser(projectId));
  }

  @PostMapping("/check")
  public Map<String, Object> check(
      @RequestParam(name = "records") MultipartFile records,
      @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "en") String lang) {
    if (records.isEmpty()) {
      throw new BadRequestAlertException("error.excel.empty");
    }
    return excelService.checkChws(records, lang);
  }

  @PostMapping("/import")
  public ResponseEntity importChws(
      @RequestParam(name = "records") MultipartFile records,
      @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "en") String lang) {

    excelService.importChws(records, lang);
    return ResponseEntity.ok().build();
  }

  private void keepCHWIdentityUnique(CommunityHouseWorker chw) {
    if (chw == null) return;
    chwRepository
        .findFirstByIdentity(chw.getIdentity())
        .ifPresent(
            consumer -> {
              if (!consumer.getId().equals(chw.getId())) {
                throw new BadRequestAlertException("error.chw.identity.exists", chw.getIdentity());
              }
            });
  }

  @PostMapping
  public ResponseEntity<User> createUser(@Valid @RequestBody UserDTO userDTO)
      throws URISyntaxException {
    if (!SecurityUtils.hasAuthorityAdmin() && !userDTO.roleChw()) {
      throw new AccessDeniedException("You cannot add users other than chw roles");
    }
    if (userRepository.findOneByUsername(userDTO.getUsername()).isPresent()
        || userRepository.findCountByUsernameAndDeletedTrue(userDTO.getUsername()) > 0) {
      throw new LoginAlreadyUsedException(userDTO.getUsername());
    }

    if (userDTO.roleChw() && userDTO.chwIsEmpty()) {
      throw new BadRequestAlertException("error.user.chwIdentityRequired");
    }
    keepCHWIdentityUnique(userDTO.getChw());

    User user = modelMapper.map(userDTO, User.class);
    if (!SecurityUtils.hasAuthorityAdmin() && userDTO.roleChw()) {
      user.getChw().setSupervisor(userService.getCurrentLogin());
    }
    if (user.getProjectId() == null || user.getProjectId() <= 0) {
      user.setProjectId(SecurityUtils.getProjectId());
    }

    user = userService.save(user);
    return ResponseEntity.created(new URI("/api/users/" + user.getId())).body(user);
  }

  @PostMapping("createUsersInBatch")
  public ResponseEntity<List<User>> createUsersInBatch(@Valid @RequestBody UserDTO userDTO) {

    return ResponseEntity.ok().build();
  }

  @PutMapping("/{id}")
  public void updateUser(@PathVariable Long id, @Valid @RequestBody ChangeUserVM vm) {
    userRepository
        .findById(id)
        .ifPresent(
            user -> {
              if (!SecurityUtils.hasAuthorityAdmin() && !ownResponsibleChw(user)) {
                throw new AccessDeniedException("You cannot modify someone else's chw");
              }

              // you can update the tags that the user role is CHW
              if (user.roleChw()) {
                user.getChw().setIdentity(vm.getChw().getIdentity());
                user.getChw().setTags(vm.getChw().getTags());
                keepCHWIdentityUnique(user.getChw());
              }
              user.setRealName(vm.realName);
              user.setPhone(vm.phone);
              userService.update(user);
            });
  }

  @Data
  static class ChangeUserVM {
    @NotNull @Size(min = 1, max = 50)
    private String realName;

    @NotNull private String phone;

    @Valid private CommunityHouseWorker chw;
  }

  @GetMapping
  public Page<User> getUsers(Pageable pageable, String role) {
    User user = User.builder().role(role).build();
    user.setProjectId(SecurityUtils.getProjectId());
    return userRepository.findAll(Example.of(user), pageable);
  }

  @PutMapping("/{id}/password")
  public void changeUserPassword(@PathVariable Long id, @Valid @RequestBody PasswordVM passwordVM) {
    userService.changePassword(id, passwordVM.getPassword());
  }

  @Data
  static class PasswordVM {
    @NotNull @Size(min = Constants.PASSWORD_MIN_LENGTH, max = Constants.PASSWORD_MAX_LENGTH)
    private String password;
  }

  @GetMapping("/chw/{id}/babies")
  public List<AssignBabyDTO> getBabiesByChwId(@PathVariable Long id) {
    return babyRepository.findAssignBabyByChwId(id);
  }

  @PostMapping("/chw/{id}/babies")
  public void assignBabyToChw(@PathVariable Long id, @RequestBody Long[] babyIds) {
    chwUserRepository.findById(id).ifPresent(chw -> babyService.assignChw(chw, babyIds));
  }

  @GetMapping("/chw/not_assigned/babies")
  public Page<Baby> getNotAssignedBabies(String search, Pageable pageable) {
    return babyRepository.findByChwIsNullAndSearchOrderByIdDesc(
        search, SecurityUtils.getProjectId(), pageable);
  }

  @GetMapping("/chw")
  public Page<ChwUserDTO> getChwList(String search, Pageable pageable) {
    Long supervisorId = null;
    if (!SecurityUtils.hasAuthorityAdmin()) {
      supervisorId = SecurityUtils.getUserId();
    }
    Page<ChwUserDTO> result =
        userRepository.findChwBySearch(
            search, supervisorId, SecurityUtils.getProjectId(), pageable);
    if (ObjectUtil.isNotEmpty(result) && CollUtil.isNotEmpty(result.getContent())) {
      result
          .getContent()
          .forEach(
              target -> {
                target.setShouldFinish(visitRepository.countByChw(target.getUser()));
                target.setHasFinish(
                    visitRepository.countByChwAndStatus(target.getUser(), VisitStatus.DONE));
              });
    }
    return result;
  }

  @DeleteMapping("/chw/{id}")
  public void deleteChw(@PathVariable Long id, @Valid @RequestBody TakeOverWrapper wrapper) {
    userService.deleteChwUserAndTurnOverBabies(id, wrapper.getUserId());
  }

  @Data
  public static class TakeOverWrapper {
    private Long userId;
  }

  @DeleteMapping("/chw/{id}/supervisor")
  public void releaseChwSupervisor(@PathVariable Long id) {
    userRepository.findById(id).ifPresent(userService::releaseChwSupervisor);
  }

  @GetMapping("/supervisor/not_assigned/chw")
  public List<User> getNotAssignedChwList(String search) {
    return userRepository.findNotAssignedChwBySearch(search, SecurityUtils.getProjectId());
  }

  @GetMapping("/supervisor")
  public Page<SupervisorUserDTO> getSupervisorList(Pageable pageable) {
    return userRepository.findAllSupervisor(SecurityUtils.getProjectId(), pageable);
  }

  @DeleteMapping("/supervisor/{id}")
  public void deleteSupervisor(@PathVariable Long id) {
    userService.deleteSupervisorAndReleaseAllChw(id);
  }

  @GetMapping("/supervisor/{id}/chw")
  public List<User> getSupervisorChwList(@PathVariable Long id) {
    return userRepository
        .findById(id)
        .map(userRepository::findAllByChwSupervisor)
        .orElse(new ArrayList<>());
  }

  @PostMapping("/supervisor/{id}/chw")
  public void assignChwToSupervisor(@PathVariable Long id, @RequestBody Long[] chwIds) {
    userRepository
        .findById(id)
        .ifPresent(
            user -> {
              if (user.roleSupervisor()) {
                userService.assignChwToSupervisor(user, chwIds);
              } else {
                log.warn("be assign user {} role is not ROLE_SUPERVISOR", id);
              }
            });
  }

  @GetMapping("/admin")
  public Page<User> getAdminList(Pageable pageable) {
    User user = User.builder().role(AuthoritiesConstants.ADMIN).build();
    user.setProjectId(SecurityUtils.getProjectId());
    return userRepository.findAll(Example.of(user), pageable);
  }

  @GetMapping("/{id}")
  public ResponseEntity<User> getUser(@PathVariable Long id) {
    return userRepository
        .findById(id)
        .filter(user -> SecurityUtils.hasAuthorityAdmin() || ownResponsibleChw(user))
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  private boolean ownResponsibleChw(User user) {
    if (!user.roleChw()) return false;
    User supervisor = user.getChw().getSupervisor();
    return supervisor != null && SecurityUtils.getUserId().equals(supervisor.getId());
  }
}
