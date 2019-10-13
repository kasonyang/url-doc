package site.kason.urldoc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.apache.commons.io.FileUtils;
import site.kason.tempera.engine.Configuration;
import site.kason.tempera.engine.Engine;
import site.kason.tempera.engine.Template;
import site.kason.tempera.loader.ClasspathTemplateLoader;
import site.kason.tempera.loader.StringTemplateLoader;

/**
 *
 * @author Kason Yang
 */
@SupportedAnnotationTypes("org.springframework.web.bind.annotation.RequestMapping")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AnnotationProcessor extends AbstractProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    //System.out.println("processor called");
    if(roundEnv.processingOver()) return false;
    String outFile = System.getProperty("urldoc.out.file");
    String tplFileProperty = System.getProperty("urldoc.template.file");
    if(outFile==null) return false;
    Set<? extends Element> rootEles = roundEnv.getRootElements();
    //System.out.println("root elements:" + rootEles);
    UrldocElementVisitor ev = new UrldocElementVisitor(this.processingEnv);
    for(Element r:rootEles){
      ev.visit(r);
    }
    List<Mapping> mappings = ev.getMappings();
    if (mappings.isEmpty()) {
        return false;
    }
    Configuration conf = new Configuration();
    String tplName = "site.kason.urldoc.template.urls";
    if(tplFileProperty!=null){
      try {
        String tplContent = FileUtils.readFileToString(new File(tplFileProperty), "utf-8");
        StringTemplateLoader tplLoader = new StringTemplateLoader();
        tplLoader.addSource(tplName, tplContent);
        conf.setTemplateLoader(tplLoader);
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }else{
      conf.setTemplateLoader(new ClasspathTemplateLoader(new String[]{".tpr"}));
    }
    Engine engine = new Engine(conf);
    try {
      Template tpl = engine.compile("site.kason.urldoc.templates.urls");
      Map<String,Object> data = new HashMap();
      data.put("mappings", mappings);
      try(FileOutputStream fos = new FileOutputStream(outFile)){
        String out = tpl.render(data);
        fos.write(out.getBytes("utf-8"));
        fos.flush();
        //System.out.println(out);
      }
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    return false;
  }

}
