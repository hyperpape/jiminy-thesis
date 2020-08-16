package com.justinblank.minithesis;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class TestCase {

    private final List<Integer> prefix;
    private final Random random;
    private final int maxSize;
    private final boolean printResults;
    private int depth = 0;
    private int targetingScore = Integer.MIN_VALUE;
    private final List<Integer> choices = new ArrayList<>();

    TestCase(List<Integer> prefix, Random random, int maxSize, boolean printResults) {
        this.prefix = prefix;
        this.random = random;
        this.maxSize = maxSize;
        this.printResults = printResults;
    }

    static TestCase forChoices(List<Integer> choices, boolean printResults) {
        return new TestCase(choices, null, choices.size(), printResults);
    }

    public TestResult<Integer> choice(int n) {
        var result = _makeChoice(n, () -> this.random.nextInt(n));
        if (shouldPrint()) {
            System.out.printf("choice(%d): %d%n", n, result);
        }
        return result;
    }

    TestResult<Integer> forcedChoice(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Can't choose value below 0");
        }
        if (choices.size() > maxSize) {
            return TestResult.error(TestStatus.OVERRUN);
        }
        choices.add(n);
        return TestResult.success(n);
    }

    TestResult<Boolean> weighted(double p) {
        TestResult<Integer> result;
        if (p <= 0) {
             result = forcedChoice(0);
        }
        else if (p >= 1) {
            result = forcedChoice(1);
        }
        else {
            result = _makeChoice(1, () -> this.random.nextDouble() < p ? 1 : 0);
        }
        return result.map((i) -> i == 1);
    }

    public <T> T any(Possibility<T> possibility) {
        try {
            this.depth++;
            var result = possibility.produce(this);
            if (this.shouldPrint()) {
                System.out.println("any(" + possibility + "): " + result);
            }
            return result;
        }
        finally {
            this.depth--;
        }
    }

    TestResult<Integer> _makeChoice(int n, Supplier<Integer> randomFunc) {
        if (n < 0) {
            throw new IllegalArgumentException("Invalid choice: " + n);
        }
        if (n == 0) {
            return TestResult.success(0);
        }
        if (choices.size() >= maxSize) {
            return TestResult.error(TestStatus.OVERRUN);
        }

        int result;
        if (choices.size() < prefix.size()) {
            result = prefix.get(choices.size());
        }
        else {
            result = randomFunc.get();
        }
        if (result > n) {
            return TestResult.error(TestStatus.INVALID);
        }
        this.choices.add(result);
        return TestResult.success(result);
    }

    boolean shouldPrint() {
        // TODO: what's this for?
        return printResults && depth == 0;
    }

    void reject() {
        throw new Minithesis.InvalidTestCaseException();
    }

    public void assume(boolean precondition) {
        if (!precondition) {
            reject();
        }
    }

    void target(int n) {
        this.targetingScore = n;
    }

    int getTargetingScore() {
        return targetingScore;
    }

    List<Integer> getChoices() {
        return choices;
    }
}
