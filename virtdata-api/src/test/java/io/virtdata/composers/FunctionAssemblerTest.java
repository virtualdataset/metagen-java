package io.virtdata.composers;

import io.virtdata.api.DataMapper;
import io.virtdata.api.composers.FunctionAssembly;
import io.virtdata.api.composers.FunctionComposer;
import org.junit.Test;

import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

//import org.junit.Test;

public class FunctionAssemblerTest {

    @Test
    public void testLongUnary() {
        FunctionComposer fass = new FunctionAssembly();
        fass.andThen(new IdentityOperator());
        DataMapper<Long> dataMapper = fass.getDataMapper();
        Long aLong = dataMapper.get(5);
        assertThat(aLong).isEqualTo(5);
    }

    @Test
    public void testLongUnaryLongUnary() {
        FunctionComposer fass = new FunctionAssembly();
        fass.andThen(new IdentityOperator());
        fass.andThen(new IdentityOperator());
        DataMapper<Long> dataMapper = fass.getDataMapper();
        Long aLong = dataMapper.get(5);
        assertThat(aLong).isEqualTo(5);
    }

    @SuppressWarnings("Duplicates")
    @Test
    public void testLongFunction() throws Exception {
        FunctionComposer fass = new FunctionAssembly();
        fass.andThen(new LongAddFiveFunction());
        DataMapper<Long> dataMapper = fass.getDataMapper();
        Long aLong = dataMapper.get(5);
        assertThat(aLong).isEqualTo(10);

    }

    @Test
    public void testLongFunctionLongFunctionProper() {
        FunctionComposer fass = new FunctionAssembly();
        fass.andThen(new LongAddFiveFunction());
        fass.andThen(new LongAddFiveFunction());
        DataMapper<Long> dataMapper = fass.getDataMapper();
        Long aLong = dataMapper.get(5);
        assertThat(aLong).isEqualTo(15);
    }

    @Test(expected = ClassCastException.class)
    public void testLongFunctionLongFunctionMistyped() throws Exception {
        FunctionComposer fass = new FunctionAssembly();
        fass.andThen(new LongAddFiveFunction());
        fass.andThen(new GenericStringCat());
        DataMapper<String> dataMapper = fass.getDataMapper();
        dataMapper.get(5);
    }

    @Test
    public void testAndThenFunction() {
        FunctionComposer fass = new FunctionAssembly();
        fass.andThen(new GenericLongToString());
        DataMapper<String> dataMapper = fass.getDataMapper();
        String s = dataMapper.get(5);
        assertThat(s).isEqualTo("5");
    }

//    @Test
//    public void testFunctionFunctionProper() {
//        FunctionComposer fass = new FunctionAssembly();
//        fass.andThen(new GenericLongToString());
//        fass.andThen(new GenericStringCat());
//        DataMapper<String> dataMapper = fass.getDataMapper();
//        String s = dataMapper.get(5);
//        assertThat(s).isEqualTo("Cat5");
//    }

    @Test(expected = ClassCastException.class)
    public void testFunctionFunctionMistyped() {
        FunctionComposer fass = new FunctionAssembly();
        fass.andThen(new GenericStringCat());
        DataMapper<String> dataMapper = fass.getDataMapper();
        String s = dataMapper.get(5);
    }

    @Test
    public void testLongUnaryLongFunctionFunctionProper() {
        FunctionComposer fass = new FunctionAssembly();
        fass.andThen(new IdentityOperator());
        fass.andThen(new LongAddFiveFunction());
        fass.andThen(new GenericLongToString());
        DataMapper<String> dataMapper = fass.getDataMapper();
        String s = dataMapper.get(5);
        assertThat(s).isEqualTo("10");

    }

    private static class IdentityOperator implements LongUnaryOperator {
        @Override
        public long applyAsLong(long operand) {
            return operand;
        }
    }

    private static class LongAddFiveFunction implements LongFunction<Long> {
        @Override
        public Long apply(long value) {
            return value + 5;
        }
    }

    private static class GenericLongToString implements Function<Long,String> {
        @Override
        public String apply(Long aLong) {
            return String.valueOf(aLong);
        }
    }

    private static class GenericStringCat implements Function<String,String> {
        @Override
        public String apply(String s) {
            return "Cat" + s;
        }
    }

}