/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beige_engine.util.math;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

/**
 * An immutable linear algebra vector. Has methods which facilitate with general
 * linear algebra as well as with beige_engine.graphics.
 *
 * @author Kosmic
 */
public class IntVectorN implements Iterable<Integer> {
    
    /**
     * Creates a vector with a one in index index and zeros everywhere else.
     *
     * @param dim The dimension of the vector.
     * @param index The index which contains a one.
     * @return A dim dimensional vector.
     */
    public static IntVectorN basisVector(int dim, int index) {
        if (index < 0 || index >= dim) {
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }
        IntVectorN nv = new IntVectorN(dim);
        nv.vec[index] = 1;
        return nv;
    }

    /**
     * Constructs a new vector from a stream of doubles.
     *
     * @param s The stream of doubles.
     * @return The new vector.
     */
    public static IntVectorN fromStream(IntStream s) {
        return of(s.toArray());
    }

    public static IntVectorN of(int... vec) {
        IntVectorN v = new IntVectorN(vec.length);
        System.arraycopy(vec, 0, v.vec, 0, vec.length);
        return v;
    }

    /**
     * Creates a vector with ones in every index.
     *
     * @param dim The dimension of the vector.
     * @return A d dimensional vector.
     */
    public static IntVectorN ones(int dim) {
        IntVectorN v = new IntVectorN(dim);
        for (int i = 0; i < dim; i++) {
            v.vec[i] = 1;
        }
        return v;
    }

    /**
     * Creates a d dimensional zero vector.
     *
     * @param dim The dimension of the vector.
     * @return A d dimensional vector.
     */
    public static IntVectorN zeros(int dim) {
        return new IntVectorN(dim);
    }

    /**
     * The dimension of the vector.
     */
    public final int dim;

    private final int[] vec;

    private IntVectorN(int dim) {
        this.dim = dim;
        this.vec = new int[dim];
    }

    /**
     * Adds two vectors together.
     *
     * @param v The other vector.
     * @return A new vector where each index is the sum of the same indeces of
     * the inputs.
     */
    public IntVectorN add(IntVectorN v) {
        assertDimMatches(v);
        IntVectorN nv = new IntVectorN(dim);
        for (int i = 0; i < dim; i++) {
            nv.vec[i] = vec[i] + v.vec[i];
        }
        return nv;
    }

    /**
     * Returns the vector as an array of integers.
     *
     * @return The int array of the component values.
     */
    public int[] asArray() {
        return Arrays.copyOf(vec, dim);
    }

    /**
     * Asserts that the dimension of the vector is dim.
     *
     * @param dim The dimension that the vector must be equal to.
     */
    public void assertDimEquals(int dim) {
        if (dim < 0) {
            throw new IllegalArgumentException("Dimension cannot be smaller than 0");
        }
        if (this.dim != dim) {
            throw new IllegalArgumentException("Dimensions don't match: " + this.dim + " != " + dim);
        }
    }

    /**
     * Throws an exception if the other vector is not the same dimension.
     *
     * @param v The other vector.
     */
    private void assertDimMatches(IntVectorN v) {
        if (dim != v.dim) {
            throw new IllegalArgumentException("Dimensions don't match: " + dim + " != " + v.dim);
        }
    }

    /**
     * Applies the binary operator to each index of the given vector and other.
     *
     * @param applicator The binary operation to apply.
     * @param other The second vector to apply to the binary operation.
     * @return The new vector given from the application.
     */
    public IntVectorN bimap(BinaryOperator<Integer> applicator, IntVectorN other) {
        IntVectorN nv = new IntVectorN(dim);
        for (int i = 0; i < dim; i++) {
            nv.vec[i] = applicator.apply(vec[i], other.vec[i]);
        }
        return nv;
    }

    /**
     * checks whether the vector v is in the same sector (quadrant in 2
     * dimensions) and whether each component is less than or equal to that of
     * the given vector.
     *
     * @param v The vector to compare.
     * @return Whether v is in the same sector and is component-wise less than
     * or equal to the given vector.
     */
    public boolean contains(IntVectorN v) {
        assertDimMatches(v);
        IntVectorN adjusted = bimap((d, t) -> d > 0 ? t : -t, v);
        IntVectorN positiveCompare = map(t -> Math.abs(t));
        for (int i = 0; i < dim; i++) {
            if (adjusted.vec[i] < 0 || positiveCompare.vec[i] < adjusted.vec[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Divides the vector by a scalar.
     *
     * @param scalar The scalar.
     * @return A new scaled vector.
     */
    public VectorN div(double scalar) {
        return VectorN.fromStream(stream().mapToDouble(i -> i / scalar));
    }

    /**
     * Returns the dot product between the two vectors.
     *
     * @param v The other vector.
     * @return The dot product.
     */
    public int dot(IntVectorN v) {
        assertDimMatches(v);
        int sum = 0;
        for (int i = 0; i < dim; i++) {
            sum += vec[i] * v.vec[i];
        }
        return sum;
    }

    @Override
    public boolean equals(Object v) {
        if(v == null){
            return false;
        }
        if (!(v instanceof IntVectorN)) {
            return false;
        }
        IntVectorN w = (IntVectorN) v;
        if (dim != w.dim) {
            return false;
        }
        for (int i = 0; i < dim; i++) {
            if (Math.abs(vec[i] - w.vec[i]) > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the value at the given index of the vector.
     *
     * @param index The index to poll.
     * @return The value at the given index.
     */
    public int get(int index) {
        if (index < 0 || index >= dim) {
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }
        return vec[index];
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Arrays.hashCode(this.vec);
        return hash;
    }

    @Override
    public Iterator<Integer> iterator() {
        return stream().iterator();
    }

    /**
     * Returns the length of the vector.
     *
     * @return The vector's length.
     */
    public double length() {
        return Math.sqrt(lengthSquared());
    }

    /**
     * Returns the square of the length of the vector.
     *
     * @return The vector's length squared.
     */
    public double lengthSquared() {
        return dot(this);
    }

    /**
     * Applies the unary operator to each index of the vector.
     *
     * @param applicator The unary operation to apply.
     * @return The new vector given from the application.
     */
    public IntVectorN map(UnaryOperator<Integer> applicator) {
        IntVectorN nv = new IntVectorN(dim);
        for (int i = 0; i < dim; i++) {
            nv.vec[i] = applicator.apply(vec[i]);
        }
        return nv;
    }

    /**
     * Multiplies the vector by a scalar.
     *
     * @param scalar The scalar.
     * @return A new scaled vector.
     */
    public IntVectorN mult(int scalar) {
        IntVectorN nv = new IntVectorN(dim);
        for (int i = 0; i < dim; i++) {
            nv.vec[i] = vec[i] * scalar;
        }
        return nv;
    }

    /**
     * Multiplies the vector by a scalar.
     *
     * @param scalar The scalar.
     * @return A new scaled vector.
     */
    public VectorN mult(double scalar) {
        return VectorN.fromStream(stream().mapToDouble(i -> i / scalar));
    }

    /**
     * Creates a new vector with every entry the same except for the entry at
     * index, which has the value value.
     *
     * @param index The index to change.
     * @param value The value to put into index.
     * @return A new vector with the index at index changed to value.
     */
    public IntVectorN set(int index, int value) {
        if (index < 0 || index >= dim) {
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }
        IntVectorN nv = new IntVectorN(dim);
        for (int i = 0; i < dim; i++) {
            nv.vec[i] = i == index ? value : vec[i];
        }
        return nv;
    }

    /**
     * Returns a stream containing the values in this vector.
     *
     * @return The stream of values.
     */
    public IntStream stream() {
        return Arrays.stream(vec);
    }

    /**
     * Subtracts v from this vector.
     *
     * @param v The other vector.
     * @return The resultant vector from the subtraction.
     */
    public IntVectorN sub(IntVectorN v) {
        assertDimMatches(v);
        IntVectorN nv = new IntVectorN(dim);
        for (int i = 0; i < dim; i++) {
            nv.vec[i] = vec[i] - v.vec[i];
        }
        return nv;
    }

    @Override
    public String toString() {
        String r = "[";
        for (int i = 0; i < dim; i++) {
            if (i > 0) {
                r += ", ";
            }
            r += vec[i];
        }
        return r + "]";
    }

    /**
     * Returns a VectorN containing the same values as this vector.
     *
     * @return A new VectorN.
     */
    public VectorN toVectorN() {
        return VectorN.fromStream(stream().mapToDouble(i -> i));
    }

    /**
     * Gets the value at index three.
     *
     * @return The value at index three.
     */
    public int w() {
        return get(3);
    }

    /**
     * Gets the value at index zero.
     *
     * @return The value at index zero.
     */
    public int x() {
        return get(0);
    }

    /**
     * Gets the value at index one.
     *
     * @return The value at index one.
     */
    public int y() {
        return get(1);
    }

    /**
     * Gets the value at index two.
     *
     * @return The value at index two.
     */
    public int z() {
        return get(2);
    }

}
