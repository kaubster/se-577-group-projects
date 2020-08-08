package edu.drexel.se577.grouptwo.viz.dataset;

import java.util.Optional;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public final class SampleValidator {
    private final Map<String, ValueValidator> validators = new HashMap<>();

    public SampleValidator(final Definition definition) {
        final Populator populator = new Populator(validators);
        definition.getKeys().stream().forEach(key -> {
            definition.get(key).ifPresent(attr -> attr.accept(populator));
        });
    }

    private final class Populator implements Attribute.Visitor {
        private final Map<String, ValueValidator> validators;
        Populator(Map<String, ValueValidator> validators) {
            this.validators = validators;
        }

        @Override
        public void visit(Attribute.Mapping mapping) {
        }
        
        @Override
        public void visit(Attribute.Arbitrary arb) {
            ArbitraryValidator validator = new ArbitraryValidator();
            validators.put(arb.name(), validator);
        }

        @Override
        public void visit(Attribute.Int attr) {
            IntegerValidator validator =
                new IntegerValidator(attr.max, attr.min);
            validators.put(attr.name(), validator);
        }

        @Override
        public void visit(Attribute.FloatingPoint attr) {
            DoubleValidator validator =
                new DoubleValidator(attr.max, attr.min);
            validators.put(attr.name(), validator);
        }
        @Override
        public void visit(Attribute.Enumerated attr) {
            EnumeratedValidator validator =
                new EnumeratedValidator(attr.choices);
            validators.put(attr.name(), validator);
        }
    }

    private interface ValueValidator {
        boolean check(Value v);
        ValueValidator clone();
    }
    private final class ArbitraryValidator
            extends Value.DefaultVisitor
            implements ValueValidator
    {
        private Optional<Boolean> result = Optional.empty(); 
        ArbitraryValidator() {
        }

        @Override
        protected void defaulted() {
            result = Optional.of(false);
        }

        @Override
        public void visit(Value.Arbitrary value) {
            result = Optional.of(true);
        }

        @Override
        public boolean check(Value v) {
            v.accept(this);
            return result.orElse(false);
        }

        @Override
        public ArbitraryValidator clone() {
            return new ArbitraryValidator();
        }
    }
    private final class IntegerValidator
            extends Value.DefaultVisitor
            implements ValueValidator
    {
        private Optional<Boolean> result = Optional.empty();
        private final int max;
        private final int min;

        IntegerValidator(int max, int min) {
            this.max = max;
            this.min = min;
        }

        @Override
        protected void defaulted() {
            result = Optional.of(false);
        }

        @Override
        public void visit(Value.Int value) {
            result = Optional.of(value.value >= min && value.value <= max);
        }

        @Override
        public boolean check(Value v) {
            v.accept(this);
            return result.orElse(false);
        }

        @Override
        public IntegerValidator clone() {
            return new IntegerValidator(max,min);
        }
    }

    private final class DoubleValidator
            extends Value.DefaultVisitor
            implements ValueValidator
    {
        private Optional<Boolean> result = Optional.empty();
        private final double max;
        private final double min;

        DoubleValidator(double max, double min) {
            this.max = max;
            this.min = min;
        }

        @Override
        protected void defaulted() {
            result = Optional.of(false);
        }

        @Override
        public void visit(Value.FloatingPoint value) {
            result = Optional.of(value.value >= min && value.value <= max);
        }

        @Override
        public boolean check(Value v) {
            v.accept(this);
            return result.orElse(false);
        }

        @Override
        public DoubleValidator clone() {
            return new DoubleValidator(max,min);
        }
    }

    private final class EnumeratedValidator
            extends Value.DefaultVisitor
            implements ValueValidator
    {
        private final Set<String> values;
        private Optional<Boolean> result = Optional.empty();

        EnumeratedValidator(Set<String> values) {
            this.values = Collections.unmodifiableSet(values);
        }

        @Override
        protected void defaulted() {
            result = Optional.of(false);
        }

        @Override
        public void visit(Value.Enumerated value) {
            result = Optional.of(values.contains(value.value));
        }

        @Override
        public boolean check(Value v) {
            v.accept(this);
            return result.orElse(false);
        }

        @Override
        public EnumeratedValidator clone() {
            return new EnumeratedValidator(values);
        }
    }

    public boolean check(final Sample sample) {
        if (!sample.getKeys().equals(validators.keySet())) {
            return false;
        }
        return sample.getKeys().stream()
            .map(key -> {
                return validators.get(key).clone().check(sample.get(key).get());
            }).filter(Boolean.FALSE::equals).findFirst().orElse(true);
    }
}
