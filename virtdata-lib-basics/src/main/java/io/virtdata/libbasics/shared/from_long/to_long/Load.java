package io.virtdata.libbasics.shared.from_long.to_long;

import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.SharedState;

import java.util.HashMap;
import java.util.function.Function;
import java.util.function.LongUnaryOperator;

@ThreadSafeMapper
public class Load implements LongUnaryOperator {

    private final String name;
    private final Function<Object,Object> nameFunc;
    private final long defaultValue;

    @Example({"Load('foo')","for the current thread, load a long value from the named variable"})
    public Load(String name) {
        this.name = name;
        this.nameFunc =null;
        this.defaultValue=0L;
    }

    @Example({"Load('foo', 423L)","for the current thread, load a long value from the named variable, or the default value if the variable is not yet defined"})
    public Load(String name, long defaultValue) {
        this.name = name;
        this.nameFunc =null;
        this.defaultValue=defaultValue;
    }

    @Example({"Load(NumberNameToString())","for the current thread, load a long value from the named variable, where the variable name is provided by the provided by a function."})
    public Load(Function<Object,Object> nameFunc) {
        this.name=null;
        this.nameFunc =nameFunc;
        this.defaultValue=0L;
    }

    @Example({"Load(NumberNameToString(),22L)","for the current thread, load a long value from the named variable, where the variable name is provided by the provided by a function, or the default value if the variable is not yet defined"})
    public Load(Function<Object,Object> nameFunc, long defaultvalue) {
        this.name=null;
        this.nameFunc =nameFunc;
        this.defaultValue=defaultvalue;
    }

    @Override
    public long applyAsLong(long operand) {
        String varname=(nameFunc !=null) ? String.valueOf(nameFunc.apply(operand)) : name;
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        Object o = map.getOrDefault(varname, defaultValue);
        return (long) o;
    }
}
