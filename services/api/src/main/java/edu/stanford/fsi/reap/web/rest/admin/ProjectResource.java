package edu.stanford.fsi.reap.web.rest.admin;

import edu.stanford.fsi.reap.dto.ProjectDTO;
import edu.stanford.fsi.reap.dto.ProjectStatusDTO;
import edu.stanford.fsi.reap.entity.Project;
import edu.stanford.fsi.reap.service.ProjectService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin/project")
public class ProjectResource {

  private final ProjectService service;

  public ProjectResource(ProjectService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<Page<Project>> getProjects(Pageable pageable) {
    return ResponseEntity.ok().body(service.findBySearch(pageable));
  }

  @GetMapping("all")
  public ResponseEntity<List<Project>> getAll() {
    return ResponseEntity.ok().body(service.findAll());
  }

  @PostMapping("create")
  public ResponseEntity<Project> createProject(@Valid @RequestBody ProjectDTO projectDTO) {
    return service.createProject(projectDTO);
  }

  @PostMapping("update/{id}")
  public ResponseEntity<Long> updateProject(
      @PathVariable(value = "id") Long id, @Valid @RequestBody ProjectDTO projectDTO) {
    return service.updateProject(id, projectDTO);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity deleteById(@PathVariable(value = "id") Long id) {
    service.deleteProject(id);
    return ResponseEntity.ok().build();
  }

  @PostMapping("update/status/{id}")
  public void updateProjectStatus(
      @PathVariable(value = "id") Long id, @Valid @RequestBody ProjectStatusDTO projectStatusDTO) {
    service.updateProjectStatus(id, projectStatusDTO);
  }
}
