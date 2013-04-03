package org.nlogo.deltatick;

import org.nlogo.deltatick.dialogs.ColorButton;
import org.nlogo.deltatick.dnd.PrettyInput;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
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
    ColorButton colorButton;

    JLabel image;
    //Image histoImage;
    ImageIcon histoImageIcon;
    //Image lineImage;
    ImageIcon lineImageIcon;



    public QuantityBlock(String name, boolean histo, String bars, String trait) {
        super(name, ColorSchemer.getColor(2));
        this.histo = histo;
        this.bars = bars;
        this.trait = trait;
        flavors = new DataFlavor[]{
                DataFlavor.stringFlavor,
                quantityBlockFlavor
        };

        colorButton = new ColorButton(parent, this);  //commented out for interviewing Gabriel (March 9, 2013)
        //label.add(colorButton);
        updateLabelImage();
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
            //passBack += "  set-current-plot-pen \"" + this.getName() + "\" \n"; // commented 20130319
            passBack += "  set-current-plot-pen \"" + this.getPenName() + "\" \n";
            if (colorButton.gotColor() == true) {
                passBack += "set-plot-pen-color " + colorButton.getSelectedColorName() + "\n";
            }

            if (((PlotBlock) parent).isHisto == true) {
                passBack += "set-plot-pen-mode 1 \n";
                for (Map.Entry<String, PrettyInput> entry : inputs.entrySet()) {
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


    public Map<String, PrettyInput> getInputs() {
        return inputs;
    }

    public void setLabelImage() {
        try {
            image = new JLabel();
              //trying new stuff here to make this work
            image.setTransferHandler(new TransferHandler("button"));
            image.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent evt) {
                    JComponent comp = (JComponent) evt.getSource();
                    TransferHandler th = comp.getTransferHandler();
                    th.exportAsDrag(comp, evt, TransferHandler.COPY);
                }
            });

            histoImageIcon = new ImageIcon(ImageIO.read(getClass().getResource("/images/deltatick/bar-graph.png")));

            lineImageIcon = new ImageIcon(ImageIO.read(getClass().getResource("/images/deltatick/line-graph.png")));
            image.setTransferHandler(new TransferHandler("image"));

            if (histo == true) {
                image.setIcon(histoImageIcon);
                label.add(image);
            }
            else if (histo == false) {
                image.setIcon(lineImageIcon);
                label.add(image);
            }
        }
        catch (IOException ex) {
             }
    }

    public void updateLabelImage() {
        try {
            ////histoImage = ImageIO.read(getClass().getResource("/images/deltatick/bar-graph.png"));
            ////histoImageIcon = new ImageIcon(histoImage);

            histoImageIcon = new ImageIcon(ImageIO.read(getClass().getResource("/images/deltatick/bar-graph.png")));

        if (histo == true) {
            image.setIcon(histoImageIcon);
            image.revalidate();

        }

        }
        catch (IOException ex) {
             }

    }

    public void addColorButton() {

        label.add(colorButton);
    }

    public void setButtonColor( Color color ) {
        colorButton.setBackground(color);
        colorButton.setOpaque(true);
        colorButton.setBorderPainted(false);
    }

    public String getPenName() {
        String passBack = new String();
        passBack += getName();
        for (JTextField input : inputs.values()) {
            passBack += "-" + input.getText();
        }
        return passBack;
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

    public boolean getHisto() {
        return histo;
    }

}
