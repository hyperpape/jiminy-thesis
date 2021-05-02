package com.justinblank.minithesis;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
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
            var mix = Possibility.mix(Possibility.just(1), Possibility.just(2));
            var i = tc.any(mix);
            assertEquals(1, i);
        }, "testMixPossibilityChoosesFirstOption"));
    }

    @Test
    public void testMixPossibilityChoosesSecondOption() {
        assertThrows(AssertionFailedError.class, () -> Minithesis.runTest((tc) -> {
            var mix = Possibility.mix(Possibility.just(1), Possibility.just(2));
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

    @Test
    public void testStringsAreNonEmpty() {
        assertThrows(AssertionFailedError.class, () -> Minithesis.runTest((tc) -> {
            var strings = Possibility.strings(0, 10, true);
            var s = tc.any(strings);
            assertTrue(s.isEmpty());
        }, "testStringsFindsNonEmptyString"));
    }

    @Test
    public void testAsciiStringsAreAscii() {
        Minithesis.runTest((tc) -> {
            var strings = Possibility.strings(0, 10, true);
            var s = tc.any(strings);
            for (var i = 0; i < s.length(); i++) {
                assertTrue(s.charAt(i) < 128);
            }
        }, "testAsciiStringsAreAscii");
    }

    @Test
    public void testNonAsciiStringsIncludeNonAsciiStrings() {
        assertThrows(AssertionFailedError.class, () -> Minithesis.runTest((tc) -> {
            var strings = Possibility.strings(0, 10, false);
            var s = tc.any(strings);
            for (var i = 0; i < s.length(); i++) {
                assertTrue(s.charAt(i) < 128);
            }
        }, "testAsciiStringsAreAscii"));
    }
}
