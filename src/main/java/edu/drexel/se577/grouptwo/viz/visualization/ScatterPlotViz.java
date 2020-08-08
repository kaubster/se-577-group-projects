package edu.drexel.se577.grouptwo.viz.visualization;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import edu.drexel.se577.grouptwo.viz.dataset.Attribute;
import edu.drexel.se577.grouptwo.viz.dataset.Sample;
import edu.drexel.se577.grouptwo.viz.dataset.Value;
import edu.drexel.se577.grouptwo.viz.dataset.Value.FloatingPoint;
import edu.drexel.se577.grouptwo.viz.storage.Dataset;

class ScatterPlotViz extends Visualization.Scatter {
    private final String name; 
	
	ScatterPlotViz(String name, String id, Dataset dataset, Attribute.Arithmetic xAxis, Attribute.Arithmetic yAxis)
	{
		super(id, dataset, xAxis, yAxis);
        this.name = name;
	}

    static final class Extractor extends Value.DefaultVisitor {
        private Optional<Double> extracted = Optional.empty();
        private Extractor() {
        }
        @Override
        protected void defaulted() {
            throw new RuntimeException("Invalid Value Type");
        }
        @Override
        public void visit(Value.FloatingPoint fp) {
            extracted = Optional.of(fp.value);
        }
        @Override
        public void visit(Value.Int i) {
            extracted = Optional.of(Integer.valueOf(i.value).doubleValue());
        }
        static double extract(Value v) {
            Extractor extractor = new Extractor();
            v.accept(extractor);
            return extractor.extracted
                .orElseThrow(() -> new RuntimeException("Unknown Value Type"));
        }
    }

	public XYDataset createDataset()
	{
		XYSeriesCollection dataset = new XYSeriesCollection();
		final XYSeries series1 = new XYSeries("");
        List<DataPoint> data = data();
		//double[] key = new double[25], value = new double[25];
        double[] key = data.stream()
            .map(point -> point.x)
            .mapToDouble(Extractor::extract)
            .toArray();
        double[] value = data.stream()
            .map(point -> point.y)
            .mapToDouble(Extractor::extract)
            .toArray();
        data.stream().forEach(point -> {
            series1.add(
                    Extractor.extract(point.x),
                    Extractor.extract(point.y));
        });
		dataset.addSeries(series1);
		return dataset;		
	}
	
	private byte[] createChartPanel() {
		String chartTitle = name;
		String xAxisLabel = xAxis.name();
		String yAxisLabel = yAxis.name();
		XYDataset dataset = createDataset();
		JFreeChart chart = ChartFactory.createScatterPlot(chartTitle, xAxisLabel, yAxisLabel, dataset,PlotOrientation.VERTICAL, false, false, false);
		int width = 500;
		int height = 300;
		try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
			ChartUtilities.writeChartAsPNG(stream, chart, width, height);
            return stream.toByteArray();
		}catch(IOException e) {
            throw new RuntimeException("Couldn't render chart", e);
		}
	}

	@Override
	public Image render() {
        return new ImageImpl("image/png", createChartPanel());
	}

	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<DataPoint> data() {
		// TODO Auto-generated method stub
		return getDataset().getSamples().stream()
            .map(samp -> {
                return new DataPoint(
                        samp.get(xAxis.name()).get(),
                        samp.get(yAxis.name()).get());
            }).collect(Collectors.toList());
	}

}
