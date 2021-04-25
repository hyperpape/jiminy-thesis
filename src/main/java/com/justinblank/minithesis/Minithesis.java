package com.justinblank.minithesis;

import org.opentest4j.AssertionFailedError;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public class Minithesis {

    public static final String USE_COVERAGE = "UseMinithesisCoverage";

    static {
        var coverageEnabled = System.getenv(USE_COVERAGE);
        if (null != coverageEnabled) {
            System.setProperty(USE_COVERAGE, coverageEnabled);
        }
    }
    
    public static <T> void runTest(Consumer<TestCase> test, String name) {
        runTest(test, name, 100);
    }

    public static <T> void runTest(Consumer<TestCase> test, String name, int examples) {
        Function<TestCase, TestResult<T>> testFunction = wrapConsumer(test);
        DirectoryDB db = null;
        try {
            db = new DirectoryDB(Path.of("target/minithesis-cache"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        boolean useCoverage = useCoverage();
        var randomGen = useCoverage ? new CoverageRandom() : new RandomGen();
        var state = new TestingState<>(randomGen, testFunction, examples);

        runTest(test, name, testFunction, state, db);
    }


    private static boolean useCoverage() {
        return Boolean.parseBoolean(System.getProperty(USE_COVERAGE));
    }

    static <T> void runTest(Consumer<TestCase> test, String name, Function<TestCase, TestResult<T>> testFn, TestingState<T> state, DirectoryDB db) {
        if (db != null) {
            var previous_failure = db.get(name);
            if (previous_failure != null) {
                var tc = TestCase.forChoices(previous_failure, false);
                state.applyTestFunction(tc);
            }
        }

        if (state.getResult() == null) {
            state.run();
        }

        if (state.getValidTestCases() == 0) {
            throw new UnsatisfiableTestCaseException();
        }
        if (state.getResult() == null) {
            if (db != null) {
                db.delete(name);
            }
        } else {
            int[] choices = new int[state.getResult().size()];
            for (int i = 0; i < state.getResult().size(); i++) {
                choices[i] = state.getResult().get(i);
            }

            if (db != null) {
                db.set(name, choices);
            }

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
                return TestResult.error(TestStatus.INVALID);
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
