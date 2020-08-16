package com.justinblank.minithesis;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.*;

public class MinithesisTest {

    @Test
    public void testTrivialTestPasses() {
	    Minithesis.runTest((tc) -> {
           var x = tc.choice(10).unwrap();
           var y = tc.choice(10).unwrap();
           assertTrue(Math.min(x, y) <= x);
        }, "testTrivialTestPasses");
    }

    @Test
    public void testMin() {
	    Minithesis.runTest((tc) -> {
           var x = tc.choice(10).unwrap();
           var y = tc.choice(10).unwrap();
           assertTrue(Math.min(x, y) <= x && Math.min(x, y) <= y);
        }, "testMin");
    }
    
    @Test
    public void testBadMaxTestFails() {
	    assertThrows(AssertionFailedError.class, () -> Minithesis.runTest((tc) -> {
           var x = tc.choice(10).unwrap();
           var y = tc.choice(10).unwrap();
           assertTrue(Math.max(x, y) <= x && Math.max(x, y) <= y);
        }, "testBadMaxTestFails"));
    }

    @Test
    public void testAny() {
        assertThrows(AssertionFailedError.class, () -> Minithesis.runTest((tc) -> {
            var x = tc.choice(10).unwrap();
            var y = tc.choice(10).unwrap();
            assertTrue(Math.max(x, y) <= x && Math.max(x, y) <= y);
        }, "testAny"));
    }

    @Test
    public void testSatisfiableAssumptions() {
        Minithesis.runTest((tc) -> {
            var x = tc.choice(10).unwrap();
            tc.assume(x % 2 == 0);
            assertEquals(0, x % 2);
        }, "testSatisfiableAssumptions");
    }

    @Test
    public void testUnsatisfiableAssumptions() {
        assertThrows(AssertionFailedError.class, () -> Minithesis.runTest((tc) -> {
            var x = tc.choice(10).unwrap();
            tc.assume(x % 2 == 1);
            assertEquals(0, x % 2);
        }, "testUnSatisfiableAssumptions"));
    }
}
