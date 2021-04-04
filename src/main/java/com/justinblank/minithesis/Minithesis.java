package com.justinblank.minithesis;

import org.opentest4j.AssertionFailedError;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

public class Minithesis {

    public static <T> void runTest(Consumer<TestCase> test, String name) {
        Function<TestCase, TestResult<T>> testFunction = wrapConsumer(test);
        var state = new TestingState<>(new Random(), testFunction, 100);

        runTest(test, name, testFunction, state);
    }

    static <T> void runTest(Consumer<TestCase> test, String name, Function<TestCase, TestResult<T>> testFn, TestingState<T> state) {
        var db = state.getDB();
        var previous_failure = db.get(name);
        if (previous_failure != null) {
            var tc = TestCase.forChoices(previous_failure, false);
            state.applyTestFunction(tc);
        }
        if (state.getResult() == null) {
            state.run();
        }

        if (state.getValidTestCases() == 0) {
            throw new UnsatisfiableTestCaseException();
        }
        if (state.getResult() == null) {
            db.delete(name);
        } else {
            int[] choices = new int[state.getResult().size()];
            for (int i = 0; i < state.getResult().size(); i++) {
                choices[i] = state.getResult().get(i);
            }

            db.set(name, choices);

            var testCase = TestCase.forChoices(new ArrayList<>(state.getResult()), false);
            test.accept(testCase);
        }
        if (state.getResult() != null) {
            testFn.apply(TestCase.forChoices(state.getResult(), true));
        }
    }

    static <T> Function<TestCase, TestResult<T>> wrapConsumer(Consumer<TestCase> test) {
        return (TestCase testCase) -> {
            try {
                test.accept(testCase);
                return TestResult.success(null);
            } catch (InvalidTestCaseException | OverrunException | UnsatisfiableTestCaseException e) {
                return TestResult.error(TestStatus.INVALID); // TODO: think more about this
            } catch (Exception | AssertionFailedError e) {
                return TestResult.error(TestStatus.INTERESTING);
            }
        };
    }

    static class UnsatisfiableTestCaseException extends RuntimeException {
    }

    static class OverrunException extends RuntimeException {
    }

    static class InvalidTestCaseException extends RuntimeException {
    }

}
