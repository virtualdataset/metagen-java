package io.virtdata.libbasics.shared.stateful;

import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.api.config.ConfigAware;
import io.virtdata.api.config.ConfigModel;
import io.virtdata.api.config.MutableConfigModel;

import java.util.Map;
import java.util.function.Function;

/**
 * Load a value from a map, based on the injected configuration.
 * The map which is used must be named by the mapname.
 * If the injected configuration contains a variable of this name
 * which is also a Map, then this map is referenced and read
 * by the provided variable name.
 */
@ThreadSafeMapper
public class LoadElement implements Function<Object,Object>, ConfigAware {

    private final String varname;
    private final Object defaultValue;
    private final String mapname;

    private Map<String,?> vars;

    @Example({"LoadElement('varname','vars','defaultvalue')","Load the varable 'varname' from a map named 'vars', or provide 'defaultvalue' if neither is provided"})
    public LoadElement(String varname, String mapname, Object defaultValue) {
        this.mapname = mapname;
        this.varname = varname;
        this.defaultValue = defaultValue;
    }

    @Override
    public Object apply(Object o) {
        if (vars==null) {
            return defaultValue;
        }
        Object object = vars.get(varname);
        return (object!=null) ? object : defaultValue;
    }

    @Override
    public void applyConfig(Map<String, ?> element) {
        Map<String,?> vars = (Map<String, ?>) element.get(mapname);
        if (vars!=null) {
            this.vars = vars;
        }
    }

    @Override
    public ConfigModel getConfigModel() {
        return new MutableConfigModel().add("<mapname>",Map.class);
    }
}
