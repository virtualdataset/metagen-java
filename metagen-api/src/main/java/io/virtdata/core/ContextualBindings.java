package io.virtdata.core;

import io.virtdata.api.ValuesArrayBinder;

/**
 * <p>A thread-local template that maps a set of generators, a context object, and a method for applying
 * generated values to the context object. This type is used in thread-local scope to map thread-specific
 * generator instances to a contextual template object and a method for applying generated values to it.</p>
 *
 * <p>This type is generally constructed by a ContextualBindingsTemplate.</p>
 *
 * @param <C> The type of the contextual template object.
 * @param <R> The resulting type from binding generated values with the contextual template C
 */
public class ContextualBindings<C, R> {

    private final C context;
    private Bindings bindings;
    private ValuesArrayBinder<C, R> valuesArrayBinder;

    public ContextualBindings(Bindings bindings, C context, ValuesArrayBinder<C, R> valuesArrayBinder) {
        this.bindings = bindings;
        this.context = context;
        this.valuesArrayBinder = valuesArrayBinder;
    }

    public Bindings getBindings() {
        return bindings;
    }

    public C getContext() {
        return context;
    }

    public R bind(long value) {
        Object[] allGeneratedValues = bindings.getAll(value);
        try { // Provide bindings context data where it may be useful
            return valuesArrayBinder.bindValues(context, allGeneratedValues);
        } catch (Exception e) {
            throw new RuntimeException("Binding error:" + bindings.getTemplate().toString(allGeneratedValues), e);
        }

    }
}
