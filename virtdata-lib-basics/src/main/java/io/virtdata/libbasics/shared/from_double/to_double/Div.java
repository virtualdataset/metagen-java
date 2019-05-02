package io.virtdata.libbasics.shared.from_double.to_double;

import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.DoubleUnaryOperator;

@ThreadSafeMapper
public class Div implements DoubleUnaryOperator {
    private final double divisor;

    public Div(double divisor) {
        this.divisor = divisor;
    }

    @Override
    public double applyAsDouble(double operand) {
        return operand / divisor;
    }
}
