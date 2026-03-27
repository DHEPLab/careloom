package edu.stanford.fsi.reap.service;

import edu.stanford.fsi.reap.entity.Tag;
import edu.stanford.fsi.reap.repository.TagRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hookszhang
 */
@Service
@Transactional
public class TagService {

  private final TagRepository repository;

  public TagService(TagRepository repository) {
    this.repository = repository;
  }

  public void saveAll(List<String> tags) {
    tags.forEach(
        tagName -> {
          if (!repository.findFirstByName(tagName).isPresent()) {
            Tag tag = new Tag(tagName);
            repository.save(tag);
          }
        });
  }
}
