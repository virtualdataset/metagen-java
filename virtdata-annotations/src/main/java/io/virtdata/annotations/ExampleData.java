package io.virtdata.annotations;

import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.LongStream;

/**
 * <H2>Formatting conventions</H2>
 * The following example formats are supported:
 *
 * <ul>
 *     <li><pre>CtorName(param,...)</pre> - as the constructor would be called.</li>
 *     <li><pre>[1,2,3]</pre> - a sequence of integer values</li>
 *     <li><pre>[longs1]</pre> - a name of a pre-defined set of sample inputs</li>
 * </ul>
 */
public class ExampleData {

    public static Pattern CTOR_PATTERN = Pattern.compile("(?<funcname>[^)]+)\\((?<args>.+)\\)");
    public static Pattern VALS_PATTERN = Pattern.compile("\\[(?<values>-?\\d+([,-. ]+-?\\d+)*)]");
    private Pattern COMMA_VALS = Pattern.compile("\\[(?<vals>-?\\d+(,-?\\d+)*)]");
    private Pattern RANGE_VALS = Pattern.compile("\\[(?<from>-?\\d+)\\.\\.(?<to>-?\\d+)( +(?<step>-?\\d+))?]");

    public String[] parts;

    public ExampleData(String[] parts) {
        this.parts = parts;
    }

    public static void validateExamples(List<List<String>> examples) {
        for (List<String> example : examples) {
            for (String examplePart : example) {
                if (CTOR_PATTERN.matcher(examplePart).matches()) {
                    continue;
                }
                if (VALS_PATTERN.matcher(examplePart).matches()) {
                    continue;
                }
                if (!examplePart.startsWith("[")) {
                    return;
                }
                throw new RuntimeException("Unable to match a valid pattern for example fragment '"+examplePart +"')" +
                        ". See javadoc for the Example annotation for details.");
            }
        }
    }


    public long[] getLongInputs() {
        long[] vals = new long[0];
        for (String part : parts) {
            if (VALS_PATTERN.matcher(part).matches()) {
                String[] specs = part.split(";");
                long[][] genvals = new long[specs.length][];
                int len =0;
                for (int i = 0; i < genvals.length; i++) {
                    String spec = specs[i];
                    genvals[i] = parseRange(spec);
                    len+=genvals[i].length;
                }
                vals = new long[len];
                int atlen=0;
                for (int i = 0; i < genvals.length; i++) {
                    System.arraycopy(genvals[i],0,vals,atlen,genvals[i].length);
                    atlen+=genvals[i].length;
                }
            }
        }
        return vals;
    }


    private long[] parseRange(String spec) {
        Matcher comma_matcher = COMMA_VALS.matcher(spec);
        if (comma_matcher.matches()) {
            String vals = comma_matcher.group("vals");
            long[] longvals = Arrays.stream(vals.split(",")).mapToLong(Long::valueOf).toArray();
            return longvals;
        }
        Matcher range_matcher = RANGE_VALS.matcher(spec);
        if (range_matcher.matches()) {
            long from=Long.parseLong(range_matcher.group("from"));
            long to=Long.parseLong(range_matcher.group("to"));
            String step_size = range_matcher.group("step");
            long step = 1L;
            if (step_size!=null) {
                step = Long.parseLong(step_size);
            }
            if (from<to && step<=0) {
                throw new RuntimeException("for increasing from-to of (" +from+"-"+to+":, stepsize must be positive.");
            }
            if (from>to && step>=0) {
                throw new RuntimeException("for decreasing from-to of (" +from+"-"+to+":, stepsize must be negative.");
            }
            long sizeL = (Math.abs(from-to) / Math.abs(step))+1;
            if (sizeL>Integer.MAX_VALUE) {
                throw new RuntimeException("example size " + sizeL + " is too big.");
            }
            long[] vals = new long[(int)sizeL];
            long sign = (step>0 ? 1L : -1L);
            for (int i = 0; i < vals.length; i++) {
                vals[i]=from+(step*i);
            }
            return vals;
        }
        throw new RuntimeException("Unable to parse spec pattern: '"+spec+"'");
    }
}
