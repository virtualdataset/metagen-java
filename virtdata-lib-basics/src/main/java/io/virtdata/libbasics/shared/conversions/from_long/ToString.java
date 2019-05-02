package io.virtdata.libbasics.shared.conversions.from_long;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

/**
 * Convert the input value to a String.
 */
@ThreadSafeMapper
@Categories({Category.conversion})
public class ToString implements LongFunction<String> {
    public String apply(long l) {
        return String.valueOf(l);
    }
}
