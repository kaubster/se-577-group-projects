package edu.drexel.se577.grouptwo.viz.visualization;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

import edu.drexel.se577.grouptwo.viz.dataset.Attribute.Countable;
import edu.drexel.se577.grouptwo.viz.dataset.Value;
import edu.drexel.se577.grouptwo.viz.storage.Dataset;

class HistogramViz extends Visualization.Histogram {
    private final String name;

	HistogramViz(String name, String id, Dataset dataset, Countable attribute) {
		super(id, dataset, attribute);
        this.name = name;
		// TODO Auto-generated constructor stub
	}

    private static final class Counter extends Value.DefaultVisitor {
        private final Map<Value.Countable, Integer> bins =
            new TreeMap<>();
        private static final Value.Arbitrary UNCOUNTABLE =
            new Value.Arbitrary("<uncountable>");
        @Override
        protected void defaulted() {
            incrementBin(UNCOUNTABLE);
        }
        @Override
        public void visit(Value.Arbitrary value) {
            incrementBin(value);
        }
        @Override
        public void visit(Value.Int value) {
            incrementBin(value);
        }

        @Override
        public void visit(Value.Enumerated value) {
            incrementBin(value);
        }

        private void incrementBin(Value.Countable bin) {
            int value = Optional.ofNullable(bins.get(bin)).orElse(0);
            bins.put(bin, value+1);
        }

        List<DataPoint> data() {
            final List<DataPoint> points = new ArrayList<>();
            bins.entrySet().iterator().forEachRemaining(entry -> {
                points.add(new DataPoint(entry.getKey(), entry.getValue()));
            });
            return Collections.unmodifiableList(points);
        }
    }
	
	@Override
	public Image render() {
        final String row = "";
		List<DataPoint> datapoints = data();

        final DefaultCategoryDataset catDataset = new DefaultCategoryDataset();
        datapoints.stream().forEach(point -> {
            catDataset.addValue(point.count, row, point.bin);
        });
        double[] values = datapoints.stream()
            .map(point -> point.count)
            .mapToDouble(count -> Long.valueOf(count).doubleValue())
            .toArray();
        // TODO: arbitrary lables for the various bins.
        /*
		double[] value = new double[datapoints.size()];
		int i = 0;
		for(DataPoint dp : datapoints)
		{
			value[i++] = dp.value.value;
		}
        */
        // Until we can get the right labels
		double[] unique = IntStream.range(0, values.length)
            .mapToDouble(v -> Integer.valueOf(v).doubleValue()).toArray();
		int number = unique.length;
		
		HistogramDataset hdataset = new HistogramDataset();
		hdataset.setType(HistogramType.FREQUENCY);
		hdataset.addSeries("Histogram", values, number);
		String plotTitle = name;
		String xAxis = "Values";
		String yAxis = "Number of Instances";
		PlotOrientation orientation = PlotOrientation.VERTICAL;
		boolean show = false;
		boolean toolTips = false;
		boolean urls = false;
		JFreeChart chart = ChartFactory.createBarChart(plotTitle, xAxis, yAxis, catDataset, orientation, false, false, false);
		int width = 500;
		int height = 300;
        /*
		try {
			ChartUtilities.saveChartAsPNG(new File("histogramTest.png"), chart, width, height);
		}catch(Exception e) {
			e.printStackTrace();
		}
        */
		try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ChartUtilities.writeChartAsPNG(stream, chart, width, height);
			return new ImageImpl("image/png", stream.toByteArray());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException("Couldn't render image", e);
		}
	}

	@Override
	public List<DataPoint> data() {
        // TODO: implement extractors for the ints and string types.
        final Counter counter = new Counter();
        getDataset().getSamples().forEach(sample -> {
            Value value = sample.get(attribute.name())
                .orElseThrow(() -> new RuntimeException("Missing Value"));
            value.accept(counter);
        });
        return counter.data();
        /*
		Optional<Dataset> datasetOp = engine.forId(datasetId);
		Dataset dataset = datasetOp.get();
		List<Sample> list = dataset.getSamples();
		List<DataPoint> dataPoints = new ArrayList<>();
        // Loop being replaced with an aggregator.
		for(Sample loop : list)
		{
			@SuppressWarnings("unchecked")
			Optional <Value.Int> val = (Optional<Value.Int>) loop.get(attribute.name()); 
			dataPoints.add(new DataPoint(val.get(), 1));
		}		
		return dataPoints;
        */
	}

	@Override
	public String getName() {
		return name;
	}

}
