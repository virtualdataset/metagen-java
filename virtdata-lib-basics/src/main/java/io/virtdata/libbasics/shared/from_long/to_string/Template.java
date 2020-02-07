package io.virtdata.libbasics.shared.from_long.to_string;

import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.util.VirtDataFunctions;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creates a template function which will yield a string which fits the template
 * provided, with all occurrences of <code>{}</code> substituted pair-wise with the
 * result of the provided functions. The number of <code>{}</code> entries in the template
 * must strictly match the number of functions or an error will be thrown.
 *
 * The objects passed must be functions of any of the following types:
 * <UL>
 *     <LI>LongUnaryOperator</LI>
 *     <LI>IntUnaryOperator</LI>
 *     <LI>DoubleUnaryOperator</LI>
 *     <LI>LongFunction</LI>
 *     <LI>IntFunction</LI>
 *     <LI>DoubleFunction</LI>
 *     <LI>Function&lt;Long,?&gt;</LI>
 * </UL>
 *
 * <p>The result of applying the input value to any of these functions is converted to a String
 * and then stitched together according to the template provided.</p>
 */
@ThreadSafeMapper
public class Template implements LongFunction<String> {
    private final static Logger logger  = LogManager.getLogger(Template.class);private static final String EXPR_BEGIN = "[[";
    private static final String EXPR_END = "]]";
    private final static ThreadLocal<StringBuilder> sb = ThreadLocal.withInitial(StringBuilder::new);
    private final String rawTemplate;
    private LongUnaryOperator iterOp;
    private String[] literals;
    private LongFunction<?>[] adaptedFuncs;

    @Example({"Template('{}-{}',Add(10),Hash())","concatenate input+10, '-', and a pseudo-random long"})
    public Template(String template, Object...funcs) {
        this(true, template, funcs);
    }

    @Example({"Template(true, '{}-{}', Add(10),Hash())", "throws an error, as the Add(10) function causes a narrowing conversion for a long input"})
    public Template(boolean truncate, String template, Object... funcs) {
        this.adaptedFuncs = adapt(funcs, truncate);
        this.rawTemplate = template;
        this.literals = parseTemplate(template, funcs.length);
    }

    private LongFunction<?>[] adapt(Object[] funcs, boolean truncate) {

        List<LongFunction<?>> adapted = new ArrayList<>();
        for (Object func : funcs) {
            LongFunction lf= VirtDataFunctions.adapt(func, LongFunction.class, Object.class, truncate);
            adapted.add(lf);
        }
        return adapted.toArray(new LongFunction<?>[0]);
    }

//    public Template(String template, LongFunction<?>... funcs) {
//        this.adaptedFuncs = funcs;
//        this.rawTemplate = template;
//        this.literals = parseTemplate(template, funcs.length);
//    }
//
//
//    public Template(String template, Function<Long,?>... funcs) {
//        this.adaptedFuncs = new LongFunction[funcs.length];
//        for (int i = 0; i < funcs.length; i++) {
//            int finalI = i;
//            adaptedFuncs[i]=(l) -> funcs[finalI].apply(l);
//        }
//        this.rawTemplate = template;
//        this.literals = parseTemplate(template,funcs.length);
//    }
//
//    public Template(String template, LongUnaryOperator... funcs) {
//        this.adaptedFuncs = new LongFunction[funcs.length];
//        for (int i = 0; i < funcs.length; i++) {
//            int finalI = i;
//            adaptedFuncs[i]=(l) -> funcs[finalI].applyAsLong(l);
//        }
//        this.rawTemplate = template;
//        this.literals = parseTemplate(template,funcs.length);
//    }
//
//    public Template(String template, IntUnaryOperator... funcs) {
//        this.adaptedFuncs = new LongFunction[funcs.length];
//        for (int i = 0; i < funcs.length; i++) {
//            int finalI = i;
//            adaptedFuncs[i]=(l) -> funcs[finalI].applyAsInt(Long.valueOf(l).intValue());
//        }
//        this.rawTemplate = template;
//        this.literals = parseTemplate(template,funcs.length);
//    }
//
//    public Template(String template, DoubleUnaryOperator... funcs) {
//        this.adaptedFuncs = new LongFunction[funcs.length];
//        for (int i = 0; i < funcs.length; i++) {
//            int finalI = i;
//            adaptedFuncs[i]=(l) -> funcs[finalI].applyAsDouble(l);
//        }
//        this.rawTemplate = template;
//        this.literals = parseTemplate(template,funcs.length);
//    }
//
//    public Template(String template, LongToDoubleFunction... funcs) {
//        this.adaptedFuncs = new LongFunction[funcs.length];
//        for (int i = 0; i < funcs.length; i++) {
//            int finalI = i;
//            adaptedFuncs[i]=(l) -> funcs[finalI].applyAsDouble(l);
//        }
//        this.rawTemplate = template;
//        this.literals = parseTemplate(template,funcs.length);
//    }
//
//    public Template(String template, LongToIntFunction... funcs) {
//        this.adaptedFuncs = new LongFunction[funcs.length];
//        for (int i = 0; i < funcs.length; i++) {
//            int finalI = i;
//            adaptedFuncs[i]=(l) -> funcs[finalI].applyAsInt(l);
//        }
//        this.rawTemplate = template;
//        this.literals = parseTemplate(template,funcs.length);
//
//    }

    /**
     * If an operator is provided, it is used to change the function input value in an additional way before each function.
     *
     * @param iterOp   A pre-generation value mapping function
     * @param template A string template containing <pre>{}</pre> anchors
     * @param funcs    A varargs length of LongFunctions of any output type
     */
    public Template(LongUnaryOperator iterOp, String template, LongFunction<?>... funcs) {
        this(template, funcs);
        this.iterOp = iterOp;
    }

    @SuppressWarnings("unchecked")
    private String[] parseTemplate(String template, int funcCount) {
        try {
            List<String> literals = new ArrayList<>();
            Pattern p = Pattern.compile("\\{}");
            Matcher m = p.matcher(template);
            int pos = 0;
            while (m.find()) {
                literals.add(template.substring(pos, m.start()));
                pos = m.end();
            }
            literals.add(template.substring(pos));
            if (literals.size() != funcCount + 1) {
                throw new RuntimeException("The number of {} place holders in '" + template + "' should equal the number of functions.");
            }
            return literals.toArray(new String[0]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String apply(long value) {
        StringBuilder buffer = sb.get();
        buffer.setLength(0);
        buffer.append(literals[0]);
        if (literals.length > 1) {
            for (int i = 0; i < adaptedFuncs.length; i++) {
                long input = iterOp != null ? iterOp.applyAsLong(value + i) : value + i;
                String genString = String.valueOf(adaptedFuncs[i].apply(input));
                buffer.append(genString);
                buffer.append(literals[i + 1]);
            }
        }
        return buffer.toString();
    }
}
