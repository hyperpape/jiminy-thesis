package com.justinblank.minithesis;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.*;

public class MinithesisTest {

    @Test
    public void testTrivialTestCasePasses() {
        Minithesis.runTest((tc) -> { }, "trivialTest");
    }

    @Test
    public void testExceptionThrowingTestFails() {
        assertThrows(Exception.class, () -> {
            Minithesis.runTest((tc) -> {
                var x = tc.choice(10);
                throw new RuntimeException();
            }, "testExceptionThrowingTestFails");
        });
    }

    @Test
    public void testSimpleIntegerTestPasses() {
	    Minithesis.runTest((tc) -> {
           var x = tc.choice(10);
           var y = tc.choice(10);
           assertTrue(Math.min(x, y) <= x);
        }, "testTrivialTestPasses");
    }

    @Test
    public void testMin() {
	    Minithesis.runTest((tc) -> {
           var x = tc.choice(10);
           var y = tc.choice(10);
           assertTrue(Math.min(x, y) <= x);
        }, "testMin");
    }
    
    @Test
    public void testBadMaxTestFails() {
	    assertThrows(AssertionFailedError.class, () -> Minithesis.runTest((tc) -> {
           var x = tc.choice(10);
           var y = tc.choice(10);
           assertTrue(Math.max(x, y) <= x && Math.max(x, y) <= y);
        }, "testBadMaxTestFails"));
    }

    @Test
    public void testAny() {
        assertThrows(AssertionFailedError.class, () -> Minithesis.runTest((tc) -> {
            var x = tc.choice(10);
            var y = tc.choice(10);
            assertTrue(Math.max(x, y) <= x && Math.max(x, y) <= y);
        }, "testAny"));
    }

    @Test
    public void testSatisfiableAssumptions() {
        Minithesis.runTest((tc) -> {
            var x = tc.choice(10);
            tc.assume(x % 2 == 0);
            assertEquals(0, x % 2);
        }, "testSatisfiableAssumptions");
    }

    @Test
    public void testUnsatisfiableAssumptions() {
        assertThrows(AssertionFailedError.class, () -> Minithesis.runTest((tc) -> {
            var x = tc.choice(10);
            tc.assume(x % 2 == 1);
            assertEquals(0, x % 2);
        }, "testUnSatisfiableAssumptions"));
    }
}
