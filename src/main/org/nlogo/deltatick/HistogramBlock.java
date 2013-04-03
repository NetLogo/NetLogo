package org.nlogo.deltatick;

import org.nlogo.deltatick.dnd.PrettyInput;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 4/24/12
 * Time: 1:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class HistogramBlock
        extends CodeBlock
        implements MouseMotionListener,
        MouseListener {

    HistogramBlock histogramBlock = this; // for deleteAction
    JTextField histoNameField;
    org.nlogo.plot.Plot netLogoPlot;
    boolean histo;
    String population;
    String variable;

    public HistogramBlock() {
        super("new plot", ColorSchemer.getColor(3));
        setBorder(org.nlogo.swing.Utils.createWidgetBorder());
        this.histo = false;

        addMouseMotionListener(this);
        addMouseListener(this);

        //BreedBlock uses these 2 data flavors to unpackAsCode and check validity of block.
        // I don't think PlotBlock uses these data flavors - A.
        flavors = new DataFlavor[]{
                DataFlavor.stringFlavor,
                plotBlockFlavor
        };
        for (QuantityBlock quantBlock : getMyBlocks()) {
            for (Map.Entry<String, PrettyInput> entry : inputs.entrySet()) {
                if (entry.getKey().equalsIgnoreCase("breed-type")) {
                    population = entry.getValue().toString();

                }
                if (entry.getKey().equalsIgnoreCase("trait")) {
                    variable = entry.getValue().toString();
                }
            }
        }
    }

    //I think this constructor is for histograms- not sure -A. (sept 26)
    public HistogramBlock(boolean histo) {
        super("new plot", ColorSchemer.getColor(3));
        setBorder(org.nlogo.swing.Utils.createWidgetBorder());
        this.histo = true;

        addMouseMotionListener(this);
        addMouseListener(this);

        flavors = new DataFlavor[]{
                DataFlavor.stringFlavor,
                plotBlockFlavor
        };
    }

    //called in DeltaTickTab to populate plots -A. (sept 26)
    public void setNetLogoPlot(org.nlogo.plot.Plot netLogoPlot) {
        this.netLogoPlot = netLogoPlot;
    }

    // called in DeltaTickTab to get plots -A. (sept 26)
    public org.nlogo.plot.Plot getNetLogoPlot() {
        return netLogoPlot;
    }


    public List<QuantityBlock> getMyBlocks() {
        List<QuantityBlock> blocks = new ArrayList<QuantityBlock>();

        for (CodeBlock block : myBlocks) {
            if (block instanceof QuantityBlock) {
                blocks.add((QuantityBlock) block);
            }
        }

        return blocks;
    }

    public void makeLabel() {
        histoNameField = new PrettyInput(this);
        label.add(removeButton);
        label.add(new JLabel("Histogram of "));
        label.add(histoNameField);
        label.setBackground(getBackground());
    }

    public String getName() {
        return histoNameField.getText();
    }

    public String unPackAsCode() {
        String passBack = " ";
        //System.out.println("HistoBlock ");
        passBack += "  set-current-plot \"" + getName() + "\"\n";
        //passBack += "  set-current-plot \"" + getName() + "\"\n";
        //passBack += "  histogram [ \"" + variable + " ] of " + population + "\n";
        /*
        for (QuantityBlock quantBlock : getMyBlocks()) {
            quantBlock.unPackAsCommand();

            for (JTextField input : quantBlock.inputs.values()) {
                passBack += input.getText() + " ";
            }
            passBack += " ]\"\n";
            passBack += "  " + quantBlock.unPackAsCommand();
        }
        */


        return passBack;
    }

    public void mouseEnter(MouseEvent evt) {
    }

    public void mouseExit(MouseEvent evt) {
    }

    public void mouseEntered(MouseEvent evt) {
    }

    public void mouseExited(MouseEvent evt) {
    }

    public void mouseClicked(MouseEvent evt) {
    }

    public void mouseMoved(MouseEvent evt) {
    }

    public void mouseReleased(MouseEvent evt) {
    }

    int beforeDragX;
    int beforeDragY;

    int beforeDragXLoc;
    int beforeDragYLoc;

    public void mousePressed(MouseEvent evt) {
        Point point = evt.getPoint();
        javax.swing.SwingUtilities.convertPointToScreen(point, this);
        beforeDragX = point.x;
        beforeDragY = point.y;
        beforeDragXLoc = getLocation().x;
        beforeDragYLoc = getLocation().y;
    }

    public void mouseDragged(MouseEvent evt) {
        Point point = evt.getPoint();
        javax.swing.SwingUtilities.convertPointToScreen(point, this);
        this.setLocation(
                point.x - beforeDragX + beforeDragXLoc,
                point.y - beforeDragY + beforeDragYLoc);
    }

    public void setHistoName(String name) {
        histoNameField.setText(name);
    }

    public boolean histogram() {
        return histo;
    }

}

