package io.virtdata.libbasics.shared.from_long.to_int;

import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.shared.from_long.to_long.Hash;

import java.util.function.LongToIntFunction;

@ThreadSafeMapper
public class HashRange implements LongToIntFunction {

    private final long minValue;
    private final long width;
    private final Hash hash = new Hash();

    @Example({"HashRange(32L)","map the input to a number in the range 0-31, inclusive, of type int"})
    public HashRange(long width) {
        this.width=width;
        this.minValue=0L;
    }

    @Example({"HashRange(35L,39L)","map the input to a number in the range 35-38, inclusive, of type int"})
    public HashRange(long minValue, long maxValue) {
        this.minValue = minValue;

        if (maxValue<=minValue) {
            throw new RuntimeException("HashRange must have min and max value in that order.");
        }
        this.width = maxValue - minValue;
    }

    @Override
    public int applyAsInt(long operand) {
        return (int) ((minValue + (hash.applyAsLong(operand) % width)) & Integer.MAX_VALUE);
    }
}
