package io.virtdata.libbasics.shared.stateful.from_long;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.SharedState;

import java.util.HashMap;
import java.util.function.LongFunction;

/**
 * Show diagnostic values for the thread-local variable map.
 */
@ThreadSafeMapper
@Categories({Category.state,Category.diagnostics})
public class Show implements LongFunction<String> {

    private final String[] names;
    private final ThreadLocal<StringBuilder> tl_sb = ThreadLocal.withInitial(StringBuilder::new);

    @Example({"Show()","Show all values in a json-like format"})
    public Show() {
        names=null;
    }

    @Example({"Show('foo')","Show only the 'foo' value in a json-like format"})
    @Example({"Show('foo','bar')","Show the 'foo' and 'bar' values in a json-like format"})
    public Show(String... names) {
        this.names = names;
    }

    @Override
    public String apply(long value) {
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        if (names==null) {
            return map.toString();
        }

        StringBuilder sb = tl_sb.get();
        sb.setLength(0);
        sb.append("{");

        for (String name : names) {
            sb.append(name).append("=");
            Object val = map.get(name);
            sb.append(val==null ? "NULL" : val.toString());
            sb.append(",");
        }
        sb.setLength(sb.length()-1);
        sb.append("}");

        return sb.toString();
    }
}
