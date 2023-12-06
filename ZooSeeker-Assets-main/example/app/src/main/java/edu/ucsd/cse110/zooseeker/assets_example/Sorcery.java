package edu.ucsd.cse110.zooseeker.assets_example;

import java.util.function.Function;

public class Sorcery {
    public static <T,V,R> Function<V, R> chain(
        Function<? super V, ? extends T> f1, Function<? super T, R> f2) {
        return f2.compose(f1);
    }
}
