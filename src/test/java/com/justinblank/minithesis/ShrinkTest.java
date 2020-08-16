package com.justinblank.minithesis;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static com.justinblank.minithesis.Possibility.lists;
import static com.justinblank.minithesis.Possibility.range;
import static org.junit.jupiter.api.Assertions.*;

public class ShrinkTest {

    @Test
    public void testShrinkSingleInt() {
        var ts = new TestingState<Integer>(new Random(), Minithesis.wrapConsumer(tc -> {
            var i = tc.choice(1000).unwrap();
            assertTrue(i > 256 || i < 24);
        }), 1000);
        ts.run();
        var result = ts.getResult().get(0);
        // TODO: is this a bug in shrinking? Compare with minithesis
        assertTrue(result == 24 || result == 25);
    }

    @Test
    public void testFindsSmallList() {
        var ts = new TestingState(new Random(), Minithesis.wrapConsumer((tc) -> {
        var list = tc.any(lists(range(0, 10000), 1, 100));
              assertTrue(list.stream().mapToInt(i -> i).sum() <= 1000);
        }), 1000);
        ts.run();
        // This tripped me up for some time, but this only works if the range argument above is 0--the value here is
        // *not* the value applied to the test case, but the choice sequence to
        assertEquals(1001, ts.getResult().get(1));
    }
}
