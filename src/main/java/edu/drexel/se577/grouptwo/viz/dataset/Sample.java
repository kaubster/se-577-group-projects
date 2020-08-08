package edu.drexel.se577.grouptwo.viz.dataset;

import java.util.Collection;
import java.util.Optional;

import edu.drexel.se577.grouptwo.viz.dataset.Value.Mapping;

public class Sample {
    private final Value.Mapping mapping = new Value.Mapping();

    public void put(String name, Value value) {
        mapping.put(name, value);
    }

    public Optional<? extends Value> get(String name) {
        return mapping.get(name);
    }

    public Collection<String> getKeys() {
        return mapping.getKeys();
    }

}
