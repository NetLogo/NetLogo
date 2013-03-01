package org.nlogo.deltatick.reps;

import com.objectplanet.chart.Chart;
import com.objectplanet.chart.PieChart;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 2/25/13
 * Time: 11:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class Piechart {

    public Piechart() {
        double[] sampleValues = new double[] {643,257,825,829,376};
        //Color[] sampleColors = new Color[] {new Color(0x63639c), new Color(0x6363ff), new Color(0x639cff), new Color(0xc6c6c6), new Color(0x31319c)};
        //Color[] colors = new Color[] {new Color(Black)};
        String[] legendLabels = new String[] {"Monday","Tuesday","Wednesday","Thursday","Friday"};
        initComponents();
    }

        public void initComponents() {
        PieChart chart = new PieChart();
        chart.setTitleOn(true);
        chart.setTitleOn(true);
        chart.setTitle("Weekly Distribution");
        chart.setFont("titleFont", new Font("Serif", Font.BOLD, 20));
        //chart.setSampleCount(sampleValues.length);
        //chart.setSampleColors(sampleColors);
        //chart.setSampleValues(0, sampleValues);
        chart.setValueLabelsOn(true);
        chart.setValueLabelStyle(Chart.INSIDE);
        chart.setFont("insideLabelFont", new Font("Serif", Font.BOLD, 14));
        chart.setLegendOn(true);
        //chart.setLegendLabels(legendLabels);
        chart.setFont("legendFont", new Font("Serif", Font.PLAIN, 13));
        //chart.setSliceSeperatorColor(Color);
        //chart.setBackground(Color.white);

//        com.objectplanet.chart.NonFlickerPanel p = new com.objectplanet.chart.NonFlickerPanel(new BorderLayout());
//        p.add("Center", chart);
//        Frame f = new Frame();
//        f.add("Center", p);
//        f.setSize(450,320);
//        f.show();

    }

    }



