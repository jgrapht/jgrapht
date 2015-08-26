package org.jgrapht.alg.util;

import java.util.Objects;

/**
 * Generic pair.
 * <br/>
 * Although the instances of this class are immutable, it is impossible
 * to ensure that the references passed to the constructor will not be
 * modified by the caller.
 *
 */
final public class Pair<A, B> {

    public A first;
    public B second;

    public Pair(A a, B b) {
        this.first  = a;
        this.second = b;
    }

    public boolean equals(Object other) {
        return other instanceof Pair
            && Objects.equals(this.first,   ((Pair) other).first)
            && Objects.equals(this.second,  ((Pair) other).second);
    }

    public int hashCode() {
        return this.first == null
            ? (this.second == null  ? 0 : this.second.hashCode() + 1)
            : (this.second == null  ? this.first.hashCode() + 3 : this.first.hashCode() * 19 + this.second.hashCode());
    }

    /**
     * Creates new pair of elements pulling of the necessity to provide corresponding
     * types of the elements supplied
     *
     * @param a first element
     * @param b second element
     * @return new pair
     */
    public static <A, B> Pair<A, B> of(A a, B b) {
        return new Pair<A, B>(a, b);
    }
}
