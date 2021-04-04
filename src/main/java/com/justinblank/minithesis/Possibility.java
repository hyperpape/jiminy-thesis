package com.justinblank.minithesis;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Possibility<T> {

    final Function<TestCase, T> function;

    public Possibility(Function<TestCase, T> function) {
        this.function = function;
    }

    public static <T> Possibility<T> of(T...args) {
        Function<TestCase, T> f = (tc) -> {
            var i = tc._makeChoice(args.length);
            return args[i.unwrap()];
        };
        return new Possibility<T>(f);
    }

    T produce(TestCase tc) {
        return function.apply(tc);
    }

    public static <T> Possibility<T> just(T t) {
        return new Possibility<T>((tc) -> t);
    }

    public static Possibility<Integer> range(int m, int n) {
        return new Possibility<>((tc) -> m + tc._makeChoice(n - m).unwrap());
    }

    public static <S> Possibility<List<S>> lists(Possibility<S> elements, int min_size, int max_size) {
        Function<TestCase, List<S>> function = (tc) -> {
            var result = new ArrayList<S>();
            while (true) {
                if (result.size() < min_size) {
                    // TODO: Error handling
                    var r = tc.forcedChoice(1);
                }
                else if (result.size() >= max_size) {
                    var r = tc.forcedChoice(0);
                    break;
                }
                else if (!tc.weighted(.9).value().orElse(false)) {
                    break;
                }
                result.add(tc.any(elements));
            }
            return result;
        };
        return new Possibility<>(function);
    }

    public static <T> Possibility<T> nothing() {
        return new Possibility<T>((tc) -> {
            tc.reject();
            return null;
        });
    }

    public static <T> Possibility<T> mix(T... args) {
        if (args.length == 0) {
            return nothing();
        }
        else {
            return new Possibility<T>((tc) -> args[tc._makeChoice(args.length).unwrap()]);
        }
    }
}
