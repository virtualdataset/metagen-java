package io.virtdata.basicsmappers;

import org.assertj.core.data.Offset;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ExprTest {

    @Test
    public void testLongUnaryExpr() {
        io.virtdata.libbasics.shared.from_long.to_long.Expr mod5 =
                new io.virtdata.libbasics.shared.from_long.to_long.Expr("cycle % 5");
        long three = mod5.applyAsLong(23);
        assertThat(three).isEqualTo(3);
    }

    @Test
    public void testDoubleUnaryExpr() {
        io.virtdata.libbasics.shared.from_double.to_double.Expr plus3point5 =
                new io.virtdata.libbasics.shared.from_double.to_double.Expr("cycle + 3.5");
        double r = plus3point5.applyAsDouble(32.5);
        assertThat(r).isCloseTo(36.0, Offset.offset(0.001d));
    }

    @Test
    public void testLongToIntExpr() {
        io.virtdata.libbasics.shared.from_long.to_int.Expr minus7 =
                new io.virtdata.libbasics.shared.from_long.to_int.Expr("cycle - 7");
        int r = minus7.applyAsInt(234233);
        assertThat(r).isEqualTo(234226);
    }

    @Test
    public void testUnaryIntExpr() {
        io.virtdata.libbasics.shared.unary_int.Expr times2 =
                new io.virtdata.libbasics.shared.unary_int.Expr("cycle * 2");
        int fourtytwo = times2.applyAsInt(21);
        assertThat(fourtytwo).isEqualTo(42);
    }

    @Test
    public void testLongExprSpeed() {
        //Expr mod5 = new Expr("(cycle / 10)*10 + (cycle % 5)");
        io.virtdata.libbasics.shared.from_long.to_long.Expr mod5 =
                new io.virtdata.libbasics.shared.from_long.to_long.Expr("(cycle / 10)*10 + (cycle % 5)");
        long three = mod5.applyAsLong(23);
        long start = System.nanoTime();
        int min=0;
        int max=1000000;
        for (int i = min; i < max; i++) {
            long l = mod5.applyAsLong(i);
            //System.out.format("input=%d output=%d\n", i, l);
            //assertThat(l).isEqualTo((i%5));

        }
        long end = System.nanoTime();
        long duration = end-start;
        double nsperop = (double) duration / (double) (max-min);

        System.out.format("(ops/time)=(%d/%dns) rate=%.3f\n", (max-min), duration, ((double) max-min)*1000000000.0/duration);
    }

}