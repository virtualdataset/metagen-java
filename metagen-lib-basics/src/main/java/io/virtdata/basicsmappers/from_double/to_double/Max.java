package io.virtdata.basicsmappers.from_double.to_double;

import io.virtdata.api.ThreadSafeMapper;

import java.util.function.DoubleUnaryOperator;

@ThreadSafeMapper
public class Max implements DoubleUnaryOperator {
    private final double max;

    public Max(double max) {
        this.max = max;
    }

    @Override
    public double applyAsDouble(double operand) {
        return Double.max(max,operand);
    }
}
