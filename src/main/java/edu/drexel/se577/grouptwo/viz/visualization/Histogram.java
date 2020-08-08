package edu.drexel.se577.grouptwo.viz.visualization;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

import edu.drexel.se577.grouptwo.viz.dataset.Attribute;

public class Histogram extends Visualization.Histogram {

    private Histogram() {
        super("fake", null, new Attribute.Int("bin", 0, 0));
    }

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		double[] value = new double[100];
		Random generator = new Random();
		for(int i = 0; i<100; i++)
		{
			value[i]=generator.nextDouble();
		}
		double[] unique = Arrays.stream(value).distinct().toArray();
		int number = unique.length;
		HistogramDataset hdataset = new HistogramDataset();
		hdataset.setType(HistogramType.RELATIVE_FREQUENCY);
		hdataset.addSeries("Histogram", value, number);
		String plotTitle = "Histogram";
		String xAxis = "Number";
		String yAxis = "Value";
		PlotOrientation orientation = PlotOrientation.VERTICAL;
		boolean show = false;
		boolean toolTips = false;
		boolean urls = false;
		JFreeChart chart = ChartFactory.createHistogram(plotTitle, xAxis, yAxis, hdataset, orientation, true, toolTips, urls);
		int width = 500;
		int height = 300;
		try {
			ChartUtilities.saveChartAsPNG(new File("E:\\Quarter3\\CS680\\Project\\group-2-project-feature-backend-abstraction-850c96c952f2e552604e1291749012b9278b8e6a\\histogramTest.png"), chart, width, height);
		}catch(Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public Image render() {
		// TODO Auto-generated method stub
		double[] value = new double[100];
		Random generator = new Random();
		for(int i = 0; i<100; i++)
		{
			value[i]=generator.nextDouble();
		}
		Arrays.sort(value);
		double q1 = value[(value.length)/4];
		double q2 = value[(value.length*3/4)];
		int binWidth = (int) ((2 * (q2-q1))/Math.pow(value.length, 3));
		int number = value.length/binWidth;

		HistogramDataset hdataset = new HistogramDataset();
		hdataset.setType(HistogramType.RELATIVE_FREQUENCY);
		hdataset.addSeries("Histogram", value, number);
		String plotTitle = "Histogram";
		String xAxis = "Number";
		String yAxis = "Value";
		PlotOrientation orientation = PlotOrientation.VERTICAL;
		boolean show = false;
		boolean toolTips = false;
		boolean urls = false;
		JFreeChart chart = ChartFactory.createHistogram(plotTitle, xAxis, yAxis, hdataset, orientation, true, toolTips, urls);
		int width = 500;
		int height = 300;
		try {
			ChartUtilities.saveChartAsPNG(new File("E:\\Quarter3\\CS680\\Project\\group-2-project-feature-backend-abstraction-850c96c952f2e552604e1291749012b9278b8e6a\\histogramTest.png"), chart, width, height);
		}catch(Exception e) {
			e.printStackTrace();
		}

		return null;
	}

    @Override
    public List<DataPoint> data() {
        return Collections.emptyList();
    }

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
}
