package io.virtdata.core;

import io.virtdata.api.DataMapperLibrary;
import io.virtdata.api.ValueType;
import io.virtdata.api.VirtDataFunctionLibrary;
import io.virtdata.api.composers.FunctionAssembly;
import io.virtdata.ast.FunctionCall;
import io.virtdata.ast.VirtDataFlow;
import io.virtdata.parser.VirtDataDSL;
import org.apache.commons.lang3.ClassUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <H2>Synopsis</H2>
 * <p>This library implements the ability to compose a lambda function from a sequence of other functions.
 * The resulting lambda will use the specialized primitive function interfaces, such as LongUnaryOperator, LongFunction, etc.
 * Where there are two functions which do not have matching input and output types, the most obvious conversion is made.
 * This means that while you are able to compose a LongUnaryOperator with a LongUnaryOperator for maximum
 * efficiency, you can also compose LongUnaryOperator with an IntFunction, and a best effort attempt will be made to
 * do a reasonable conversion in between.</p>
 *
 * <H2>Limitations</H2>
 * <P>Due to type erasure, it is not possible to know the generic type parameters for non-primitive functional types.
 * These include IntFunction&lt;?&gt;, LongFunction&lt;?&gt;, and in the worst case, Function&lt;?,?&gt;.
 * For these types, annotations are provided to better inform the runtime lambda compositor.</P>
 *
 * <H2>Multiple Paths</H2>
 * <P>The library allows for there to be multiple functions which match the spec, possibly because multiple
 * functions have the same name, but exist in different libraries or in different packages within the same library.
 * This means that the composer library must find a connecting path between the functions that can match at each stage,
 * disregarding all but one.</P>
 *
 * <H2>Path Finding</H2>
 * <P>The rule for finding the best path among the available functions is as follows, at each pairing between
 * adjacent stages of functions:</P>
 * <OL>
 * <li>The co-compatible output and input types between the functions are mapped. Functions sharing the co-compatible
 * types are kept in the list. Functions not sharing them are removed.</li>
 * <li>As long as functions can be removed in this way, the process iterates through the chain, starting again
 * at the front of the list.</li>
 * <li>When no functions can be removed due to lack of co-compatible types, each stage is selected according to
 * type preferences as represented in {@link ValueType}</li>
 *
 * <LI>If the next (outer) function does not have a compatible input type, move it down on the list.
 * If, after this step, there are functions which do have matching signatures, all others are removed.</LI>
 * </OL>
 */
public class VirtDataComposer {

    private final static String PREAMBLE = "compose ";
    private final static Logger logger  = LogManager.getLogger(DataMapperLibrary.class);private final static MethodHandles.Lookup lookup = MethodHandles.publicLookup();

    private final VirtDataFunctionLibrary functionLibrary;
    private final Map<String,Object> customElements = new HashMap<>();

    public VirtDataComposer(VirtDataFunctionLibrary functionLibrary) {
        this.functionLibrary = functionLibrary;
    }

    public VirtDataComposer() {
        this.functionLibrary = VirtDataLibraries.get();
    }


    public Optional<ResolvedFunction> resolveFunctionFlow(String flowspec) {

        String strictSpec = flowspec.startsWith("compose ") ? flowspec.substring(8) : flowspec;
        VirtDataDSL.ParseResult parseResult = VirtDataDSL.parse(strictSpec);
        if (parseResult.throwable != null) {
            throw new RuntimeException(parseResult.throwable);
        }
        VirtDataFlow flow = parseResult.flow;

        return resolveFunctionFlow(flow);
    }

    public ResolverDiagnostics resolveDiagnosticFunctionFlow(String flowspec) {
        String strictSpec = flowspec.startsWith("compose ") ? flowspec.substring(8) : flowspec;
        VirtDataDSL.ParseResult parseResult = VirtDataDSL.parse(strictSpec);
        if (parseResult.throwable != null) {
            throw new RuntimeException(parseResult.throwable);
        }
        VirtDataFlow flow = parseResult.flow;

        return resolveDiagnosticFunctionFlow(flow);
    }

    public ResolverDiagnostics resolveDiagnosticFunctionFlow(VirtDataFlow flow) {
        ResolverDiagnostics diagnostics = new ResolverDiagnostics();
        diagnostics.trace("processing flow " + flow.toString() + " from output to input");

        LinkedList<List<ResolvedFunction>> funcs = new LinkedList<>();

        LinkedList<Set<Class<?>>> nextFunctionInputTypes = new LinkedList<>();
        Optional<Class<?>> finalValueTypeOption =
                Optional.ofNullable(flow.getLastExpression().getCall().getOutputType())
                        .map(ValueType::valueOfClassName).map(ValueType::getValueClass);

        nextFunctionInputTypes.add(new HashSet<>());
        finalValueTypeOption.ifPresent(t -> nextFunctionInputTypes.get(0).add(t));

        diagnostics.trace("working backwards from " + (flow.getExpressions().size()-1));

        for (int i = flow.getExpressions().size() - 1; i >= 0; i--) {
            FunctionCall call = flow.getExpressions().get(i).getCall();
            diagnostics.trace("resolving args for " + call.toString());

            List<ResolvedFunction> nodeFunctions = new LinkedList<>();

            String funcName = call.getFunctionName();
            Class<?> inputType = ValueType.classOfType(call.getInputType());
            Class<?> outputType = ValueType.classOfType(call.getOutputType());
            Object[] args = call.getArguments();
            try {
                args = populateFunctions(diagnostics, args, this.customElements);
            } catch (Exception e) {
                return diagnostics.error(e);
            }

            diagnostics.trace("resolved args: ");
            for (Object arg : args) {
                diagnostics.trace(" " + arg.getClass().getSimpleName() + ": " + arg.toString());
            }

            List<ResolvedFunction> resolved = functionLibrary.resolveFunctions(outputType, inputType, funcName, this.customElements,args);
            if (resolved.size() == 0) {
                return diagnostics.error(new RuntimeException("Unable to find even one function for " + call));
            }
            diagnostics.trace(" resolved functions:");
            diagnostics.trace(summarize(resolved));

            nodeFunctions.addAll(resolved);
            funcs.addFirst(nodeFunctions);

            Set<Class<?>> inputTypes = nodeFunctions.stream().map(ResolvedFunction::getInputClass).collect(Collectors.toSet());
            nextFunctionInputTypes.addFirst(inputTypes);
        }

        if (!nextFunctionInputTypes.peekFirst().contains(Long.TYPE)) {
            return diagnostics.error(new RuntimeException("There is no initial function which accepts a long input. Function chain, after type filtering: \n" + summarizeBulk(funcs)));
        }
        removeNonLongFunctions(funcs.getFirst());

        List<ResolvedFunction> flattenedFuncs = optimizePath(funcs, ValueType.classOfType(flow.getLastExpression().getCall().getOutputType()));

        if (flattenedFuncs.size() == 1) {
            diagnostics.trace("FUNCTION resolution succeeded (single): '" + flow.toString() + "'");
            return diagnostics.setResolvedFunction(flattenedFuncs.get(0));
        }

        FunctionAssembly assembly = new FunctionAssembly();
        diagnostics.trace("composed summary: " + summarize(flattenedFuncs));

        boolean isThreadSafe = true;
        diagnostics.trace("FUNCTION chain selected: (multi) '" + this.summarize(flattenedFuncs) + "'");
        for (ResolvedFunction resolvedFunction : flattenedFuncs) {
            try {
                Object functionObject = resolvedFunction.getFunctionObject();
                assembly.andThen(functionObject);
                if (!resolvedFunction.isThreadSafe()) {
                    isThreadSafe = false;
                }
            } catch (Exception e) {
                String flowdata = flow!=null? flow.toString() : "undefined";
                return diagnostics.error(new RuntimeException("FUNCTION resolution failed: '" + flowdata + "': " + e.toString()));
            }
        }
        ResolvedFunction composedFunction = assembly.getResolvedFunction(isThreadSafe);
        diagnostics.trace("FUNCTION resolution succeeded (lambda): '" + flow.toString() + "'");
        return diagnostics.setResolvedFunction(composedFunction);
    }

    public Optional<ResolvedFunction> resolveFunctionFlow(VirtDataFlow flow) {
        ResolverDiagnostics resolverDiagnostics = resolveDiagnosticFunctionFlow(flow);
        return resolverDiagnostics.getResolvedFunction();
    }

    private Object[] populateFunctions(ResolverDiagnostics diagnostics, Object[] args, Map<String,?> cconfig) {
        for (int i = 0; i < args.length; i++) {
            Object o = args[i];
            if (o instanceof FunctionCall) {
                FunctionCall call = (FunctionCall) o;

                String funcName = call.getFunctionName();
                Class<?> inputType = ValueType.classOfType(call.getInputType());
                Class<?> outputType = ValueType.classOfType(call.getOutputType());
                Object[] fargs = call.getArguments();
                diagnostics.trace("resolving argument as function '" + call.toString() + "'");
                fargs = populateFunctions(diagnostics, fargs, cconfig);

                List<ResolvedFunction> resolved = functionLibrary.resolveFunctions(outputType, inputType, funcName, cconfig, fargs);
                if (resolved.size() == 0) {
                    throw new RuntimeException("Unable to resolve even one function for argument: " + call);
                }
                args[i] = resolved.get(0).getFunctionObject();
            }
        }
        return args;
    }

    private void removeNonLongFunctions(List<ResolvedFunction> funcs) {
        List<ResolvedFunction> toRemove = new LinkedList<>();
        for (ResolvedFunction func : funcs) {
            if (!func.getInputClass().isAssignableFrom(long.class)) {
                logger.trace("input type " + func.getInputClass().getCanonicalName() + " is not assignable from long");
                toRemove.add(func);
            }
        }
        if (toRemove.size() > 0 && toRemove.size() == funcs.size()) {
            throw new RuntimeException("removeNonLongFunctions would remove all functions: " + funcs);
        }
        funcs.removeAll(toRemove);
    }

    private String summarize(List<ResolvedFunction> funcs) {
        return funcs.stream()
                .map(String::valueOf).collect(Collectors.joining("|"));
    }

    private String summarizeBulk(List<List<ResolvedFunction>> funcs) {

        List<List<String>> spans = new LinkedList<>();
        funcs.forEach(l -> spans.add(l.stream().map(String::valueOf).collect(Collectors.toList())));
        List<Optional<Integer>> widths = spans.stream().map(
                l -> l.stream().map(String::length).max(Integer::compare)).collect(Collectors.toList());
        String funcsdata = spans.stream().map(
                l -> l.stream().map(String::valueOf).collect(Collectors.joining("|\n"))
        ).collect(Collectors.joining("\n\n"));

        StringBuilder sb = new StringBuilder();
        sb.append("---\\\\\n").append(funcsdata).append("\n---////\n");
        return sb.toString();
    }

    /**
     * <p>
     * Attempt path optimizations on each phase junction, considering the set of
     * candidate inner functions with the candidate outer functions.
     * This is an iterative process, that will keep trying until no apparent
     * progress is made. Each higher-precedence optimization strategy is used
     * iteratively as long as it makes progress and then the lower precedence
     * strategies are allowed to have their turn.
     * </p>
     * <p>
     * <p>It is considered an error if the strategies are unable to reduce each
     * phase down to a single preferred function. Therefore, the lowest precedence
     * strategy is the most aggressive, simply sorting the functions by basic
     * type preference and then removing all but the highest selected function.</p>
     *
     * @param funcs the list of candidate functions offered at each phase, in List&lt;List&gt; form.
     * @return a List of resolved functions that has been fully optimized
     */
    private List<ResolvedFunction> optimizePath(List<List<ResolvedFunction>> funcs, Class<?> type) {
        List<ResolvedFunction> prevFuncs = null;
        List<ResolvedFunction> nextFuncs = null;
        int progress = -1;

        int pass = 0;
        while (progress != 0) {
            pass++;
            progress = 0;
            progress += reduceByRequiredResultsType(funcs.get(funcs.size() - 1), type);
            if (funcs.size() > 1) {
                int stage = 0;
                for (List<ResolvedFunction> funcList : funcs) {
                    stage++;
                    nextFuncs = funcList;
                    if (prevFuncs != null && nextFuncs != null) {
                        if (progress == 0) {
                            progress += reduceByDirectTypes(prevFuncs, nextFuncs);
                            if (progress == 0) {
                                progress += reduceByAssignableTypes(prevFuncs, nextFuncs, false);
                                if (progress == 0) {
                                    progress += reduceByAssignableTypes(prevFuncs, nextFuncs, true);
                                    if (progress == 0) {
                                        progress += reduceByPreferredTypes(prevFuncs, nextFuncs);
                                    }
                                }
                            }
                        }

                    } // else first pass, prime pointers
                    prevFuncs = nextFuncs;
                }
                nextFuncs = null;
                prevFuncs = null;

            } else {
                progress += reduceByPreferredResultTypes(funcs.get(0));
            }
        }
        List<ResolvedFunction> optimized = funcs.stream().map(l -> l.get(0)).collect(Collectors.toList());

        return optimized;
    }

    private int reduceByRequiredResultsType(List<ResolvedFunction> endFuncs, Class<?> resultType) {
        int progressed = 0;
        LinkedList<ResolvedFunction> tmpList = new LinkedList<>(endFuncs);
        for (ResolvedFunction endFunc : tmpList) {
            if (resultType != null && !ClassUtils.isAssignable(endFunc.getResultClass(), resultType, true)) {
                endFuncs.remove(endFunc);
                String logmsg = "BY-REQUIRED-RESULT-TYPE removed function '" + endFunc + "' because is not assignable to " + resultType;
                logger.trace(logmsg);
                progressed++;
            }
        }
        if (endFuncs.size() == 0) {
            throw new RuntimeException("BY-REQUIRED-RESULT-TYPE No end funcs were found which are assignable to " + resultType);
        }
        return progressed;
    }

    private int reduceByPreferredResultTypes(List<ResolvedFunction> funcs) {
        int progressed = 0;
        if (funcs.size() > 1) {
            progressed += funcs.size() - 1;
            funcs.sort(ResolvedFunction.PREFERRED_TYPE_COMPARATOR);
            while (funcs.size() > 1) {
                logger.trace("BY-SINGLE-PREFERRED-TYPE removing func " + funcs.get(funcs.size() - 1)
                        + " because " + funcs.get(0) + " has more preferred types.");
                funcs.remove(funcs.size() - 1);
            }

        }
        return progressed;
    }

    private int reduceByPreferredTypes(List<ResolvedFunction> prevFuncs, List<ResolvedFunction> nextFuncs) {
        int progressed = 0;
        if (prevFuncs.size() > 1) {
            progressed += prevFuncs.size() - 1;
            prevFuncs.sort(ResolvedFunction.PREFERRED_TYPE_COMPARATOR);
            while (prevFuncs.size() > 1) {
                String logmsg = "BY-PREV-PREFERRED-TYPE removing func " + prevFuncs.get(prevFuncs.size() - 1)
                        + " because " + prevFuncs.get(0) + " has more preferred types.";
                logger.trace(logmsg);
                prevFuncs.remove(prevFuncs.size() - 1);
            }
        } else if (nextFuncs.size() > 1) {
            progressed += nextFuncs.size() - 1;
            nextFuncs.sort(ResolvedFunction.PREFERRED_TYPE_COMPARATOR);
            while (nextFuncs.size() > 1) {
                String logmsg = "BY-NEXT-PREFERRED-TYPE removing func " + nextFuncs.get(nextFuncs.size() - 1)
                        + " because " + nextFuncs.get(0) + " has more preferred types.";
                logger.trace(logmsg);
                nextFuncs.remove(nextFuncs.size() - 1);
            }
        }
        return progressed;
    }

    /**
     * If there are direct type matches between the inner func and the outer func, then remove all
     * other outer funcs except the ones with direct matches.
     *
     * @param prevFuncs The list of candidate inner functions
     * @param nextFuncs The list of candidate outer functions
     * @return count of items removed
     */
    private int reduceByDirectTypes(List<ResolvedFunction> prevFuncs, List<ResolvedFunction> nextFuncs) {

        int progressed = 0;

        // Rule 1: If there are direct type matches, remove extraneous next funcs
        Set<Class<?>> outputs = getOutputs(prevFuncs);
        Set<Class<?>> inputs = getInputs(nextFuncs);
        Set<Class<?>> directMatches =
                inputs.stream().filter(outputs::contains).collect(Collectors.toCollection(HashSet::new));

        if (directMatches.size() > 0) {
            List<ResolvedFunction> toremove = new ArrayList<>();
            for (ResolvedFunction nextFunc : nextFuncs) {
                if (!directMatches.contains(nextFunc.getArgType())) {
                    String logmsg = "BY-DIRECT-TYPE removing next func: " + nextFunc + " because its input types are not satisfied by any previous func";
                    logger.trace(logmsg);
                    toremove.add(nextFunc);
                    progressed++;
                }
            }
            nextFuncs.removeAll(toremove);
        }
        return progressed;
    }

    /**
     * Remove any functions in the second set which do not have an input type which is assignable
     * from any of the output types of the functions in the first set.
     *
     * @param prevFuncs the functions that come before the nextFuncs
     * @param nextFuncs the functions that come after prevFuncs
     * @return the number of next funcs that have been removed
     */
    private int reduceByAssignableTypes(List<ResolvedFunction> prevFuncs, List<ResolvedFunction> nextFuncs, boolean autoboxing) {

        // Rule 1: If there are direct type matches, remove extraneous next funcs
        Set<Class<?>> outputs = getOutputs(prevFuncs);
        Set<Class<?>> inputs = getInputs(nextFuncs);
        Set<Class<?>> compatibleInputs = new HashSet<>();

        for (Class<?> input : inputs) {
            for (Class<?> output : outputs) {
                if (ClassUtils.isAssignable(output, input, autoboxing)) {
                    compatibleInputs.add(input);
                }
            }
        }
        List<ResolvedFunction> toremove = new ArrayList<>();


        for (ResolvedFunction nextfunc : nextFuncs) {
            if (!compatibleInputs.contains(nextfunc.getInputClass())) {
                toremove.add(nextfunc);
            }
        }

        if (toremove.size() == nextFuncs.size()) {
            String logmsg = "BY-ASSIGNABLE-TYPE Not removing remaining " + nextFuncs.size() + " next funcs " + (autoboxing ? "with autoboxing " : "") + "because no functions would be left.";
            logger.trace(logmsg);
            return 0;
        } else {
            toremove.forEach(nextfunc -> {
                        String logmsg = "BY-ASSIGNABLE-TYPE removing next func: " + nextfunc + " because its input types are not assignable from any of the previous funcs";
                        logger.trace(logmsg);
                    }
            );

            nextFuncs.removeAll(toremove);
            return toremove.size();
        }

    }

    private Set<Class<?>> getOutputs(List<ResolvedFunction> prevFuncs) {
        Set<Class<?>> outputs = new HashSet<>();
        for (ResolvedFunction func : prevFuncs) {
            outputs.add(func.getResultClass());
        }
        return outputs;
    }

    private Set<Class<?>> getInputs(List<ResolvedFunction> nextFuncs) {
        Set<Class<?>> inputs = new HashSet<>();
        for (ResolvedFunction nextFunc : nextFuncs) {
            inputs.add(nextFunc.getArgType());
        }
        return inputs;
    }

    public Map<String,?> getCustomElements() {
        return this.customElements;
    }
    public VirtDataComposer addCustomElement(String name, Object element) {
        this.customElements.put(name, element);
        return this;
    }

    public VirtDataComposer addCustomElements(Map<String, ?> config) {
        this.customElements.putAll(config);
        return this;
    }
}
