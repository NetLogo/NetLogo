package org.nlogo.deltatick.reps;

import org.jfree.chart.*;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.nlogo.deltatick.TraitDistribution;
import org.nlogo.deltatick.TraitPreview;
import org.nlogo.deltatick.xml.Variation;
import org.nlogo.headless.Shell;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 2/26/13
 * Time: 9:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class Piechart1 extends JPanel {
    TraitDistribution traitDistribution;
    HashMap<String, String> dummy = new HashMap<String, String>();
    HashMap<String, Double> selectedVariationsPerc = new HashMap<String, Double>();
    String trait;
    ChartPanel chartPanel;
    JFreeChart chart;

//    public Piechart1(TraitDistribution traitDistribution) {
//        this.traitDistribution = traitDistribution;
//        this.dummy = traitDistribution.getSelectedVariationsPercent();
//        traitDistribution.getSelectedVariationsPercent();
//        PieDataset dataset = createDataset();
//        JFreeChart chart = createChart(dataset);
//        ChartPanel chartPanel = new ChartPanel(chart);
//        chartPanel.setPreferredSize(new Dimension(100, 150));
//        this.setVisible(true);
//        this.validate();
//    }

    public Piechart1() {
        this.dummy = new HashMap<String, String>();
        this.trait = new String("");
        PieDataset dataset = createDataset();
        chart = createChart(dataset);
        chartPanel = new ChartPanel(chart);
        chartPanel.setVisible(false);
        chartPanel.setPreferredSize(new Dimension(200, 200));
        this.setVisible(false);
        this.validate();
    }

    public Piechart1 (HashMap<String, String> map, String trait) {
        this.dummy = map;
        this.trait = trait;
        PieDataset dataset = createDataset();
        chart = createChart(dataset);
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(200, 200));
        this.setVisible(true);
        this.validate();
    }

    //used to be "static", changed it because i will have more than one dataset -Aditi (feb 27, 2013)?
    private PieDataset createDataset() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Map.Entry<String, String> map : dummy.entrySet() ) {
            String variation = map.getKey();
            double perc = Double.parseDouble(map.getValue());
            selectedVariationsPerc.put(variation, perc);
            dataset.setValue(variation, perc);
        }
        return dataset;
    }

    // public static
    private JFreeChart createChart(PieDataset dataset) {
        //JFreeChart
         chart = ChartFactory.createPieChart(
            "Pie Chart",  // chart title
            dataset,             // data
            //true,               // include legend
            false,
            true,
            false
        );


        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionOutlinesVisible(false);
        plot.setIgnoreZeroValues(true);
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 11));
        plot.setNoDataMessage("No data available");
        plot.setCircular(false);
        plot.setLabelGap(0.02);
        plot.setBackgroundPaint(Color.white);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({2})"));

        return chart;
    }

//    public JPanel createPiePanel() {
//        JFreeChart piechart = createChart(createDataset());
//        return new ChartPanel(piechart);
//    }

    public JPanel getChartPanel() {
        return chartPanel;
    }

    public void updatePieChart(HashMap<String, String> map, String trait) {

        this.dummy = map;
        this.trait = trait;
        PieDataset dataset = createDataset();

        //chart = createChart(dataset);
        chart.setTitle(this.trait);
        ((PiePlot) chart.getPlot()).setDataset(dataset);

        chartPanel.setChart(chart);
        //chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(250, 250));
        //chartPanel.setBackground(Color.white);

        if (map.size() > 0) {
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
