package io.virtdata.libbasics.shared.from_long.to_time_types;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.DateTimeFormats;
import org.joda.time.DateTime;

import java.util.function.LongUnaryOperator;

/**
 * This function sets the minimum long value to the equivalent
 * unix epoch time in milliseconds. It simply adds the input
 * value to this base value as determined by the provided
 * time specifier. It wraps any overflow within this range as well.
 */
@ThreadSafeMapper
@Categories({Category.datetime})
public class StartingEpochMillis implements LongUnaryOperator {

    private final DateTime startingTime;
    private final long startingUnixEpochMillis;
    private final long headroom;

    @Example({"StartingEpochMillis('2017-01-01 23:59:59')","add the millisecond epoch time of 2017-01-01 23:59:59 to all input values"})
    public StartingEpochMillis(String baseTimeSpec) {
        startingTime = DateTimeFormats.parseEpochTimeToDateTime(baseTimeSpec);
        startingUnixEpochMillis = startingTime.getMillis();
        headroom = Long.MAX_VALUE - startingUnixEpochMillis;
    }

    @Override
    public long applyAsLong(long value) {
        return startingUnixEpochMillis + (value % headroom);
    }
}
