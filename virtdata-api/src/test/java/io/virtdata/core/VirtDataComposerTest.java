package io.virtdata.core;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class VirtDataComposerTest {

    @Test
    public void testResolveFunctionFlow() {
        VirtDataComposer composer = new VirtDataComposer();
        ResolverDiagnostics dashrepeats = composer.resolveDiagnosticFunctionFlow("TestableMapper('--', TestingRepeater(2));");
    }

    @Test
    public void testResolveDiagnosticFunctionFlow() {
    }

    @Test
    public void testResolveFunctionFlow1() {
    }
}