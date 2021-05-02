package com.justinblank.minithesis;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Possibility<T> implements Function<TestCase, T> {

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

    @Override
    public T apply(TestCase tc) {
        return function.apply(tc);
    }

    public static <T> Possibility<T> just(T t) {
        return new Possibility<T>((tc) -> t);
    }

    public static Possibility<Integer> range(int m, int n) {
        return new Possibility<>((tc) -> m + tc._makeChoice(n - m).unwrap());
    }

    public static <S> Possibility<List<S>> lists(Possibility<S> elements, int minSize, int maxSize) {
        return lists((tc) -> tc.any(elements), minSize, maxSize);
    }

    static <S> Possibility<List<S>> lists(Function<TestCase, S> fn, int minSize, int maxSize) {
        Function<TestCase, List<S>> function = (tc) -> {
            var result = new ArrayList<S>();
            while (true) {
                if (result.size() < minSize) {
                    // TODO: Error handling
                    var r = tc.forcedChoice(1);
                }
                else if (result.size() >= maxSize) {
                    var r = tc.forcedChoice(0);
                    break;
                }
                else if (!tc._weighted(.9).value().orElse(false)) {
                    break;
                }
                result.add(fn.apply(tc));
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

    @SafeVarargs
    public static <T> Possibility<T> mix(Possibility<T>... args) {
        if (args.length == 0) {
            return nothing();
        }
        else {
            // TODO: This has a weird result that the first possibility will always be preferred during shrinking
            return new Possibility<T>((tc) -> {
                var choice = tc._makeChoice(args.length).unwrap();
                return tc.any(args[choice]);
            });
        }
    }

    public <S> Possibility<S> map(Function<T, S> fn) {
        return new Possibility<>((tc) -> fn.apply(apply(tc)));
    }

    public static Possibility<String> strings(int minLength, int maxLength, boolean ascii) {
        Possibility<List<Character>> chars = lists((tc) -> {
            if (ascii) {
                return (char) (tc._makeChoice(128).unwrap()).intValue();
            }
            else {
                return (char) (tc._makeChoice(0xD800).unwrap()).intValue();
            }
        }, minLength, maxLength);
        return chars.map((characters) -> {
            var sb = new StringBuilder(characters.size());
            for (var c : characters) {
                sb.append(c);
            }
            return sb.toString();
        });
    }

    public static Possibility<Boolean> bools() {
        return Possibility.of(Boolean.TRUE, Boolean.FALSE);
    }
}
