package edu.stanford.fsi.reap.pojo;

import edu.stanford.fsi.reap.entity.enumerations.ModuleComponentType;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hookszhang
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Component implements Serializable {
  private ModuleComponentType type;
  private Long key;
  private Object value;

  public boolean mediaType() {
    return ModuleComponentType.Media.equals(type);
  }

  public boolean switchType() {
    return ModuleComponentType.Switch.equals(type);
  }

  public boolean pageFooterType() {
    return ModuleComponentType.PageFooter.equals(type);
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Media {
    private String type;
    private String file;
    private String text;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Switch {
    private Text question;
    private List<Case> cases;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Text {
    private String type;
    private String html;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Case {
    private Long key;
    private String text;
    private List<String> finishAction;
    private List<Component> components;
  }
}
