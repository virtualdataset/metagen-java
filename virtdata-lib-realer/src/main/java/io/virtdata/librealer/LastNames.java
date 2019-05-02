package io.virtdata.librealer;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.stathelpers.aliasmethod.WeightedStrings;

import java.util.function.LongFunction;

/**
 * Return a pseudo-randomly sampled last name from the last US census data on last names
 * occurring more than 100 times.
 */
@ThreadSafeMapper
@Categories({Category.premade})
public class LastNames extends WeightedStrings implements LongFunction<String> {

    @Example({"LastNames()","select a random last name based on the chance of seeing it in the census data"})
    public LastNames() {
        super("Name", "prop100k", "surnames");
    }
    @Example({"LastNames('map')","select over the last names by probability as input varies from 1L to Long.MAX_VALUE"})
    public LastNames(String modifier) {
        super("Name", modifier, "prop100k", "surnames");
    }

}
