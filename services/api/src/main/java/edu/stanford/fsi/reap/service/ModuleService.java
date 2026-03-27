package edu.stanford.fsi.reap.service;

import static edu.stanford.fsi.reap.entity.enumerations.CurriculumBranch.MASTER;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.fsi.reap.entity.Module;
import edu.stanford.fsi.reap.entity.enumerations.CurriculumBranch;
import edu.stanford.fsi.reap.pojo.Component;
import edu.stanford.fsi.reap.repository.ModuleRepository;
import edu.stanford.fsi.reap.security.SecurityUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hookszhang
 */
@Service
@Transactional
public class ModuleService {

  private final ModuleRepository repository;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final LessonService lessonService;

  public ModuleService(ModuleRepository repository, LessonService lessonService) {
    this.repository = repository;
    this.lessonService = lessonService;
  }

  private String generateVersionKey() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  /**
   * For the first time publish module, generate a 32bit version key. or else set the original
   * version key
   *
   * @param module Published Module
   */
  public Module publish(Module module) {

    if (module.getId() == null) {
      module.setVersionKey(generateVersionKey());
      module.setBranch(MASTER);
      module.setPublished(true);
      if (module.getProjectId() == null) {
        module.setProjectId(SecurityUtils.getProjectId());
      }
      return repository.save(module);
    }

    return repository
        .findById(module.getId())
        .map(
            db -> {
              if (CurriculumBranch.DRAFT.equals(db.getBranch())) {
                module.setVersionKey(db.getVersionKey());
                return publishDraftBranch(module);
              }

              renameLessonModuleLabel(module, db);
              // Module that directly update their published status
              copy(module, db);
              db.setPublished(true);
              if (module.getProjectId() == null) {
                module.setProjectId(SecurityUtils.getProjectId());
              }
              return repository.save(db);
            })
        .orElse(null);
  }

  private void renameLessonModuleLabel(Module current, Module old) {
    if (!old.getNumber().equals(current.getNumber())) {
      // synchronously modify the label of visit modules
      lessonService.renameModuleLabel(old.getId(), current.getNumber());
    }
  }

  /**
   * Publish a draft, copy draft field to published module then delete draft
   *
   * @param draft Draft Module
   * @return Published Module
   */
  private Module publishDraftBranch(Module draft) {
    return repository
        .findOneByVersionKeyAndBranch(draft.getVersionKey(), MASTER)
        .map(
            publishedModule -> {
              renameLessonModuleLabel(draft, publishedModule);
              copy(draft, publishedModule);
              repository.deleteById(draft.getId());
              return repository.save(publishedModule);
            })
        .orElse(null);
  }

  public Module draft(Module draft) {
    if (draft.getId() == null) {
      draft.setVersionKey(generateVersionKey());
      draft.setBranch(MASTER);
      draft.setProjectId(SecurityUtils.getProjectId());
      return repository.save(draft);
    }

    return repository
        .findById(draft.getId())
        .map(
            db -> {
              if (MASTER.equals(db.getBranch()) && db.isPublished()) {
                draft.setId(null);
                draft.setVersionKey(db.getVersionKey());
                draft.setBranch(CurriculumBranch.DRAFT);
                return repository.save(draft);
              }
              copy(draft, db);
              return repository.save(db);
            })
        .orElse(null);
  }

  private void copy(Module from, Module to) {
    to.setNumber(from.getNumber());
    to.setName(from.getName());
    to.setDescription(from.getDescription());
    to.setTopic(from.getTopic());
    to.setComponents(from.getComponents());
  }

  public List<String> media(List<Component> components) {
    List<String> media = new ArrayList<>();
    components.forEach(
        component -> {
          if (component.mediaType()) {
            Component.Media value =
                objectMapper.convertValue(component.getValue(), Component.Media.class);
            if (value != null && StrUtil.isNotEmpty(value.getFile())) {
              media.add(value.getFile());
            }
          }

          if (component.switchType()) {
            Component.Switch value =
                objectMapper.convertValue(component.getValue(), Component.Switch.class);
            value.getCases().forEach(aCase -> media.addAll(media(aCase.getComponents())));
          }
        });
    return media;
  }

  public List<Component> cleanUpTheExtraPageFooter(List<Component> components) {
    if (components.size() == 0) return components;

    List<Component> list = new ArrayList<>(components);

    // remove first component if it type is page footer
    Component firstComponent = list.get(0);
    while (firstComponent.pageFooterType()) {
      list.remove(firstComponent);
      firstComponent = list.get(0);
    }

    // remove last component if it type is page footer
    Component lastComponent = list.get(list.size() - 1);
    while (lastComponent.pageFooterType()) {
      list.remove(lastComponent);
      lastComponent = list.get(list.size() - 1);
    }

    Iterator<Component> iterator = list.iterator();
    Component prev = null;
    while (iterator.hasNext()) {
      Component next = iterator.next();
      // remove the page footer following the switch component or page footer
      if (prev != null
          && ((prev.switchType() && next.pageFooterType())
              || (prev.pageFooterType() && next.pageFooterType()))) {
        iterator.remove();
      }

      prev = next;
    }

    return list;
  }
}
