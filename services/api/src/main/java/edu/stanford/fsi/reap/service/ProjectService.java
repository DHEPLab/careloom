package edu.stanford.fsi.reap.service;

import cn.hutool.core.util.RandomUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.fsi.reap.config.Constants;
import edu.stanford.fsi.reap.dto.ProjectCreateResultDTO;
import edu.stanford.fsi.reap.dto.ProjectDTO;
import edu.stanford.fsi.reap.dto.ProjectStatusDTO;
import edu.stanford.fsi.reap.entity.Project;
import edu.stanford.fsi.reap.entity.User;
import edu.stanford.fsi.reap.repository.ProjectRepository;
import edu.stanford.fsi.reap.repository.UserRepository;
import edu.stanford.fsi.reap.security.AuthoritiesConstants;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
@Slf4j
public class ProjectService {

  private final ProjectRepository repository;
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;

  private final ObjectMapper objectMapper = new ObjectMapper();

  public ProjectService(
      ProjectRepository repository,
      PasswordEncoder passwordEncoder,
      UserRepository userRepository) {
    this.repository = repository;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public Page<Project> findBySearch(Pageable pageable) {
    return repository.findAll(pageable);
  }

  public List<Project> findAll() {
    return repository.findAll(Sort.by("createdAt"));
  }

  public ResponseEntity createProject(ProjectDTO projectDTO) {
    long nameCount = repository.countByName(projectDTO.getName());
    if (nameCount > 0) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("项目名已存在");
    }
    Project project = objectMapper.convertValue(projectDTO, Project.class);
    if (projectDTO.getStatus() == null) {
      project.setStatus(Constants.PROJECT_VALID);
    }
    project.setCode(UUID.randomUUID().toString());
    repository.save(project);
    User admin = new User();
    admin.setProjectId(project.getId());
    admin.setRole(AuthoritiesConstants.ADMIN);
    admin.setRealName("admin");
    String username = "admin_" + RandomUtil.randomString(5);
    admin.setUsername(username);
    String originPsw = username;
    admin.setPassword(passwordEncoder.encode(originPsw));
    admin.setPhone(RandomUtil.randomNumbers(13));
    userRepository.save(admin);
    project.setUserId(admin.getId());
    repository.save(project);
    ProjectCreateResultDTO projectCreateResult =
        ProjectCreateResultDTO.builder()
            .projectId(project.getId())
            .username(admin.getUsername())
            .password(originPsw)
            .build();
    return ResponseEntity.ok().body(projectCreateResult);
  }

  public ResponseEntity<Long> updateProject(Long id, ProjectDTO projectDTO) {
    Optional<Project> project = repository.findById(id);
    project.ifPresent(
        target -> {
          if (!target.getCode().equals(Constants.DEFAULT_PROJECT)) {
            if (!StringUtils.isEmpty(projectDTO.getName())) {
              target.setName(projectDTO.getName());
            }
            if (projectDTO.getStatus() != null) {
              target.setStatus(projectDTO.getStatus());
            }
            repository.save(target);
          }
        });
    return ResponseEntity.ok().body(id);
  }

  public void deleteProject(Long id) {
    Optional<Project> project = repository.findById(id);
    project.ifPresent(
        target -> {
          if (!target.getCode().equals(Constants.DEFAULT_PROJECT)) {
            repository.deleteById(id);
          }
        });
  }

  public void updateProjectStatus(Long id, ProjectStatusDTO projectStatusDTO) {
    Optional<Project> project = repository.findById(id);
    project.ifPresent(
        target -> {
          if (projectStatusDTO.getStatus() != null) {
            target.setStatus(projectStatusDTO.getStatus());
            repository.save(target);
          }
        });
  }
}
