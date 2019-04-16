package io.virtdata.testmappers;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestableTemplateTest {

    @Test
    public void testTestableTemplateValidForTesting() {
        TestableTemplate tt = new TestableTemplate(",", String::valueOf, String::valueOf);
        String v = tt.apply(3);
        assertThat(v).isEqualTo("3,3");
    }

}