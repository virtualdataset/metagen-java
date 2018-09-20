package io.virtdata.basicsmappers.stateful;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.threadstate.ThreadLocalState;

import java.util.HashMap;
import java.util.function.Function;

/**
 * Save the current input value at this point in the function chain to a thread-local variable name.
 * The input value is unchanged, and available for the next function in the chain to use as-is.
 */
@ThreadSafeMapper
@Categories({Category.state})
public class Save implements Function<Object,Object> {

    private final String name;
    private final Function<Object,Object> nameFunc;

    @Example({"Save('foo')","for the current thread, save the input object value to the named variable"})
    public Save(String name) {
        this.name = name;
        this.nameFunc=null;
    }

    @Example({"Save('foo')","for the current thread, save the current input object value to the named variable"})
    public Save(Function<Object,Object> nameFunc) {
        this.name = null;
        this.nameFunc = nameFunc;
    }

    @Override
    public Object apply(Object o) {

        HashMap<String, Object> map = ThreadLocalState.tl_ObjectMap.get();
        String varname = (nameFunc!=null) ? String.valueOf(nameFunc.apply(o)) : name;
        map.put(varname,o);
        return o;
    }

}
