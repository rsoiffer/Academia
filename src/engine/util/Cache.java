package engine.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Cache<A, B> {

    private final Map<A, B> cache = new HashMap();
    private final Function<A, B> loader;

    public Cache(Function<A, B> loader) {
        this.loader = loader;
    }

    public B get(A a) {
        return cache.computeIfAbsent(a, loader);
    }
}
