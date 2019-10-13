package site.kason.urldoc;

/**
 *
 * @author Kason Yang
 */
public class Parameter {

  private String name;

  private String type;
  
  private boolean required;

  public Parameter(String name, String type,boolean required) {
    this.name = name;
    this.type = type;
    this.required = required;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public boolean isRequired() {
    return required;
  }

}
