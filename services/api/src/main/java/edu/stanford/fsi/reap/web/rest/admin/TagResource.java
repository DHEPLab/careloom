package edu.stanford.fsi.reap.web.rest.admin;

import edu.stanford.fsi.reap.entity.Tag;
import edu.stanford.fsi.reap.repository.TagRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * @author hookszhang
 */
@RestController
@RequestMapping("/admin/tags")
public class TagResource {

  private final TagRepository repository;

  public TagResource(TagRepository repository) {
    this.repository = repository;
  }

  @GetMapping
  public List<Tag> getAllTags() {
    return repository.findAll(Sort.by(Sort.Direction.DESC, "id"));
  }

  @DeleteMapping("/{id}")
  public void deleteTag(@PathVariable Long id) {
    repository.deleteById(id);
  }
}
