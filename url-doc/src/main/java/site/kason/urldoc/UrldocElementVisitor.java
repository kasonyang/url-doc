package site.kason.urldoc;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.util.AbstractElementVisitor8;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
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
        //Controller controller = e.getAnnotation(Controller.class);
        //RestController restController = e.getAnnotation(RestController.class);
        //boolean isController = controller!=null || restController!=null;
        //System.out.println("visiting type:" + e);
        RequestMapping rm = e.getAnnotation(RequestMapping.class);
        String[] basePath;
        if (rm != null) {
            basePath = rm.value();
        } else {
            basePath = new String[]{"/"};
        }
        for (Element ee : e.getEnclosedElements()) {
            if (ee instanceof TypeElement) {
                ee.accept(this, p);
            } else if (ee instanceof ExecutableElement) {
                ee.accept(this, basePath);
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
        String controller = e.getEnclosingElement().asType().toString();
        for (RequestMapping rm : e.getAnnotationsByType(RequestMapping.class)) {
            RequestMethod[] reqMethods = rm.method();
            String[] methods = new String[reqMethods.length];
            for (int i = 0; i < methods.length; i++) {
                methods[i] = reqMethods[i].name();
            }
            mappings.add(createMapping(methods, rm.path(),rm.value(), basePath, doc, controller));
        }
        for (GetMapping gm : e.getAnnotationsByType(GetMapping.class)) {
            mappings.add(createMapping(new String[]{RequestMethod.GET.name()}, gm.path(), gm.value(), basePath, doc, controller));
        }
        for (PostMapping pm : e.getAnnotationsByType(PostMapping.class)) {
            mappings.add(createMapping(new String[]{RequestMethod.POST.name()}, pm.path(), pm.value(), basePath, doc, controller));
        }
        for (VariableElement pv : e.getParameters()) {
            String ptype = pv.asType().toString();
            String pname = pv.getSimpleName().toString();
            if (this.isAutowiredParameter(ptype)) continue;
            if (pv.getAnnotation(PathVariable.class) != null) continue;
            RequestParam reqParam = pv.getAnnotation(RequestParam.class);
            for (Mapping m : mappings) {
                m.addParameter(this.createParameter(reqParam, pname, ptype));
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

    public List<Mapping> getMappings() {
        return new ArrayList(this.mappingList);
    }

    private Mapping createMapping(String[] methods,String[] paths, String[] values, String[] basePaths, String doc, String controller) {
        Mapping mp = new Mapping();
        mp.setDoc(doc);
        mp.setController(controller);
        if (methods.length == 0) {
            mp.addMethod("ANY");
        } else {
            for (String m : methods) {
                mp.addMethod(m);
            }
        }
        for (String bp : basePaths) {
            for (String p : paths) {
                mp.addPath(path(bp, p));
            }
            for (String p : values) {
                mp.addPath(path(bp, p));
            }
        }
        return mp;
    }

    private String path(String p1, String p2) {
        if (p1 == null) p1 = "";
        if (p2 == null) p2 = "";
        if (p1.endsWith("/")) p1 = p1.substring(0, p1.length() - 1);
        if (p2.startsWith("/")) p2 = p2.substring(1);
        String url = p1 + "/" + p2;
        if (!url.startsWith("/")) {
            url = "/" + url;
        }
        return url;
    }

    private boolean isAutowiredParameter(String type) {
        return type.startsWith("org.springframework.") || AUTOWIRED_TYPE_LIST.contains(type);
    }

    private Parameter createParameter(@Nullable RequestParam reqParam, String defaultName, String type) {
        boolean required = false;
        if (reqParam != null) {
            required = reqParam.required();
            String name = reqParam.name();
            String value = reqParam.value();
            if (name != null && !name.isEmpty()) {
                defaultName = name;
            } else if (value != null && !value.isEmpty()) {
                defaultName = name;
            }
        }
        return new Parameter(defaultName, type, required);
    }

}
