package edu.drexel.se577.grouptwo.viz;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;

import edu.drexel.se577.grouptwo.viz.dataset.Definition;
import edu.drexel.se577.grouptwo.viz.filetypes.FileInputHandler;
import edu.drexel.se577.grouptwo.viz.storage.Dataset;
import edu.drexel.se577.grouptwo.viz.storage.Engine;
import edu.drexel.se577.grouptwo.viz.visualization.Visualization;

class RealRouting extends Routing {

    @Override
    Collection<? extends Dataset> listDatasets() {
        return Engine.getInstance().listDatasets();
    }

    @Override
    Optional<? extends Dataset> getDataset(String id) {
        return Engine.getInstance().forId(id);
    }

    @Override
    Optional<? extends Visualization> getVisualization(String id) {
        return Engine.getInstance().getVisualization(id);
    }

    @Override
    URI storeVisualization(Visualization def) {
        Visualization realized = Engine.getInstance().createViz(def);
        return URI.create(realized.getId());
    }

    @Override
    URI storeDataset(Definition def) {
        Dataset realized = createDataset(def);
        return URI.create(realized.getId());
    }

    @Override
    Dataset createDataset(Definition def) {
        return Engine.getInstance().create(def);
    }

    @Override
    Optional<? extends FileInputHandler> getFileHandler(String contentType) {
        return FileInputHandler.forType(contentType);
    }

    Collection<? extends Visualization> listVisualizations() {
        return Engine.getInstance().listVisualizations();
    }
}
