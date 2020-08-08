package edu.drexel.se577.grouptwo.viz.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import edu.drexel.se577.grouptwo.viz.dataset.Definition;
import edu.drexel.se577.grouptwo.viz.dataset.Sample;
import edu.drexel.se577.grouptwo.viz.visualization.Upgrader;
import edu.drexel.se577.grouptwo.viz.visualization.Visualization;

final class MemEngine implements Engine {
    private static Optional<MemEngine> instance = Optional.empty();
    final Map<UUID, Dataset> datasets = new HashMap<>();
    final Map<UUID, Visualization> visualizations = new HashMap<>();

    static MemEngine getInstance() {
        instance = Optional.of(instance.orElseGet(MemEngine::new));
        return instance.get();
    }

    @Override
    public Dataset create(Definition def) {
        MemDataset dataset = new MemDataset(def);
        datasets.put(dataset.id, dataset);
        return dataset;
    }

    @Override
    public Collection<? extends Dataset> listDatasets() {
        return Collections.unmodifiableCollection(datasets.values());
    }

    @Override
    public Optional<Dataset> forId(String id) {
        return Optional.ofNullable(datasets.get(UUID.fromString(id)));
    }

    @Override
    public Visualization createViz(Visualization visualization) {
        UUID id = UUID.randomUUID();
        Visualization prime = Upgrader.upgrade(id.toString(), visualization);
        visualizations.put(id, prime);
        return prime;
    }

    @Override
    public Optional<Visualization> getVisualization(String id) {
        return Optional.ofNullable(visualizations.get(UUID.fromString(id)));
    }

    @Override
    public Collection<Visualization> listVisualizations() {
        return Collections.unmodifiableCollection(visualizations.values());
    }

    static final class MemDataset implements Dataset {
        final UUID id;
        private final Definition definition;
        private final List<Sample> samples;

        {
            id = UUID.randomUUID();
            samples = new ArrayList<>();
        }

        MemDataset(Definition definition) {
            this.definition = definition;
        }

        @Override
        public String getId() {
            return id.toString();
        }

        @Override
        public String getName() {
            return definition.name;
        }

        @Override
        public Definition getDefinition() {
            return definition;
        }

        @Override
        public List<Sample> getSamples() {
            return Collections.unmodifiableList(samples);
        }

        @Override
        public void addSample(Sample sample) {
            // TODO: validate this
            samples.add(sample);
        }
    }
}
