package com.justinblank.minithesis;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.*;

public class PossibilityTest {

    @Test
    public void testListPossibilitySizes() {
        Minithesis.runTest((tc) -> {
            var listPos = Possibility.of(1);
            var x = tc.any(Possibility.lists(listPos, 1, 10));
            assertTrue(1 <= x.size());
            assertTrue(10 >= x.size());
        }, "testListPossibilitySizes");
    }

    // Non-deterministic, but should be predictable
    @Test
    public void testListPossibilityChoosesFirstOption() {
        assertThrows(AssertionFailedError.class, () -> Minithesis.runTest((tc) -> {
            var listPos = Possibility.of(1, 2);
            var listInts = tc.any(Possibility.lists(listPos, 1, 10));
            assertTrue(listInts.stream().allMatch((x) -> x.equals(1)));
            assertTrue(listInts.stream().allMatch((x) -> x.equals(2)));
        }, "testListPossibilityChoosesFirstOption"));
    }

    // Non-deterministic, but should be predictable
    @Test
    public void testListPossibilityChoosesSecondOption() {
        assertThrows(AssertionFailedError.class, () -> Minithesis.runTest((tc) -> {
            var listPos = Possibility.of(1, 2);
            var listInts = tc.any(Possibility.lists(listPos, 1, 10));
            assertTrue(listInts.stream().allMatch((x) -> x.equals(1)));
        }, "testListPossibilityChoosesSecondOption"));
    }

    @Test
    public void testMixPossibilityChoosesFirstOption() {
        assertThrows(AssertionFailedError.class, () -> Minithesis.runTest((tc) -> {
            var mix = Possibility.mix(1, 2);
            var i = tc.any(mix);
            assertEquals(1, i);
        }, "testMixPossibilityChoosesFirstOption"));
    }

    @Test
    public void testMixPossibilityChoosesSecondOption() {
        assertThrows(AssertionFailedError.class, () -> Minithesis.runTest((tc) -> {
            var mix = Possibility.mix(1, 2);
            var i = tc.any(mix);
            assertEquals(2, i);
        }, "testMixPossibilityChoosesSecondOption"));
    }

    @Test
    public void testRangesChooseValuesInRange() {
        Minithesis.runTest((tc) -> {
            var range = Possibility.range(1, 10);
            var i = tc.any(range);
            assertTrue(i <= 10);
            assertTrue(i >= 1);
        }, "testMixPossibilityChoosesValuesInRange");
    }

    @Test
    public void testRangesChooseNonMinValues() {
        assertThrows(AssertionFailedError.class, () -> Minithesis.runTest((tc) -> {
            var range = Possibility.range(1, 10);
            var i = tc.any(range);
            assertEquals(1, i);
        }, "testRangesChooseNonMinValues"));
    }

    @Test
    public void testRangesChoosesNonMaxValues() {
        assertThrows(AssertionFailedError.class, () -> Minithesis.runTest((tc) -> {
            var range = Possibility.range(1, 10);
            var i = tc.any(range);
            assertEquals(10, i);
        }, "testRangesChooseNonMaxValues"));
    }
}
