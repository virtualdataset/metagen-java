package io.virtdata.libbasics.shared.from_double.to_double;

import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.DoubleUnaryOperator;

@ThreadSafeMapper
public class Mul implements DoubleUnaryOperator {
    private final double factor;

    public Mul(double factor) {
        this.factor = factor;
    }

    @Override
    public double applyAsDouble(double operand) {
        return factor * operand;
    }
}
