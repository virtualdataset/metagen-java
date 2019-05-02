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
public class LoadString implements Function<Object,String> {

    private final String name;
    private final Function<Object,Object> nameFunc;
    private final String defaultValue;

    @Example({"LoadString('foo','examplevalue')","for the current thread, load a String value from the named variable."})
    public LoadString(String name) {
        this.name = name;
        this.nameFunc=null;
        this.defaultValue="";
    }

    @Example({"LoadString('foo','examplevalue')","for the current thread, load a String value from the named variable," +
            " or the default value if the named variable is not defined."})
    public LoadString(String name, String defaultValue) {
        this.name = name;
        this.nameFunc=null;
        this.defaultValue=defaultValue;
    }

    @Example({"LoadString(NumberNameToString(),'examplevalue')","for the current thread, load a String value from the named variable," +
            " or the default value if the named variable is not defined."})
    public LoadString(Function<Object,Object> nameFunc) {
        this.name=null;
        this.nameFunc = nameFunc;
        this.defaultValue="";
    }

    @Example({"LoadString(NumberNameToString(),'examplevalue')","for the current thread, load a String value from the named variable," +
            "where the variable name is provided by a function, or the default value if the named" +
            "variable is not defined."})
    public LoadString(Function<Object,Object> nameFunc, String defaultValue) {
        this.name=null;
        this.nameFunc = nameFunc;
        this.defaultValue=defaultValue;
    }

    @Override
    public String apply(Object o) {
        String varname=(nameFunc!=null) ? String.valueOf(nameFunc.apply(o)) : name;
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        Object value = map.getOrDefault(varname,defaultValue);
        return (String) value;
    }
}
