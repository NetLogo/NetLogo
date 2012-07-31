package org.nlogo.deltatick;

// need to have some kind of pens
import org.nlogo.deltatick.dnd.RemoveButton;
import org.nlogo.window.Widget;
import org.nlogo.deltatick.dnd.PrettyInput;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.*;
import java.util.List;

public strictfp class PlotBlock
        extends CodeBlock
        implements MouseMotionListener,
        MouseListener {

    PlotBlock plotBlock = this; // for deleteAction
    JTextField plotNameField;
    org.nlogo.plot.Plot netLogoPlot;
    boolean histo;

    public PlotBlock() {
        super("new plot", ColorSchemer.getColor(3));
        setBorder(org.nlogo.swing.Utils.createWidgetBorder());
        this.histo = true;

        addMouseMotionListener(this);
        addMouseListener(this);


        //BreedBlock uses these 2 data flavors to unpackAsCode and check validity of block.
        // I don't think PlotBlock uses these data flavors - A.
        flavors = new DataFlavor[]{
                DataFlavor.stringFlavor,
                plotBlockFlavor
        };
    }

    //I think this constructor is for histograms- not sure -A. (sept 26)
    public PlotBlock(boolean histo) {
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

    /*
    public java.awt.Dimension getMinimumSize() {
        return new java.awt.Dimension( 250 , 200 );
    }

    public Dimension getPreferredSize() {
        return new java.awt.Dimension( 250 , 275 );
    }                  */

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
        plotNameField = new PrettyInput(this);
        label.add(removeButton);
        label.add(new JLabel("Graph of "));
        label.add(plotNameField);
        label.setBackground(getBackground());
    }

    public String getName() {
        return plotNameField.getText();
    }

    public String unPackAsCode() {
        String passBack = "";
        passBack += "  set-current-plot \"" + getName() + "\"\n";

            //?
        for (QuantityBlock quantBlock : getMyBlocks()) {
            passBack += "  set-current-plot-pen \"" + quantBlock.getName() + " ";
            for (JTextField input : quantBlock.inputs.values()) {
                passBack += input.getText() + " ";
            }
            passBack += "\"\n";
            passBack += "  " + quantBlock.unPackAsCommand();
        }

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

    public void setPlotName(String name) {
        plotNameField.setText(name);
    }

    public boolean histogram() {
        return histo;
    }

    public void getPlotPen () {
        netLogoPlot.createPlotPen(plotNameField.getText(), false);
    }

}
