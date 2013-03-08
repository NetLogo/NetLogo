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
            // Trait not previously present
            // Create corresponding charts
            ChartsPanel chartsPanel = new ChartsPanel(traitName, varPercent);
            chartsPanelMap.put(traitName, chartsPanel);

            this.add(chartsPanelMap.get(traitName));
            this.validate();

        }

        // Check is trait is to be removed
        if (varPercent.size() == 0) {
            // Remove corresponding panel
            this.remove(chartsPanelMap.get(traitName));
            this.validate();

            chartsPanelMap.remove(traitName);
        }

    }

    private class ChartsPanel extends JPanel {

        HashMap<String, Piechart> selectedTraitPieChart = new HashMap<String, Piechart>();
        HashMap<String, Barchart> selectedTraitBarChart = new HashMap<String, Barchart>();

        Color[] COLORS;

        public ChartsPanel(String traitName, HashMap<String, String> varPercent) {

            // Set up this panel
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.setVisible(true);

            // Trait+Variation selected for the first time, i.e. no previously selected variation
            // Create new chart and add to hashmap and panel
            Piechart piechart = new Piechart(traitName, new PaintSupplier());
            piechart.updateChart(traitName, varPercent);
            selectedTraitPieChart.put(traitName, piechart);
            // Create the corresponding barchart
            Barchart barchart = new Barchart(traitName, new PaintSupplier());
            barchart.updateChart(traitName, varPercent);
            selectedTraitBarChart.put(traitName, barchart);

            // Create a JPanel for both charts
            // Add charts to panel
            this.add(selectedTraitPieChart.get(traitName).getChartPanel());
            this.add(selectedTraitBarChart.get(traitName).getChartPanel());
            this.validate();

//            this.repaint();
//            sidePanel.setMinimumSize(this.getPreferredSize());
//            sidePanel.setMaximumSize(this.getMaximumSize());
//            sidePanel.revalidate();
//            sidePanel.repaint();
//            //myFrame.getContentPane().setPreferredSize();
//            myFrame.pack();
            this.setVisible(true);
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
        int paintIndex;

        public PaintSupplier() {

            paintIndex = 0;
            // Set up colors
            COLORS = new ArrayList<Color>(6);
            COLORS.add(new Color(0xFF, 0x66, 0x66)); // RED
            COLORS.add(new Color(0x66, 0x66, 0xFF)); // BLUE
            COLORS.add(new Color(0x66, 0xFF, 0x66)); // GREEN
            COLORS.add(new Color(0xFF, 0xFF, 0x66)); // YELLOW
            COLORS.add(new Color(0x66, 0xFF, 0xFF)); // CYAN
            COLORS.add(new Color(0xFF, 0x66, 0xFF)); // PINK
        }

        public Paint getNextPaint() {
            Paint nextPaint = COLORS.get(paintIndex);
            paintIndex = (paintIndex+1) % COLORS.size();
            return nextPaint;
        }

        public void reset() {
            paintIndex = 0;
        }

    } // Paint Supplier



} // class TraitDisplay

