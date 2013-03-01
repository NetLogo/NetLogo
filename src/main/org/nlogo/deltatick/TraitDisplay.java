package org.nlogo.deltatick;

import org.nlogo.deltatick.reps.Piechart1;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 1/22/13
 * Time: 9:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class TraitDisplay extends JPanel {
    Piechart1 piechart;

    TraitDistribution traitDistribution;

    public TraitDisplay() {
        this.piechart = new Piechart1();
        this.add(piechart.getChartPanel());
        this.setVisible(true);
    }


    public TraitDisplay(TraitDistribution traitDistribution) {
        this.traitDistribution = traitDistribution;

    }

        }