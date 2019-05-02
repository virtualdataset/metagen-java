package io.virtdata.libbasics.shared.stateful.from_long;

import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.SharedState;

import java.util.HashMap;
import java.util.function.LongFunction;

/**
 * Save a value to a named thread-local variable, where the variable
 * name is fixed or a generated variable name from a provided function.
 * Note that the input type is not that suitable for constructing names,
 * so this is more likely to be used in an indirect naming pattern like
 * <pre>SaveDouble(Load('id'))</pre>
 */
@ThreadSafeMapper
public class SaveFloat implements LongFunction<Float> {

    private final String name;
    private final LongFunction<Object> nameFunc;

    @Example({"SaveFloat('foo')","save the current float value to a named variable in this thread."})
    public SaveFloat(String name) {
        this.name = name;
        this.nameFunc=null;
    }

    @Example({"SaveFloat(NumberNameToString())","save the current float value to a named variable in this thread" +
            ", where the variable name is provided by a function."})
    public SaveFloat(LongFunction<Object> nameFunc) {
        this.name=null;
        this.nameFunc = nameFunc;
    }

    @Override
    public Float apply(long value) {
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        String varname=(nameFunc!=null) ? String.valueOf(nameFunc.apply(value)) : name;
        map.put(varname,value);
        return (float) value;
    }
}
