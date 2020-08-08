package edu.drexel.se577.grouptwo.viz.storage;

import java.util.Collection;
import java.util.Optional;

import edu.drexel.se577.grouptwo.viz.dataset.Definition;
import edu.drexel.se577.grouptwo.viz.visualization.Visualization;

public interface Engine {
    Optional<? extends Dataset> forId(String id);
    Dataset create(Definition definition);
    Collection<? extends Dataset> listDatasets();
    Visualization createViz(Visualization visualization);
    Optional<Visualization> getVisualization(String id);
    Collection<Visualization> listVisualizations();

    public static Engine getInstance() {
        return MongoEngine.getInstance();
    }
}
