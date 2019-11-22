package io.virtdata.processors;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This annotation processor is responsible for finding all annotated functions and adding
 * them to the manifest file for the current project.
 * Specifically, any class annotated as {@link io.virtdata.annotations.ThreadSafeMapper}
 * or {@link io.virtdata.annotations.PerThreadMapper} are recorded in
 * <pre>classes/META-INF/functions</pre>
 *
 * This operates slightly differently than the service loader facility. The point is to
 * enumerate candidate functions without requiring them to have a no-args constructor.
 */
@SupportedOptions({"title"})
@SupportedSourceVersion(SourceVersion.RELEASE_12)
@SupportedAnnotationTypes({
        "io.virtdata.annotations.ThreadSafeMapper",
        "io.virtdata.annotations.PerThreadMapper"})
public class FunctionManifestProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messenger;
    private Writer writer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.messenger = processingEnv.getMessager();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        List<Element> ts = new ArrayList<>();

        try {
            if (writer==null) {
                writer = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/functions")
                        .openWriter();
            }
            for (String annotationType : this.getSupportedAnnotationTypes()) {
                Class<? extends Annotation> annotationClass =
                        (Class<? extends Annotation>) Class.forName(annotationType);
                Set<? extends Element> tsms = roundEnv.getElementsAnnotatedWith(annotationClass);
                if (tsms.size() > 0) {
                    for (Element e : tsms) {
                        writer.write(((TypeElement) e).getQualifiedName() + "\n");
                    }
                }
            }

            writer.close();

        } catch (Exception e) {
            messenger.printMessage(Diagnostic.Kind.ERROR, e.toString());
        }
        return false;
    }
}
