package org.nlogo.deltatick;

import org.nlogo.deltatick.reps.Barchart;
import org.nlogo.deltatick.reps.Piechart;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 1/22/13
 * Time: 9:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class TraitDisplay extends JPanel {

    HashMap<String, ChartsPanel>chartsPanelMap;
    PaintSupplier paintSupplier;
    static int defaultPaintIndex = 0;
    JRadioButton button;
    JPanel sidePanel;
    JFrame myFrame;

    public TraitDisplay() {

    }
    public TraitDisplay(JPanel sidePanel, JFrame frame) {
        chartsPanelMap = new HashMap<String, ChartsPanel>();

        this.sidePanel = sidePanel; // so we can resize sidePanel when piechart and histogram are added - Aditi (March 5, 2013)
        this.myFrame = frame;       // so we resize the frame when piechart and histogram are added - Aditi (March 5, 2013)
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//        this.setPreferredSize();
        this.setVisible(true);
    }

    public void updateChart(String traitName, HashMap<String, String> varPercent) {
        // Check if trait has been added
        if (chartsPanelMap.containsKey(traitName)) {
            // Trait found
            // Update corresponding charts
            chartsPanelMap.get(traitName).updateChart(traitName, varPercent);

        }
        else {
            if (varPercent.size() > 0) {
            // Trait not previously present
            // Create corresponding charts
            ChartsPanel chartsPanel = new ChartsPanel(traitName, varPercent);
            chartsPanelMap.put(traitName, chartsPanel);

            this.add(chartsPanelMap.get(traitName));
            }

        }

        // Check if trait is to be removed
        if ((varPercent.size() == 0) &&
             (chartsPanelMap.containsKey(traitName))) {
            // Remove corresponding panel
            this.remove(chartsPanelMap.get(traitName));

            chartsPanelMap.remove(traitName);
        }

        this.validate();
        this.setPreferredSize(new Dimension(600, 250*chartsPanelMap.size()));
        //myFrame.pack();

    }

    private class ChartsPanel extends JPanel {

        HashMap<String, Piechart> selectedTraitPieChart = new HashMap<String, Piechart>();
        HashMap<String, Barchart> selectedTraitBarChart = new HashMap<String, Barchart>();

        public ChartsPanel(String traitName, HashMap<String, String> varPercent) {

            // Set up this panel
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.setPreferredSize(new Dimension(600, 250));
            this.setVisible(true);

            // Trait+Variation selected for the first time, i.e. no previously selected variation
            // Create new chart and add to hashmap and panel
            Piechart piechart = new Piechart(traitName, new PaintSupplier(defaultPaintIndex));
            piechart.updateChart(traitName, varPercent);
            selectedTraitPieChart.put(traitName, piechart);
            // Create the corresponding barchart
            Barchart barchart = new Barchart(traitName, new PaintSupplier(defaultPaintIndex));
            barchart.updateChart(traitName, varPercent);
            selectedTraitBarChart.put(traitName, barchart);

            // Create a JPanel for both charts
            // Add charts to panel
            this.add(selectedTraitPieChart.get(traitName).getChartPanel());
            this.add(selectedTraitBarChart.get(traitName).getChartPanel());
            this.validate();

            this.setVisible(true);
            defaultPaintIndex += 3;

        }

        public void updateChart(String traitName, HashMap<String, String> varPercent) {
            if (selectedTraitPieChart.containsKey(traitName)) {
                if (varPercent.size() > 0) {
                    // At lease one variation is still selected.
                    // Update chart variations and percentages
                    selectedTraitPieChart.get(traitName).updateChart(traitName, varPercent);
                    selectedTraitBarChart.get(traitName).updateChart(traitName, varPercent);
                }
                else {
                    // All variations unselected. Remove this chart

                    this.remove(selectedTraitPieChart.get(traitName).getChartPanel());
                    selectedTraitPieChart.remove(traitName);
                    this.remove(selectedTraitBarChart.get(traitName).getChartPanel());
                    selectedTraitBarChart.remove(traitName);
                }
            }

            this.revalidate();
            this.repaint();

        }

    } // ChartsPanel

    public class PaintSupplier {
        ArrayList<Color> COLORS;
        int resetIndex;
        int paintIndex;

        public PaintSupplier(int defaultIndex) {

            // Set up colors
            COLORS = new ArrayList<Color>();
            COLORS.add(new Color(0xFF, 0x66, 0x66)); // RED
            COLORS.add(new Color(0x66, 0x66, 0xFF)); // PURPLEBLUE
            COLORS.add(new Color(0x66, 0xFF, 0x66)); // GREEN
            COLORS.add(new Color(0xFF, 0xFF, 0x66)); // YELLOW
            COLORS.add(new Color(0x66, 0x00, 0xFF)); // PURPLE
            COLORS.add(new Color(0x66, 0xFF, 0xFF)); // CYAN
            COLORS.add(new Color(0x00, 0x66, 0x00)); // DARK GREEN
            COLORS.add(new Color(0xFF, 0x66, 0xFF)); // PINK
            COLORS.add(new Color(0x66, 0x00, 0x00)); // BROWN
            COLORS.add(new Color(0x66, 0x66, 0x00)); // OLIVE
            COLORS.add(new Color(0x00, 0x00, 0xFF)); // BLUE
            COLORS.add(new Color(0xFF, 0x66, 0x00)); // ORANGE

            resetIndex = defaultIndex % COLORS.size();
            paintIndex = resetIndex;
        }

        public Paint getNextPaint() {
            Paint nextPaint = COLORS.get(paintIndex);
            paintIndex = (paintIndex+1) % COLORS.size();
            return nextPaint;
        }

        public void reset() {
            paintIndex = resetIndex;
        }

    } // Paint Supplier



} // class TraitDisplay

