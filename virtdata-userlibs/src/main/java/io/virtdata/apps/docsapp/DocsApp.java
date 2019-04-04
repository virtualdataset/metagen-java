package io.virtdata.apps.docsapp;

import io.virtdata.annotations.Category;
import io.virtdata.autodoctypes.DocCtorData;
import io.virtdata.autodoctypes.DocFuncData;
import io.virtdata.core.VirtDataDocs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

public class DocsApp {
    private final static Logger logger = LoggerFactory.getLogger(DocsApp.class);
    private final static String SPLIT = "split";
    private final static String COMBINED = "combined";
    private final static String ALL = "all";
    private final static String DEFAULT_FILE="funcref";
    private final static String fileExtension=".md";

    // category -> funcname -> [docfuncdata, ...]
    private Map<Category, Map<String, List<DocFuncData>>> groupedModels = new HashMap<>();

    private String baseFileName = DEFAULT_FILE;
    private String print = ALL;
    private String categories = SPLIT;

    public static void main(String[] args) {
        new DocsApp().invoke(args);
    }

    private void invoke(String[] args) {
        LinkedList<String> largs = new LinkedList<>(Arrays.asList(args));
        while (largs.peekFirst() != null) {
            String argtype = largs.removeFirst();
            if (largs.peekFirst() == null) {
                throw new RuntimeException(DocsApp.class.toString() + " expects args in param value couplets.");
            }
            String argval = largs.removeFirst().toLowerCase();
            switch (argtype) {
                case "output":
                    this.baseFileName = argval;
                    break;
                case "print":
                    if (argval.equals("all") || argval.equals("logs")) {
                        this.print = argval;
                    } else {
                        throw new InvalidParameterException("valid args for print: print all, print logs");
                    }
                case "categories":
                    if (!argval.equals(SPLIT) && !argval.equals(COMBINED)) {
                        throw new RuntimeException("categories must either be " + SPLIT + ", or " + COMBINED + ".");
                    }
                    categories = argval;
                    break;
                default:
            }
        }

        List<DocFuncData> docModels = VirtDataDocs.getAllDocs();
        for (DocFuncData docModel : docModels) {
            for (Category category : docModel.getCategories()) {
                Map<String, List<DocFuncData>> category_funcname_list = this.groupedModels.get(category);
                if (category_funcname_list == null) {
                    category_funcname_list = new HashMap<>();
                    this.groupedModels.put(category, category_funcname_list);
                }
                List<DocFuncData> group = category_funcname_list.getOrDefault(docModel.getClassName(), new ArrayList<>());
                group.add(docModel);
                category_funcname_list.put(docModel.getClassName(), group);
            }
        }


        StringBuilder sb = new StringBuilder();
//        Map<Category,String> docsByCategory=new HashMap<>();

        Map<String,Set<Category>> assignments = new HashMap<>();
        // Map single category annotation to global Name -> Category assignment
        for (DocFuncData docModel : docModels) {

            Set<Category> listForFuncName = assignments.getOrDefault(docModel.getClassName(), new HashSet<>());
            assignments.put(docModel.getClassName(),listForFuncName);
            if (listForFuncName.size()>0) {
                logger.warn("Func name " +  docModel.getClassName() + " has " + listForFuncName.size() + " multiple category annotations:");
            }
            listForFuncName.addAll(Arrays.asList(docModel.getCategories()));
            logger.info("Assigning " + docModel.getClassName() + " to categories " + listForFuncName.toString());
        }

        Set<Category> generalSet = new HashSet<Category>() {{ add(Category.general); }};
        // regroup docs under categories
        // category -> funcname -> [docfuncdata, ...]
        Map<Category, Map<String, List<DocFuncData>>> regrouped = new HashMap<>();
        for (DocFuncData docModel : docModels) {
            Set<Category> assignment = assignments.getOrDefault(docModel.getClassName(), generalSet);
            if (assignment.size()==0) {
                assignment=generalSet;
            }
            logger.info("looking up assignment for " + docModel.getClassName() +":" + assignment.toString());
            for (Category category : assignment) {
                Map<String, List<DocFuncData>> assignToCategory = regrouped.getOrDefault(category, new HashMap<>());
                regrouped.put(category,assignToCategory);

                List<DocFuncData> assignToClass = assignToCategory.getOrDefault(docModel.getClassName(), new ArrayList<>());
                assignToCategory.put(docModel.getClassName(),assignToClass);

                assignToClass.add(docModel);
            }
        }
        groupedModels=regrouped;


        Map<String,Writer> writers = new HashMap<>();
        Writer writer = new OutputStreamWriter(System.out);
        try {

            for (Category category : Category.values()) {
                if (groupedModels.keySet().contains(category)) {

                    if (!this.baseFileName.isEmpty() && this.categories.equals(SPLIT)) {
                        writer = writers.getOrDefault(category.toString(), new FileWriter(baseFileName + "_" + category.toString() + fileExtension));
                    } else if (!this.baseFileName.isEmpty() && this.categories.equals(COMBINED)) {
                        writer = writers.getOrDefault(baseFileName + fileExtension, new FileWriter(baseFileName));
                    }

                    String docs = writeCategoryDocs(category, groupedModels.get(category));
                    docs = replacePatterns(docs);
                    writer.write(docs);
                    writer.flush();
                }
            }
            for (Writer writer1 : writers.values()) {
                writer1.close();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String replacePatterns(String docdata) {
        docdata = docdata.replaceAll("java.lang.", "");
        docdata = docdata.replaceAll("^\\s*</?pre>\\s*\n", "\n```\n");
        docdata = docdata.replaceAll("(<p>|</p>| \n)+", "\n");
        docdata = docdata.replaceAll("<pre>", "`");
        docdata = docdata.replaceAll("</pre>", "`");
        docdata = docdata.replaceAll("\\{@link (.+?)}", "$1");
        docdata = docdata.replaceAll("(?m)@param .*\n", "");
        docdata = docdata.replaceAll("(?m)\n\n+", "\n\n");
        docdata = docdata.replaceAll("(?m)\\[]...:","...");
        docdata = docdata.replaceAll("(?m)@see ","See ");
        return docdata;
    }


    private String writeCategoryDocs(Category category, Map<String, List<DocFuncData>> groupedDocs) {

        StringBuilder sb = new StringBuilder();
        ArrayList<String> funcNames = new ArrayList<>(groupedDocs.keySet());
        Collections.sort(funcNames);
        sb.append("# CATEGORY ").append(category).append("\n");

        for (String name : funcNames) {
            List<DocFuncData> docs = groupedDocs.get(name);

            sb.append("## ").append(name).append("\n\n");

            List<DocFuncData> classdocs = docs.stream()
                    .filter(d -> d.getClassJavadoc() != null && !d.getClassJavadoc().isEmpty())
                    .collect(Collectors.toList());

            List<String> distinctClassDocs = classdocs.stream()
                    .map(DocFuncData::getClassJavadoc)
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.toList());

            if (distinctClassDocs.size() == 0) {
                logger.warn("There were no class docs found for types named " + name);
            }
            if (distinctClassDocs.size() > 1) {
                logger.warn("There were multiple class docs found for types named " + name);
            }

            if (distinctClassDocs.size() == 1) {
                String classdoc = distinctClassDocs.get(0);
                sb.append(classdoc);
                if (!classdoc.endsWith("\n\n")) {
                    sb.append("\n");
                }
                if (!classdoc.endsWith("\n")) {
                    sb.append("\n");
                }
            }

            for (DocFuncData doc : docs) {
                List<DocCtorData> ctors = doc.getCtors();
                for (DocCtorData ctor : ctors) {
                    sb.append("- ").append(doc.getInType()).append(" -> ");
                    sb.append(doc.getClassName());
                    sb.append("(");
                    sb.append(
                            ctor.getArgs().entrySet().stream().map(
                                    e -> e.getValue() + ": " + e.getKey()
                            ).collect(Collectors.joining(", "))
                    );
                    sb.append(")");
                    sb.append(" -> ").append(doc.getOutType()).append("\n");
                    String ctorDoc = ctor.getCtorJavaDoc();
                    if (!ctorDoc.isEmpty()) {
                        sb.append("  - *notes:* ").append(ctorDoc);
                    }
                    for (List<String> example : ctor.getExamples()) {
                        sb.append("  - *ex:* `").append(example.get(0)).append("`");
                        if (example.size() > 1) {
                            sb.append(" - *").append(example.get(1)).append("*");
                        }
                        sb.append("\n");
                    }
                }
            }
            sb.append("\n");
            sb.append("\n");
        }
        return sb.toString();
    }


}
