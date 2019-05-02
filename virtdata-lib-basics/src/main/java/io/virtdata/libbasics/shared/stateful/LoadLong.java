package io.virtdata.libbasics.shared.stateful;

import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.SharedState;

import java.util.HashMap;
import java.util.function.Function;

/**
 * Load a value from a named thread-local variable, where the variable
 * name is fixed or a generated variable name from a provided function.
 * If the named variable is not defined, then the default value is returned.
 */
@ThreadSafeMapper
public class LoadLong implements Function<Object,Long> {

    private final String name;
    private final Function<Object,Object> nameFunc;
    private long defaultValue;

    @Example({"LoadLong('foo',42L)","for the current thread, load a long value from the named variable."})
    public LoadLong(String name) {
        this.name = name;
        this.nameFunc=null;
        this.defaultValue=0L;
    }

    @Example({"LoadLong('foo',42L)","for the current thread, load a long value from the named variable," +
            " or the default value if the named variable is not defined."})
    public LoadLong(String name, long defaultValue) {
        this.name = name;
        this.nameFunc=null;
        this.defaultValue=defaultValue;
    }

    @Example({"LoadLong(NumberNameToString(),42L)","for the current thread, load a long value from the named variable," +
            "where the variable name is provided by a function."})
    public LoadLong(Function<Object,Object> nameFunc) {
        this.name=null;
        this.nameFunc = nameFunc;
        this.defaultValue=0L;
    }

    @Example({"LoadLong(NumberNameToString(),42L)","for the current thread, load a long value from the named variable," +
            "where the variable name is provided by a function, or the default value if the named" +
            "variable is not defined."})
    public LoadLong(Function<Object,Object> nameFunc, long defaultValue) {
        this.name=null;
        this.nameFunc = nameFunc;
        this.defaultValue=defaultValue;
    }

    @Override
    public Long apply(Object o) {
        String varname=(nameFunc!=null) ? String.valueOf(nameFunc.apply(o)) : name;
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        Object value = map.getOrDefault(varname,defaultValue);
        return (Long) value;
    }
}
