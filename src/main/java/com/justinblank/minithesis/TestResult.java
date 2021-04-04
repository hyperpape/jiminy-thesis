package com.justinblank.minithesis;

import java.util.Optional;
import java.util.function.Function;

class TestResult<T> {

    private final TestStatus err;
    private final T value;

    private TestResult(TestStatus s, T t) {
        this.err = s;
        this.value = t;
    }

    static <T> TestResult<T> error(TestStatus s) {
        return new TestResult<>(s, null);
    }

    static <T> TestResult<T> success(T t) {
        return new TestResult<>(null, t);
    }

    T unwrap() {
        if (value == null) {
            // TODO: I *think* it makes sense, because a lack of value would happen because of an overrun, but
            //  confirm/document
            throw new Minithesis.OverrunException();
        }
        return value;
    }

    public Optional<T> value() {
	    return Optional.ofNullable(value);
    }
    
    public Optional<TestStatus> error() {
	    return Optional.ofNullable(err);
    }

    public boolean isValid() {
        return err == null || err == TestStatus.INTERESTING;
    }

    public <R> TestResult<R> map(Function<T,R> f) {
        if (value != null) {
            return TestResult.success(f.apply(value));
        }
        else {
            return TestResult.error(err);
        }
    }
}
