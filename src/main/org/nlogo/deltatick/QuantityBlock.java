package org.nlogo.deltatick;

import org.nlogo.app.WidgetWrapper;
import org.nlogo.deltatick.dnd.ColorButton;
import org.nlogo.window.Widget;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
//import org.nlogo.deltatick.xml.Breed;

public strictfp class QuantityBlock
        extends CodeBlock {

    transient JPanel penColorButton;
    transient JFrame parent;
    Color penColor = Color.black;
    boolean histo = false;
    String bars = "0";
    String trait = " ";
    String population;
    String variable;
    String penSetUpCode;
    String penUpdateCode;
    String penColorString;
    ColorButton colorButton = new ColorButton(parent, this);

    public QuantityBlock(String name, boolean histo, String bars, String trait) {
        super(name, ColorSchemer.getColor(2));
        this.histo = histo;
        this.bars = bars;
        this.trait = trait;
        flavors = new DataFlavor[]{
                DataFlavor.stringFlavor,
                quantityBlockFlavor
        };
        //label.add(makeBreedShapeButton());
        // - quantity block need not have shape change -A. (sept 30)
        label.add(colorButton);
        colorButton.setPreferredSize(new Dimension(30, 30));
    }

    public String unPackAsCode() {
        if (myParent == null) {
            return unPackAsProcedure();
        }
        return unPackAsCommand();
    }


    public String unPackAsProcedure() {

        String passBack = "";
        Container parent = getParent();
        if (parent instanceof PlotBlock) {
        passBack += "to-report " + getName();
        if (inputs.size() > 0) {
            passBack += " [ ";
            for (String input : inputs.keySet()) {
                passBack += input + " ";
            }
            passBack += "]";
        }
        passBack += "\n";
        passBack += "report " + code;
        passBack += "\n";
        passBack += "end";
        passBack += "\n";
        passBack += "\n";
        }

        if (parent instanceof HistogramBlock) {
            passBack = "" ;
        }

        return passBack;
    }


    public String unPackAsCommand() {
        String passBack = "";
        Container parent = getParent();

        /* not being used because HistogramBlock is not used any more -Aditi (Jan 15, 2013)
        if (parent instanceof HistogramBlock) {
            passBack += "set-plot-pen-mode 1 \n";

            for (Map.Entry<String, JTextField> entry : inputs.entrySet()) {
                if (entry.getKey().equalsIgnoreCase("breed-type")) {
                    population = entry.getValue().getText().toString();
                }
                if (entry.getKey().equalsIgnoreCase("trait")) {
                    variable = entry.getValue().getText().toString();
                }
            }
            //passBack += "set-histogram-num-bars " + bars + "\n";
            //passBack += "set-plot-x-range 0 max " + getName() + " ";
            //passBack += "plotxy" + x + y + "\n";
            passBack += "histogram [ " + variable + " ] of " + population ;
            passBack += "\n";
        }
        */

        if (parent instanceof PlotBlock) {
            passBack += "  set-current-plot-pen \"" + this.getName() + "\" \n";
            if (colorButton.gotColor() == true) {
                passBack += "set-plot-pen-color " + colorButton.getSelectedColorName() + "\n";
            }

            if (((PlotBlock) parent).isHisto == true) {
                passBack += "set-plot-pen-mode 1 \n";
                for (Map.Entry<String, JTextField> entry : inputs.entrySet()) {
                if (entry.getKey().equalsIgnoreCase("breed-type")) {
                    population = entry.getValue().getText().toString();
                }
                if (entry.getKey().equalsIgnoreCase("trait")) {
                    variable = entry.getValue().getText().toString();
                }
            }
            passBack += "histogram [ " + variable + " ] of " + population ;
            passBack += "\n";
            }

            else {
                passBack += "plot " + getName() + " ";
                for (JTextField input : inputs.values()) {
                    passBack += input.getText() + " ";
                }
            }

        passBack += "\n";
        }
        penUpdateCode = passBack;
        return passBack;
    }


    public Map<String, JTextField> getInputs() {
        return inputs;
    }

    public void setButtonColor( Color color ) {
        colorButton.setBackground(color);
        colorButton.setOpaque(true);
        colorButton.setBorderPainted(false);
    }


    public void mouseReleased(java.awt.event.MouseEvent event) {
    }

    public void mouseEntered(java.awt.event.MouseEvent event) {
    }

    public void mouseExited(java.awt.event.MouseEvent event) {
    }

    public void mousePressed(java.awt.event.MouseEvent event) {
    }

        /*

        public void mouseClicked(java.awt.event.MouseEvent event) {
            penColor = JColorChooser.showDialog(null, "Pick a pen color...", java.awt.Color.BLACK);
            //if( penColor == null ) { penColor = java.awt.Color.BLACK; }
            this.setBackground(penColor);
            this.setForeground(penColor);
            this.setVisible(true);
            //System.out.println(penColor);
            //System.out.println(penColor.getRGB());
        }

    }

    private final javax.swing.Action colorAction =
            new javax.swing.AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    JColorChooser.showDialog(null, "Pick a pen color...", java.awt.Color.BLACK);

                    //System.out.println(penColor);
                    //System.out.println(penColor.getRGB());
                    //penColorButton.setBackground( penColor );
                }
            };

    public Color getPenColor() {
        //System.out.println(penColor);
        return penColor;
    }
    */
    public String getPenUpdateCode() {
        return penUpdateCode;
    }

}
