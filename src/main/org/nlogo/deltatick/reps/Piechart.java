package org.nlogo.deltatick.reps;

//import net.sf.cglib.core.Local;
import org.jfree.chart.*;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.nlogo.deltatick.TraitDisplay;
import org.nlogo.deltatick.TraitPreview;
import org.nlogo.deltatick.xml.Variation;
import org.nlogo.headless.Shell;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 2/26/13
 * Time: 9:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class Piechart extends JPanel {


    HashMap<String, Double> selectedVariationsPerc = new HashMap<String, Double>();
    String trait;
    ChartPanel chartPanel;
    JFreeChart chart;
    DefaultPieDataset dataset;
    TraitDisplay.PaintSupplier paintSupplier;
    double startAngle;

//    public Piechart() {
//        this.dummy = new HashMap<String, String>();
//        this.trait = new String("");
//        dataset = (DefaultPieDataset) createDataset();
//        chart = createChart(dataset);
//        chartPanel = new ChartPanel(chart);
//        chartPanel.setVisible(false);
//        chartPanel.setPreferredSize(new Dimension(200, 200));
//        this.setVisible(false);
//        this.validate();
//    }
//
//    public Piechart(HashMap<String, String> map, String trait) {
//        this.dummy = map;
//        this.trait = trait;
//        dataset = (DefaultPieDataset) createDataset();
//        chart = createChart(dataset);
//        chartPanel = new ChartPanel(chart);
//        chartPanel.setPreferredSize(new Dimension(200, 200));
//        this.setVisible(true);
//        this.validate();
//    }

    public Piechart(String traitName, TraitDisplay.PaintSupplier paintSupplier) {
        trait = traitName;
        this.paintSupplier = paintSupplier;
        startAngle = Math.random() * 360.0;
        dataset = new DefaultPieDataset();
        selectedVariationsPerc = new HashMap<String, Double>();
        dataset = (DefaultPieDataset) createDataset();
        chart = createChart(dataset);

        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(320, 250));
        //chartPanel.setMaximumSize(new Dimension(250,250));
        this.setVisible(true);
        this.validate();
    }

    //used to be "static", changed it because i will have more than one dataset -Aditi (feb 27, 2013)?
    private PieDataset createDataset() {
        dataset.clear();
        for (Map.Entry entry: selectedVariationsPerc.entrySet()) {
            dataset.setValue((String) entry.getKey(), (Double) entry.getValue());
        }

//        for (Map.Entry<String, String> map : dummy.entrySet() ) {
//            String variation = map.getKey();
//            double perc = Double.parseDouble(map.getValue());
//            selectedVariationsPerc.put(variation, perc);
//            dataset.setValue(variation, perc);
//        }
        return dataset;
    }

    // public static
    private JFreeChart createChart(PieDataset dataset) {
        //JFreeChart
         chart = ChartFactory.createPieChart(
            trait,  // chart title
            dataset,             // data
            //true,               // include legend
            false,
            true,
            false
        );

        chart.setBorderVisible(false);
        chart.setBackgroundPaint(null);
        chart.setBorderVisible(false);

        PiePlot plot = (PiePlot) chart.getPlot();

        plot.setSectionOutlinesVisible(false);
        plot.setIgnoreZeroValues(true);
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        //plot.setSimpleLabels(true);
        plot.setNoDataMessage("No data available");
        plot.setCircular(false);
        plot.setLabelGap(0.02);
        plot.setBackgroundPaint(null);
        DecimalFormat df = (DecimalFormat) NumberFormat.getPercentInstance(Locale.US);
        df.applyPattern("##.##%");
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({2})", df, df));
        plot.setShadowPaint(null);
        plot.setStartAngle(startAngle);
        plot.setOutlineVisible(false);

        paintSupplier.reset();
        for (Map.Entry entry: selectedVariationsPerc.entrySet()) {
            plot.setSectionPaint((String) entry.getKey(), paintSupplier.getNextPaint());
        }
        // Set Colors
        return chart;
    }


    public JPanel getChartPanel() {
        return chartPanel;
    }

    public void updateChart(String trait, HashMap<String, String> varPercent) {

        this.selectedVariationsPerc.clear();
        for(Map.Entry entry: varPercent.entrySet()) {
            if ((int) Math.round(Double.parseDouble((String) entry.getValue())) > 0) {
                selectedVariationsPerc.put((String) entry.getKey(), Double.parseDouble((String) entry.getValue()));
            }
        } // for


        this.trait = trait;
        PieDataset dataset = createDataset();

        chart = createChart(dataset);
//        chart.setTitle(this.trait);
//        ((PiePlot) chart.getPlot()).setDataset(dataset);

        chartPanel.setChart(chart);

        //chartPanel.setPreferredSize(new Dimension(300, 250));


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
}
