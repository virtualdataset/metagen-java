package io.virtdata.libbasics.lfsrs;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MetaShiftTest {

    public void testWidthSelection() {
        assertThat(MetaShift.getMsbPosition(2)).isEqualTo(2);
        assertThat(MetaShift.getMsbPosition(7)).isEqualTo(3);
        assertThat(MetaShift.getMsbPosition(8)).isEqualTo(4);
        assertThat(MetaShift.getMsbPosition(Long.MAX_VALUE)).isEqualTo(63);
    }

    @Test(expected = RuntimeException.class)
    //, expectedExceptionsMessageRegExp = ".*Only values between.*")
    public void testNegativeException() {
        assertThat(MetaShift.getMsbPosition(-34)).isEqualTo(64);
    }

    @Test
    public void testSmallRegisterSanity() {
        MetaShift.Func f = MetaShift.forSizeAndBank(4, 0);
        assertThat(f.config.feedback).isEqualTo(9L);
    }

    @Test(expected = RuntimeException.class)
    //}, expectedExceptionsMessageRegExp = ".*are only 2 items available.*")
    public void testBankSelectorOverrun() {
        MetaShift.Func f = MetaShift.forSizeAndBank(4, 123);
    }

//    @Test
//    public void testOverrunModuloCompensator() {
//        MetaShift.Func f = MetaShift.forSizeAndBank(8, 0);
//        assertThat(f.applyAsLong(7)).isEqualTo(10L);
//        assertThat(f.applyAsLong(22)).isEqualTo(11L);
//        assertThat(f.applyAsLong(37)).isEqualTo(27L);
//
//    }

    @Test
    public void testMaskSize() {

        MetaShift.GaloisData data31 = MetaShift.Masks.forPeriodAndBank(31L, 0);
        assertThat(data31.feedback).isEqualTo(18L);
        assertThat(MetaShift.toBitString(data31.mask,10)).isEqualTo("0000011111");
        assertThat(data31.width).isEqualTo(5L);
        assertThat(data31.resamplePeriod).isEqualTo(31L);
        assertThat(data31.actualPeriod).isEqualTo(31);

        MetaShift.GaloisData data32 = MetaShift.Masks.forPeriodAndBank(32L, 0);
        assertThat(data32.feedback).isEqualTo(33L);
        assertThat(MetaShift.toBitString(data32.mask,10)).isEqualTo("0000111111");
        assertThat(data32.width).isEqualTo(6L);
        assertThat(data32.resamplePeriod).isEqualTo(32L);
        assertThat(data32.actualPeriod).isEqualTo(63);


    }


}