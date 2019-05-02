package io.virtdata.libbasics.shared.unary_string;

import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.SharedState;

import java.util.HashMap;
import java.util.function.Function;

@ThreadSafeMapper
public class Load implements Function<String,String> {

    private final String name;
    private final Function<Object,Object> nameFunc;
    private final String defaultValue;


    @Example({"Load('foo')","for the current thread, load a String value from the named variable"})
    public Load(String name) {
        this.name = name;
        this.nameFunc = null;
        this.defaultValue=null;
    }

    @Example({"Load('foo','track05')","for the current thread, load a String value from the named variable, or teh default value if the variable is not yet defined."})
    public Load(String name, String defaultvalue) {
        this.name = name;
        this.nameFunc = null;
        this.defaultValue=defaultvalue;
    }

    @Example({"Load(NumberNameToString())","for the current thread, load a String value from the named variable, where the variable name is provided by a function"})
    public Load(Function<Object,Object> nameFunc) {
        this.name = null;
        this.nameFunc = nameFunc;
        this.defaultValue=null;
    }

    @Example({"Load(NumberNameToString(),'track05')","for the current thread, load a String value from the named variable, where the variable name is provided by a function, or the default value if the variable is not yet defined."})
    public Load(Function<Object,Object> nameFunc, String defaultValue) {
        this.name = null;
        this.nameFunc = nameFunc;
        this.defaultValue=defaultValue;
    }

    @Override
    public String apply(String s) {
        String varname = nameFunc !=null ? String.valueOf(nameFunc.apply(s)) : name;
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        Object output = map.getOrDefault(varname,defaultValue);
        return (String) output;
    }

}
