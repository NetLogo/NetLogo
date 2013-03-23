package org.nlogo.deltatick.reps;

import org.apache.log4j.Category;
import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.*;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;
import org.nlogo.deltatick.TraitDisplay;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
//import org.jCharts.axisChart.AxisChart;
//import org.jCharts.axisChart.customRenderers.axisValue.renderers.*;
//import org.jCharts.chartData.*;
//import org.jCharts.properties.*;
//import org.jCharts.properties.util.ChartStroke;
//import org.jCharts.test.TestDataGenerator;
//import org.jCharts.types.ChartType;

// import java.awt.*;

/**
* Created by IntelliJ IDEA.
* User: aditiwagh
* Date: 2/25/13
* Time: 3:18 PM
* To change this template use File | Settings | File Templates.
*/
public class Barchart extends JPanel {
    String trait;
    ChartPanel chartPanel;
    JFreeChart chart;
    DefaultCategoryDataset dataset;
    HashMap<String, Double> selectedVariationsPerc;
    TraitDisplay.PaintSupplier paintSupplier;

//    public Barchart() {
//        trait = new String("Pick a trait");
//        dataset = new DefaultCategoryDataset();
//        selectedVariationsPerc = new HashMap<String, Double>();
//    }
    public Barchart(String traitName, TraitDisplay.PaintSupplier paintSupplier) {

        trait = traitName;
        this.paintSupplier = paintSupplier;
        dataset = new DefaultCategoryDataset();
        selectedVariationsPerc = new HashMap<String, Double>();
        dataset = (DefaultCategoryDataset) createDataset();
        chart = createChart(dataset);

        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(250, 250));
        this.setVisible(true);
        this.validate();


    }

    private CategoryDataset createDataset() {

        this.dataset.clear();
        for (Map.Entry entry: selectedVariationsPerc.entrySet()) {
            this.dataset.addValue((Double) entry.getValue(), (String) entry.getKey(), (String) entry.getKey());
        } // for
        return this.dataset;
    }

    private JFreeChart createChart(CategoryDataset dataset) {
        chart = ChartFactory.createBarChart(
                //trait,
                " ",
                "Variation",
                "Percentage",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );

        // Set Background white
        chart.setBorderVisible(false);
        chart.setBackgroundPaint(null);

        // Get reference to plot for further customizaiton

        final CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(null);
        plot.setDomainGridlinePaint(Color.GRAY);


        // set the range axis to display integers only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setRange(0, 100);

        // disable bar outlines...
        final BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);
        renderer.setMaximumBarWidth(0.25); // 25% of total width
        renderer.setShadowVisible(false);
        renderer.setBarPainter(new StandardBarPainter());

        int i = 0;
        paintSupplier.reset();
        for (Map.Entry entry: selectedVariationsPerc.entrySet()) {
            renderer.setSeriesPaint(i, paintSupplier.getNextPaint());
            i++;
        }


        return chart;
    }

    public void updateChart(String trait, HashMap<String, String> varPercent) {

        this.selectedVariationsPerc.clear();
        for(Map.Entry entry: varPercent.entrySet()) {
            if ((int) Math.round(Double.parseDouble((String) entry.getValue())) > 0) {
                selectedVariationsPerc.put((String) entry.getKey(), Double.parseDouble((String) entry.getValue()));
            }
        } // for

        this.trait = trait;
        CategoryDataset dataset = createDataset();
        chart = createChart(dataset);

        //chart.setTitle(this.trait);
        //((CategoryPlot) chart.getPlot()).setDataset(dataset);

        chartPanel.setChart(chart);
        chartPanel.setPreferredSize(new Dimension(250, 250));

        if (varPercent.size() > 0) {
            chartPanel.setVisible(true);
        }
        else {
            chartPanel.setVisible(false);
        }

        chartPanel.revalidate();

        this.setVisible(true);
        this.validate();
    }

    public JPanel getChartPanel() {
        return chartPanel;
    }

}
