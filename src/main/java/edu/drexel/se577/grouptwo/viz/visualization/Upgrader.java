package edu.drexel.se577.grouptwo.viz.visualization;

import java.util.Optional;

import edu.drexel.se577.grouptwo.viz.storage.Dataset;
import edu.drexel.se577.grouptwo.viz.storage.Engine;

/**
 * Class for upgrading stub versions of visualizations to working ones.
 * <p>
 * Dispite the private constructor, this class is NOT a singlton.
 * It is instead intended to be constructed and used only within a
 * static method of this class.
 */
public final class Upgrader implements Visualization.Visitor {

    private final String id;
    private final Dataset dataset;
    private Optional<Visualization> product = Optional.empty();

    private Upgrader(String id, Dataset dataset) {
        this.id = id;
        this.dataset = dataset;
    }

    @Override
    public void visit(Visualization.Histogram hist) {
        // Assert that the attribute exists and matches the version in the
        // dataset.
        dataset.getDefinition().get(hist.attribute.name())
            .filter(hist.attribute::equals)
            .orElseThrow(() -> new RuntimeException("No matching attribute"));
        product = Optional.of(
                new HistogramViz(hist.getName(), id, dataset, hist.attribute));
    }

    @Override
    public void visit(Visualization.Scatter scatter) {
        // TODO: implement
        dataset.getDefinition().get(scatter.xAxis.name())
            .filter(scatter.xAxis::equals)
            .orElseThrow(() -> new RuntimeException("No matching attribute"));
        dataset.getDefinition().get(scatter.yAxis.name())
            .filter(scatter.yAxis::equals)
            .orElseThrow(() -> new RuntimeException("No matching attribute"));
        product = Optional.of(
                new ScatterPlotViz(scatter.getName(), id, dataset, scatter.xAxis, scatter.yAxis));
    }

    @Override
    public void visit(Visualization.Series series) {
        dataset.getDefinition().get(series.attribute.name())
            .filter(series.attribute::equals)
            .orElseThrow(() -> new RuntimeException("No matching attribute"));
        Visualization.Series viz =
            new SeriesImpl(series.getName(), id, dataset, series.attribute);
        product = Optional.of(viz);
    }

    public static Visualization upgrade(String id, Visualization vis) {
        // TODO: implement
        String datasetId = vis.getDataset().getId();
        Dataset dataset = Engine.getInstance().forId(datasetId)
            .orElseThrow(() -> new RuntimeException("No Such Dataset"));
        Upgrader upgrader = new Upgrader(id, dataset);
        vis.accept(upgrader);
        return upgrader.product
            .orElseThrow(() -> new RuntimeException("Could not create real visualization"));
    }
}
