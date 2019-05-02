package io.virtdata.libbasics.shared.stateful.from_long;

import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.SharedState;

import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;

/**
 * Save a value to a named thread-local variable, where the variable
 * name is fixed or a generated variable name from a provided function.
 * Note that the input type is not that suitable for constructing names,
 * so this is more likely to be used in an indirect naming pattern like
 * <pre>SaveDouble(Load('id'))</pre>
 */
@ThreadSafeMapper
public class SaveInteger implements LongToIntFunction {

    private final String name;
    private final LongFunction<Object> nameFunc;

    @Example({"SaveInteger('foo')","save the current integer value to a named variable in this thread."})
    public SaveInteger(String name) {
        this.name = name;
        this.nameFunc=null;
    }

    @Example({"SaveInteger(NumberNameToString())","save the current integer value to a named variable in this thread" +
            ", where the variable name is provided by a function."})
    public SaveInteger(LongFunction<Object> nameFunc) {
        this.name=null;
        this.nameFunc = nameFunc;
    }

    @Override
    public int applyAsInt(long value) {
        String varname=(nameFunc!=null) ? String.valueOf(nameFunc.apply(value)) : name;
        SharedState.tl_ObjectMap.get().put(varname, value);
        return (int) value;
    }
}
