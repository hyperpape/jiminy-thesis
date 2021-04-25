package com.justinblank.minithesis;

import com.justinblank.examples.Example;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class MinithesisCoverageTest {

    private String previousCoverage = "";

    // Leaving this with the annotation, but apparently certain versions of maven don't properly handle JUnit5, so the
    // individual test-cases below explicitly call it, as well as the setup code that could be in a @BeforeEach method
    @AfterEach
    public void clearCoverage() {
        System.setProperty(Minithesis.USE_COVERAGE, previousCoverage != null ? previousCoverage : "");
    }

    // Without coverage, probability of 3 failures is 1/10000. See coveragecalcuation.py in contrib folder
    @Test
    public void testCanFindMediumIntsWithCoverage() {
        previousCoverage = System.getProperty(Minithesis.USE_COVERAGE);
        System.setProperty(Minithesis.USE_COVERAGE, "true");
        try {
            final var bound = (int) Math.pow(2, 20);
            AtomicInteger failures = new AtomicInteger(0);
            for (int i = 0; i < 10; i++) {
                try {
                    Minithesis.runTest((tc) -> {
                        assertFalse(Example.findMediumInts(tc.choice(bound), tc.choice(bound), tc.choice(bound),
                                tc.choice(bound), tc.choice(bound)));
                    }, "FindMediumIntsWithCoverage", 10000);
                } catch (AssertionError e) {
                    failures.incrementAndGet();
                }
            }
            assertThat(failures.get()).isGreaterThan(8);
        }
        finally {
            clearCoverage();
        }
    }

    @Test
    public void testDoesntFindMediumIntsWithoutCoverage() {
        previousCoverage = System.getProperty(Minithesis.USE_COVERAGE);
        System.setProperty(Minithesis.USE_COVERAGE, "false");
        try {
            final var bound = (int) Math.pow(2, 20);
            AtomicInteger failures = new AtomicInteger(0);
            for (int i = 0; i < 10; i++) {
                try {
                    Minithesis.runTest((tc) -> {
                        assertFalse(Example.findMediumInts(tc.choice(bound), tc.choice(bound), tc.choice(bound),
                                tc.choice(bound), tc.choice(bound)));
                    }, "FindMediumIntsWithoutCoverage", 10000);
                }
                catch (AssertionError e) {
                    failures.incrementAndGet();
                }
            }
            assertThat(failures.get()).isLessThan(3);
        }
        finally {
            clearCoverage();
        }

    }
}
