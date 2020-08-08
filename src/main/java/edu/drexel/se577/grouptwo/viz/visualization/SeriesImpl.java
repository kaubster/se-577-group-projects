package edu.drexel.se577.grouptwo.viz.visualization;

import edu.drexel.se577.grouptwo.viz.dataset.Attribute;
import edu.drexel.se577.grouptwo.viz.dataset.Value;
import edu.drexel.se577.grouptwo.viz.storage.Dataset;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.ChartUtilities;
import org.jfree.data.category.DefaultCategoryDataset;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

import static edu.drexel.se577.grouptwo.viz.visualization.ScatterPlotViz.Extractor;

final class SeriesImpl extends Visualization.Series {
    private final String name;
    
    SeriesImpl(
            String name,
            String id,
            Dataset dataset,
            Attribute.Arithmetic attribute)
    {
        super(id, dataset, attribute);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Image render() {
        // TODO: implement
        final String row = "";
        final Counter counter = new Counter(0);
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        data().stream().forEach(value -> {
            dataset.addValue(Extractor.extract(value), row, counter.next());
        });
        JFreeChart chart = ChartFactory.createLineChart(
                name, "", attribute.name(),
                dataset, PlotOrientation.VERTICAL,
                false, false, false);
		int width = 500;
		int height = 300;
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ChartUtilities.writeChartAsPNG(stream, chart, width, height);
            return new ImageImpl("image/png",stream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Couldn't render chart", e);
        }
    }

    private static final class Counter {
        private int current;
        Counter(int start) {
            current = start;
        }
        public Integer next() {
            return current++;
        }
    }

    @Override
    public List<Value> data() {
        return getDataset().getSamples().stream()
            .map(samp -> samp.get(attribute.name()))
            .map(Optional::get)
            .collect(Collectors.toList());
    }
}
