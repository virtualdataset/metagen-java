package io.virtdata.libbasics.shared.from_long.to_collection;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.shared.from_long.to_long.HashRange;
import io.virtdata.libbasics.shared.from_long.to_string.HashedLineToString;

import java.util.HashSet;
import java.util.Set;
import java.util.function.LongFunction;

/**
 * Return a pseudo-randomly created Set from the values in
 * the specified file.
 */
@ThreadSafeMapper
@Categories({Category.collections})
public class HashedLineToStringSet implements LongFunction<Set<String>> {

    private final HashedLineToString hashedLineToString;
    private final HashRange hashRange;

    @Example({"HashedLineToStringSet('variable_words.txt',2,10)","Create a set of words sized between 2 and 10 elements"})
    public HashedLineToStringSet(String filename, int minSize, int maxSize) {
        this.hashedLineToString = new HashedLineToString(filename);
        this.hashRange = new HashRange(minSize,maxSize);
    }

    @Override
    public Set<String> apply(long value) {
        long size = hashRange.applyAsLong(value);
        Set<String> list = new HashSet<>();
        for (int i = 0; i < size; i++) {
            list.add(hashedLineToString.apply(value+i));
        }
        return list;
    }
}
