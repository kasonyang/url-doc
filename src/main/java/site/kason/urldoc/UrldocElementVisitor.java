package site.kason.urldoc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.AbstractElementVisitor8;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author Kason Yang
 */
public class UrldocElementVisitor extends AbstractElementVisitor8<Object, Object> {
  
  private final static List<String> AUTOWIRED_TYPE_LIST = Arrays.asList(
          "javax.servlet.http.HttpServletRequest",
          "javax.servlet.ServletRequest",
          "javax.portlet.ActionRequest",
          "javax.portlet.RenderRequest",
          "javax.servlet.http.HttpSession",
          "javax.portlet.PortletSession",
          "java.util.Locale",
          "java.io.InputStream",
          "java.io.OutputStream",
          "java.util.Map"
    );

  private final ProcessingEnvironment processingEnv;
  
  private final List<Mapping> mappingList = new LinkedList();

  UrldocElementVisitor(ProcessingEnvironment processingEnv) {
    this.processingEnv = processingEnv;
  }

  @Override
  public Object visitPackage(PackageElement e, Object p) {
    //System.out.println("visiting package");
    return null;
  }

  @Override
  public Object visitType(TypeElement e, Object p) {
    //System.out.println("visiting type:" + e);
    //TODO check @Controller
    RequestMapping rm = e.getAnnotation(RequestMapping.class);
    String[] basePath;
    if (rm != null) {
      basePath = rm.value();
    } else {
      basePath = new String[]{"/"};
    }
//    for (int i = 0; i < basePath.length; i++) {
//      if (!basePath[i].endsWith("/")) {
//        basePath[i] += "/";
//      }
//      if (!basePath[i].startsWith("/")) {
//        basePath[i] = "/" + basePath[i];
//      }
//    }
    for (Element ee : e.getEnclosedElements()) {
      if (ee instanceof ExecutableElement) {
        ee.accept(this, basePath);
      } else if (ee instanceof TypeElement) {
        ee.accept(this, p);
      }
    }
    return null;
  }

  @Override
  public Object visitVariable(VariableElement e, Object p) {
    //System.out.println("visiting variable");
    return null;
  }

  @Override
  public Object visitExecutable(ExecutableElement e, Object p) {
    String[] basePath = (String[]) p;
    //System.out.println("visiting executable:"+e);
    List<Mapping> mappings = new LinkedList();
    String doc = this.processingEnv.getElementUtils().getDocComment(e);
    for (RequestMapping rm : e.getAnnotationsByType(RequestMapping.class)) {
      mappings.add(createMapping(rm, basePath,doc));
    }
    for (GetMapping gm : e.getAnnotationsByType(GetMapping.class)) {
      mappings.add(createMapping(gm, basePath,doc));
    }
    for (PostMapping pm : e.getAnnotationsByType(PostMapping.class)) {
      mappings.add(createMapping(pm, basePath,doc));
    }
    for(VariableElement pv:e.getParameters()){
      String ptype = pv.asType().toString();
      String pname = pv.getSimpleName().toString();
      if(this.isAutowiredParameter(ptype)) continue;
      for(Mapping m:mappings){
        m.addParameter(new Parameter(pname,ptype));
      }
    }
    this.mappingList.addAll(mappings);
    return null;
  }

  @Override
  public Object visitTypeParameter(TypeParameterElement e, Object p) {
    //System.out.println("visiting type parameter");
    return null;
  }
  
  public List<Mapping> getMappings(){
    return new ArrayList(this.mappingList);
  }

  private Mapping createMapping(RequestMapping rm, String[] basePaths,String doc) {
    Mapping mp = new Mapping();
    mp.setDoc(doc);
    for (RequestMethod m : rm.method()) {
      mp.addMethod(m.name());
    }
    for (String bp : basePaths) {
      for (String p : rm.path()) {
        mp.addPath(path(bp, p));
      }
      for (String p : rm.value()) {
        mp.addPath(path(bp, p));
      }
    }

    return mp;
  }

  private Mapping createMapping(PostMapping pm, String[] basePaths,String doc) {
    Mapping mp = new Mapping();
    mp.setDoc(doc);
    mp.addMethod(RequestMethod.POST.name());
    for (String b : basePaths) {
      for (String p : pm.path()) {
        mp.addPath(path(b, p));
      }
      for (String p : pm.value()) {
        mp.addPath(path(b, p));
      }
    }

    return mp;
  }

  private Mapping createMapping(GetMapping gm, String[] basePaths,String doc) {
    Mapping mp = new Mapping();
    mp.setDoc(doc);
    mp.addMethod(RequestMethod.GET.name());
    for (String b : basePaths) {
      for (String p : gm.path()) {
        mp.addPath(path(b, p));
      }
      for (String p : gm.value()) {
        mp.addPath(path(b, p));
      }
    }

    return mp;
  }

  private String path(String p1, String p2) {
    if(p1==null) p1 = "";
    if(p2==null) p2 = "";
    if(p1.endsWith("/")) p1 = p1.substring(0,p1.length()-1);
    if(p2.startsWith("/")) p2 = p2.substring(1);
    String url = p1 + "/" + p2;
    if(!url.startsWith("/")){
      url = "/" + url;
    }
    return url;
  }
  
  private boolean isAutowiredParameter(String type){
    return type.startsWith("org.springframework.") || AUTOWIRED_TYPE_LIST.contains(type);
  }

}
