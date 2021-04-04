package com.justinblank.minithesis;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

class TestingState<T> {

    private final Random random;

    private final Function<TestCase, TestResult<T>> testFunction;
    private final int maxExamples;

    private int validTestCases;
    private int calls;
    private List<Integer> result;
    private Object bestScoring;
    private boolean testIsTrivial;

    TestingState(Random random, Function<TestCase, TestResult<T>> testFunction, int maxExamples) {
        this.random = random;
        this.testFunction = testFunction;
        this.maxExamples = maxExamples;
    }

    /**
     * Apply test function, and wrap into a result
     * @param testCase
     * @return
     */
    TestResult<T> testFunction(TestCase testCase) {
        try {
            return testFunction.apply(testCase);
        }
        catch (Exception e) {
            return TestResult.error(TestStatus.INTERESTING);
        }
    }

    List<Integer> getResult() {
        return result;
    }

    void run() {
        generate();
        target();
        shrink();
    }

    private void generate() {
        // TODO: best scoring
        while (shouldKeepGenerating() && validTestCases < maxExamples) {
            // TODO: determine what proper value for printResults is
            applyTestFunction(new TestCase(new ArrayList<>(), random, 8 * 1024, false));
        }
    }

    void applyTestFunction(TestCase testCase) {
	    var testResult = testFunction.apply(testCase);
        calls++;
        if (testCase.getChoices().size() == 0 && (testResult.isValid())) {
            testIsTrivial = true;
        }
        if (testResult.isValid()) {
            validTestCases++;
            if (testCase.getTargetingScore() > Integer.MIN_VALUE) {
                // TODO: Targeting
            }
        }
        if (testResult.error().equals(Optional.of(TestStatus.INTERESTING))) {
            if (result == null || compare(testCase.getChoices(), result) == -1) {
                result = testCase.getChoices();
            }
        }
    }

    private boolean shouldKeepGenerating() {
        return !testIsTrivial && result == null && validTestCases < maxExamples && calls < maxExamples * 10;
    }

    private void target() {
        // TODO
    }


    private void shrink() {
        if (this.result == null) {
            return;
        }

        List<Integer> previous = null;
        // Iterate until our test case reaches a fixed point where we can't shrink it more
        while (!(this.result.equals(previous))) {
            previous = this.result;
            shrinkByDeletion();
            shrinkByZeroing();
            shrinkIndividualValues();
            // TODO: left out a shrink from minithesis--sorting consecutive values
            shrinkBySwapping();
        }
    }

    private void shrinkByDeletion() {
        for (var k = 8; k > 0; k /= 2) {
            for (var i = this.result.size() - k - 1; i >= 0; i--) {
                // successively try to delete chunks from our list of values
                // original: [...i-1, i, ...,i+k,...]
                // deleted version: [...i-1,i+k,...]
                if (i >= this.result.size()) {
                    i = this.result.size() - 1;
                    continue;
                }
                int size = Math.max(0, this.result.size() - k);
                var attempt = new ArrayList<Integer>(size);
                for (var j = 0; j < i; j++) {
                    attempt.add(this.result.get(j));
                }
                for (var j = i; j + k < this.result.size(); j++) {
                    attempt.add(this.result.get(j + k));
                }
                // TODO: there's a case in minithesis I've omitted here

                // TODO: this deserves serious side-eye...where
                consider(attempt);
            }
        }
    }

    private void shrinkByZeroing() {
        var k = 8;
        while (k > 1) {
            var i = this.result.size() - k;
            while (i >= 0) {
                var values = new HashMap<Integer, Integer>();
                var replacement = replace(values);
                if (replacement.isPresent()) {
                    i -= k;
                } else {
                    i--;
                }
            }
            k /= 2;
        }
    }

    private void shrinkIndividualValues() {
        for (var i =  this.result.size() - 1; i >= 0; i--) {
            final var finalI = i;
            binSearchDown(0, this.result.get(i), (v) -> this.replace(finalI, v));
        }
    }

    private void shrinkBySwapping() {
        var k = 2;
        for (int i = this.result.size() - 1 - k; i >= 0; i--) {
            var j = i + k;
            if (j < this.result.size()) {
                if (this.result.get(i) < this.result.get(j)) {
                    replace(Map.of(j, this.result.get(i), i, this.result.get(j)));
                }
                if (j < this.result.size() && this.result.get(i) > 0) {
                    var iPrev = this.result.get(i);
                    var jPrev = this.result.get(j);
                    var finalI = i;
                    // We shrink i and increase j to keep the sum of the two values constant
                    binSearchDown(0, iPrev, (v) -> this.replace(Map.of(finalI, v, j, jPrev + (iPrev - v))));
                }
            }
        }
    }

    private Optional<List<Integer>> consider(List<Integer> attempt) {
        var testCase = TestCase.forChoices(attempt, false);
        var newResult = this.testFunction.apply(testCase);
        if (newResult.error().equals(Optional.of(TestStatus.INTERESTING))) {
            this.result = attempt;
            return Optional.of(attempt);
        }
        return Optional.empty();
    }

    /**
     * Binary search for a value between low and high which "succeeds" according to the passed function.
     *
     * This function does not guarantee that it will find the lowest value that succeeds.
     *
     * @param low the lowest value to use
     * @param high the upper limit of values considered--this value will never be tested with the passed function
     * @param f a function taking the proposed value, returning Optional.empty() on failure, Optional.of(something)
     *         otherwise
     */
    <T> void binSearchDown(int low, int high, Function<Integer, Optional<T>> f) {
        if (f.apply(low).isPresent()) {
            return;
        }
        while (low + 1 < high) {
            var mid = low + (high - low) / 2;
            if (f.apply(mid).isPresent()) {
                high = mid;
            }
            else {
                low = mid;
            }
        }
    }

    /**
     * Try to set index to value, and run the test. Accept the replacement if the test result is interesting.
     * @param index
     * @param value
     * @return Optional.empty() if the test was not interesting, the new list of values, otherwise
     */
    Optional<List<Integer>> replace(int index, int value) {
        var attempt = new ArrayList<>(this.result);
        attempt.set(index, value);
        return consider(attempt);
    }

    Optional<List<Integer>> replace(Map<Integer, Integer> values) {
        var attempt = new ArrayList<>(this.result);
        for (var entry : values.entrySet()) {
            attempt.set(entry.getKey(), entry.getValue());
        }
        return consider(attempt);
    }

    int getValidTestCases() {
        return validTestCases;
    }

    private static int compare(List<Integer> choices1, List<Integer> choices2) {
        int comparison = Integer.compare(choices1.size(), choices2.size());
        if (comparison == 0) {
            for (int i = 0; comparison == 0 && i < choices1.size() && i < choices2.size(); i++) {
                comparison = Integer.compare(choices1.get(i), choices2.get(i));
            }
        }
        return comparison;
    }
}
