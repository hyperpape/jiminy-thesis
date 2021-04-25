package com.justinblank.minithesis;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.HashSet;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.justinblank.minithesis.Possibility.lists;
import static com.justinblank.minithesis.Possibility.range;
import static org.junit.jupiter.api.Assertions.*;

public class ShrinkTest {

    @Test
    public void testShrinkSingleInt() {
        Consumer<TestCase> consumer = (TestCase tc) -> {
            var i = tc.choice(1000);
            assertTrue(i > 256 || i < 24);
        };
        Function<TestCase, TestResult<Integer>> testFunction = Minithesis.wrapConsumer(consumer);
        var ts = new TestingState<>(new RandomGen(), testFunction, 1000);
        assertThrows(AssertionFailedError.class, () -> {
            Minithesis.runTest(consumer, "testShrinkSingleInt", testFunction, ts, null);
        });
        var result = ts.getResult().get(0);
        // TODO: is this a bug in shrinking? Compare with minithesis
        assertTrue(result == 24 || result == 25);
    }

    @Test
    public void testFindsSmallList() {
        var ts = new TestingState<Integer>(new RandomGen(), Minithesis.wrapConsumer((tc) -> {
        var list = tc.any(lists(range(0, 10000), 1, 100));
        assertTrue(list.stream().mapToInt(i -> i).sum() <= 1000);
        }), 1000);
        ts.run();
        // This tripped me up for some time, but this only works if the range argument above is 0--the value here is
        // *not* the value applied to the test case, but the choice sequence to
        assertEquals(1001, ts.getResult().get(1));
    }

    @Test
    public void testFindsSmallUniqueValuesForList() {
        var ts = new TestingState<Integer>(new RandomGen(), Minithesis.wrapConsumer((tc) -> {
            var list = tc.any(lists(range(0, 10000), 1, 100));
            tc.assume(list.size() >= 4);
            assertEquals(list.size(), new HashSet<>(list).size());
        }), 1000);
        ts.run();
        assertTrue(ts.getResult().stream().mapToInt((i) -> i).sum() < 20);
    }
}
