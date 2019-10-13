package site.kason.urldoc;

import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;

/**
 *
 * @author Kason Yang
 */
public class Mapping {

  private final List<String> methods = new LinkedList();

  private final List<String> paths = new LinkedList();
  
  private final List<Parameter> parameters = new LinkedList();
  
  private String doc;

  public Mapping() {
  }
  
  public void addMethod(String method){
    this.methods.add(method);
  }

  public String[] getMethods() {
    return methods.toArray(new String[methods.size()]);
  }

  public void addPath(String path) {
    paths.add(path);
  }

  public String[] getPaths() {
    return paths.toArray(new String[paths.size()]);
  }
  
  public void addParameter(Parameter p){
    this.parameters.add(p);
  }
  
  public Parameter[] getParameters(){
    return this.parameters.toArray(new Parameter[parameters.size()]);
  }

  @Override
  public String toString() {
    return String.join("|", methods) + " " + String.join(",", paths);
  }

  @Nullable
  public String getDoc() {
    return doc;
  }

  public void setDoc(String doc) {
    this.doc = doc;
  }
  
  

}
