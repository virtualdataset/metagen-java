package io.virtdata.libraryimpl;

import com.google.auto.service.AutoService;
import io.virtdata.api.GeneratorLibrary;
import io.virtdata.core.ResolvedFunction;
import io.virtdata.core.AllGenerators;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;

@AutoService(GeneratorLibrary.class)
public class ComposerLibrary implements GeneratorLibrary {

    @Override
    public String getLibraryName() {
        return "composer";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<ResolvedFunction> resolveFunction(String specline) {
        if (!specline.startsWith("compose ")) {
            return Optional.empty();
        }

        String[] specs = specline.substring("compose ".length()).split(" ");

        List<ResolvedFunction> resolvedFunctions = new LinkedList<>();

        Object composed = null;
        for (String spec : specs) {
            Optional<ResolvedFunction> resolvedFunction = AllGenerators.get().resolveFunction(spec);
            resolvedFunctions.add(resolvedFunction.orElseThrow(
                    () -> new RuntimeException("Unable to find generator for " + spec)
            ));
        }
        FunctionAssembler assy = new FunctionAssembler();

        for (ResolvedFunction resolvedFunction : resolvedFunctions) {
            assy.andThen(resolvedFunction.getFunctionObject());
        }
        LongFunction<?> function = assy.getFunction();
        ResolvedFunction composedFunction = new ResolvedFunction(function, this);
        return Optional.of(composedFunction);
    }


    @Override
    public List<String> getGeneratorNames() {
        List<String> genNames = new ArrayList<>();
        return new ArrayList<String>() {{
            add("compose");
        }};
    }
}
