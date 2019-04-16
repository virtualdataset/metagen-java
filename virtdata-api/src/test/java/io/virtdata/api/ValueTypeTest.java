package io.virtdata.api;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ValueTypeTest {

    @Test
    public void testMatchingRawObject() {
        ValueType vt = ValueType.valueOfClassName("Object");
        assertThat(vt).isEqualTo(ValueType.OBJECT);

    }

}