package edu.drexel.se577.grouptwo.viz.dataset;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface Value {
    void accept(Visitor visitor);

    public interface Countable extends Value, Comparable<Countable> {
    }

    public static final class Arbitrary implements Value, Countable {
        public final String value;

        public Arbitrary(String value) {
            this.value = value;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public int hashCode() {
            return Arbitrary.class.hashCode() + value.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (!Arbitrary.class.isInstance(o)) return false;
            Arbitrary other = Arbitrary.class.cast(o);
            return value.equals(other.value);
        }

        @Override
        public int compareTo(Countable c) {
            if (Arbitrary.class.isInstance(c)) {
                return value.compareTo(Arbitrary.class.cast(c).value);
            }
            // Greater than all other countables;
            return 1;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static final class Int implements Value, Countable {
        public final int value;

        public Int(int value) {
            this.value = value;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public int hashCode() {
            return Int.class.hashCode() + Integer.valueOf(value).hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (!Int.class.isInstance(o)) return false;
            Int other = Int.class.cast(o);
            return value == other.value;
        }

        @Override
        public int compareTo(Countable c) {
            if (Int.class.isInstance(c)) {
                return Integer.compare(value,Int.class.cast(c).value);
            }
            // less than all other countables;
            return -1;
        }

        @Override
        public String toString() {
            return Integer.toString(value);
        }
    }

    public static final class FloatingPoint implements Value {
        public final double value;

        public FloatingPoint(double value) {
            this.value = value;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public int hashCode() {
            return FloatingPoint.class.hashCode() + Double.valueOf(value).hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (!FloatingPoint.class.isInstance(o)) return false;
            FloatingPoint other = FloatingPoint.class.cast(o);
            return value == other.value;
        }

        @Override
        public String toString() {
            return Double.toString(value);
        }
    }

    public static final class Enumerated implements Value, Countable {
        public final String value;

        public Enumerated(String value) {
            this.value = value;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public int hashCode() {
            return Enumerated.class.hashCode() + value.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (!Enumerated.class.isInstance(o)) return false;
            Enumerated other = Enumerated.class.cast(o);
            return value.equals(other.value);
        }

        @Override
        public int compareTo(Countable c) {
            if (Enumerated.class.isInstance(c)) {
                return value.compareTo(Enumerated.class.cast(c).value);
            }
            // Greater than Ints
            if (Int.class.isInstance(c)) return 1;
            // less than all other countables;
            return -1;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static final class Mapping implements Value {
        public final Map<String, Value> mapping = new HashMap<>();

        Mapping() {
        }

        Collection<String> getKeys() {
            return mapping.keySet();
        }

        void put(String name, Value value) {
            mapping.put(name, value);
        }

        public Optional<? extends Value> get(String name) {
            return Optional.ofNullable(mapping.get(name));
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public int hashCode() {
            return Mapping.class.hashCode() + mapping.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (!Mapping.class.isInstance(o)) return false;
            Mapping other = Mapping.class.cast(o);
            return mapping.equals(other.mapping);
        }
    }

    public static interface Visitor {
        void visit(Arbitrary value);
        void visit(Int value);
        void visit(FloatingPoint value);
        void visit(Enumerated value);
        void visit(Mapping mapping);
    }

    public abstract class DefaultVisitor implements Visitor {
        protected abstract void defaulted();

        @Override
        public void visit(Arbitrary value) {
            defaulted();
        }

        @Override
        public void visit(Mapping value) {
            defaulted();
        }

        @Override
        public void visit(Int value) {
            defaulted();
        }

        @Override
        public void visit(Enumerated value) {
            defaulted();
        }

        @Override
        public void visit(FloatingPoint value) {
            defaulted();
        }
    }
}
