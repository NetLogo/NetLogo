package org.nlogo.deltatick;

import org.nlogo.deltatick.xml.Breed;
import org.nlogo.deltatick.xml.Envt;
import org.nlogo.deltatick.xml.ModelBackgroundInfo;
import org.nlogo.deltatick.xml.ModelBackgroundInfo2;
import org.nlogo.window.GUIWorkspace;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mwilkerson
 * Date: Mar 8, 2010
 * Time: 5:13:59 PM
 * To change this template use File | Settings | File Templates.
 */

// Transferable defines the interface for classes that can be used to provide data for a transfer operation -A. (sept 8)
public class BuildPanel
        extends JPanel
        implements Transferable {

    GUIWorkspace workspace;

    // Linked list is a class. In addition to implementing the List interface, the LinkedList class provides
    // uniformly named methods to get, remove and insert an element at the beginning and end of the list. -a.

    List<BreedBlock> myBreeds = new LinkedList<BreedBlock>();
    List<TraitBlock> myTraits = new LinkedList<TraitBlock>();
    List<TraitBlockNew> myTraitsNew = new LinkedList<TraitBlockNew>();
    List<PlotBlock> myPlots = new LinkedList<PlotBlock>();
    List<HistogramBlock> myHisto = new LinkedList<HistogramBlock>();
    List<EnvtBlock> myEnvts = new LinkedList<EnvtBlock>();
    ModelBackgroundInfo bgInfo = new ModelBackgroundInfo();
    //ModelBackgroundInfo2 bgInfo2 = new ModelBackgroundInfo2();
    JLabel label;


    //DataFlavor"[]" is an array - A. (sept 8)
    DataFlavor[] flavors = new DataFlavor[]{
            DataFlavor.stringFlavor
    };

    public BuildPanel(GUIWorkspace workspace) {
        super();
        this.workspace = workspace;
        setBackground(java.awt.Color.white);
        setLayout(null);
        validate();
    }

    public Object getTransferData(DataFlavor dataFlavor)
            throws UnsupportedFlavorException {
        if (isDataFlavorSupported(dataFlavor)) {
            return unPackAsCode();
        }
        return null;
    }

    public String unPackAsCode() {
        String passBack = "";

        for (BreedBlock breedBlock : myBreeds) {
            passBack += breedBlock.declareBreed();
        }

        passBack += "\n";
        for (BreedBlock breedBlock : myBreeds) {
            passBack += breedBlock.breedVars();
            // traitBlock declared as breed variable here -A. (Aug 8, 2012)
            HashSet<String> allTraits = new HashSet<String>(); // exclusive list of myTraits & myUsedBehInputs to add in breeds-own
                                                                        //-A. (Aug 10, 2012)
            if ( myTraitsNew.size() > 0 ) {
                for ( TraitBlockNew traitBlock : myTraitsNew ) {
                    if ( traitBlock.getMyParent().plural().equals(breedBlock.plural()) ) {      // TODO check if works March 8, 2013
                        //allTraits.add(breedBlock.plural() + "-" + traitBlock.getName());
                        allTraits.add(traitBlock.getName());
                    }
                }
            }
            for ( String string : allTraits ) {
                passBack += string + "\n";
            }
            passBack += "]\n";
        }

        passBack += "\n";
        for (EnvtBlock envtBlock : myEnvts) {
            passBack += envtBlock.OwnVars();
        }

        passBack += "\n";

        passBack += bgInfo.declareGlobals();

        passBack += "\n";

        //TraitBlock's setup code doesn't come from here at all. It comes from breeds -A. (Aug 8, 2012)
        passBack += bgInfo.setupBlock(myBreeds, myTraits, myEnvts, myPlots);
        passBack += "\n";

        // begin function to go
        passBack += "to go\n";
        passBack += bgInfo.updateBlock(myBreeds, myEnvts);
        for (BreedBlock breedBlock : myBreeds) {
            passBack += breedBlock.unPackAsCode();
        }
        for (EnvtBlock envtBlock : myEnvts) {
            passBack += envtBlock.unPackAsCode();
        }
        if (myPlots.size() > 0) {
            passBack += "do-plotting\n";
        }
        passBack += "tick\n";
        if (myHisto.size() > 0) {
            passBack += "make-histo\n";
        }
        passBack += "end\n";
        passBack += "\n";

        //new function: to draw - Aditi (jan 17, 2013)
        passBack += "to draw\n";
        passBack += bgInfo.drawCode() + "\n";
        passBack += "end\n";

        // remaining procedures
        passBack += unPackProcedures();
        passBack += "\n";

        if (myPlots.size() > 0) {
            passBack += "\n\n";
            passBack += "to do-plotting\n";
            for (PlotBlock plot : myPlots) {
                passBack += plot.unPackAsCode();
            }
            passBack += "end\n";
        }

        if (myHisto.size() > 0) {
            passBack += "\n\n";

            for (HistogramBlock hblock : myHisto) {
                passBack += "to make-histo\n";
                passBack += hblock.unPackAsCode();
                for (QuantityBlock qBlock : hblock.getMyBlocks()) {
                    passBack += qBlock.unPackAsCommand();
                }
                //passBack += hblock.getMyBlocks().
            }
            passBack += "end\n";
        }

        return passBack;
    }

public String newSaveAsXML() {
    String passBack = "";
    passBack += "<?xml version=\"1.0\" encoding=\"us-ascii\"?>\n" +
            "<!DOCTYPE model SYSTEM \"DeltaTickModelFormat.dtd\">\n";

    passBack += "<model>\n";

    // declare breeds
    for (BreedBlock breedBlock : myBreeds) {
        passBack += breedBlock.declareBreedXML();

    }

//        passBack += "\n";
//        for (BreedBlock breedBlock : myBreeds) {
//            passBack += breedBlock.breedVars();
//        }
//
//        for (TraitBlock traitBlock : myTraits) {
//            passBack += traitBlock.getTraitName();
//        }
//
//        passBack += "\n";
//
//        passBack += bgInfo.declareGlobals();
//        passBack += "\n";
//
//        passBack += bgInfo.setupBlock(myBreeds, myTraits, myEnvts, myPlots);
//        passBack += "\n";
//
//        passBack += "to go\n";
//        passBack += bgInfo.updateBlock(myBreeds, myEnvts);
//
//        for (BreedBlock breedBlock : myBreeds) {
//            passBack += breedBlock.unPackAsCode();
//        }
//        passBack += "tick\n";
//        if (myPlots.size() > 0) {
//            passBack += "do-plotting\n";
//        }
//        passBack += "end\n";
//        passBack += "\n";
//
//        passBack += "to draw\n";
//        passBack += bgInfo.drawCode() + "\n";
//        passBack += "end\n";
//
//        passBack += unPackProcedures();
//        passBack += "\n";
//
//        if (myPlots.size() > 0) {
//            passBack += "\n\n";
//            passBack += "to do-plotting\n";
//            for (PlotBlock plot : myPlots) {
//                passBack += plot.unPackAsCode();
//            }
//            passBack += "end\n";
//        }
    passBack += "\n</model>";
    return passBack;
    }

   //Michelle's code
    public String saveAsXML() {
        String passBack = "<";

        // declare breeds
        for (BreedBlock breedBlock : myBreeds) {
            passBack += breedBlock.declareBreed();
        }

        passBack += "\n";
        for (BreedBlock breedBlock : myBreeds) {
            passBack += breedBlock.breedVars();
        }

        for (TraitBlock traitBlock : myTraits) {
            passBack += traitBlock.getTraitName();
        }

        passBack += "\n";

        passBack += bgInfo.declareGlobals();
        passBack += "\n";

        passBack += bgInfo.setupBlock(myBreeds, myTraits, myEnvts, myPlots);
        passBack += "\n";

        passBack += "to go\n";
        passBack += bgInfo.updateBlock(myBreeds, myEnvts);

        for (BreedBlock breedBlock : myBreeds) {
            passBack += breedBlock.unPackAsCode();
        }
        passBack += "tick\n";
        if (myPlots.size() > 0) {
            passBack += "do-plotting\n";
        }
        passBack += "end\n";
        passBack += "\n";

        passBack += "to draw\n";
        passBack += bgInfo.drawCode() + "\n";
        passBack += "end\n";

        passBack += unPackProcedures();
        passBack += "\n";

        if (myPlots.size() > 0) {
            passBack += "\n\n";
            passBack += "to do-plotting\n";
            for (PlotBlock plot : myPlots) {
                passBack += plot.unPackAsCode();
            }
            passBack += "end\n";
        }

        return passBack;
    }


//*
//isDataFlavorSupported
//public boolean isDataFlavorSupported(DataFlavor flavor)
    //  Returns whether the requested flavor is supported by this Transferable.
    //Specified by:
    //  isDataFlavorSupported in interface Transferable
    //Parameters:
    //  flavor - the requested flavor for the data
    //Returns:
    //   true if flavor is equal to DataFlavor.stringFlavor or DataFlavor.plainTextFlavor; false if flavor is not one of the above flavors
    //Throws:
    //  NullPointerException - if flavor is null
// -a.

    public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
        for (int i = 0; i < flavors.length; i++) {
            if (dataFlavor.equals(flavors[i])) {
                return true;
            }
        }
        return false;
    }

    // all array being returned -A. (sept 8)
    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    public ModelBackgroundInfo getBgInfo() {
        return bgInfo;
    }


    // find method in DeltaTicktab to add actors -a.
    public void addBreed(BreedBlock block) {
        myBreeds.add(block);
        block.setBounds(0,
                0,
                block.getPreferredSize().width,
                block.getPreferredSize().height);

        add(block);
        block.doLayout();
        block.validate();
        block.repaint();
    }


    // do we want variation to show up inside a breed block or to act like a condition block? - (feb 4)
    //public void addTrait(TraitBlock block) {
    public void addTrait(TraitBlockNew block) {
        block.setBounds(0,
                        0,
                        block.getPreferredSize().width,
                        block.getPreferredSize().height);

        //block.colorButton.setEnabled(false);
        block.doLayout();
        block.validate();
        block.repaint();
        //myTraits.add(block);
        myTraitsNew.add(block);
    }

    public void addOperator(OperatorBlock oBlock) {
        // do I need a list of OperatorBlocks myOperators.add(oBlock);
    }


    public void addPlot(PlotBlock block) {
        myPlots.add(block);
        block.setPlotName("plot " + myPlots.size());
        block.setBounds(200,
                0,
                block.getPreferredSize().width,
                block.getPreferredSize().height);
        //block.getPlotPen();
        add(block);
        block.doLayout();
        block.validate();
        block.repaint();
    }

    public void addHisto(HistogramBlock block) {
        myHisto.add(block);
        block.setHistoName("New Histogram " + myHisto.size());
        block.setBounds(200,
                0,
                block.getPreferredSize().width,
                block.getPreferredSize().height);
        add(block);
        block.doLayout();
        block.validate();
        block.repaint();
    }

    //make linked list for envt? -A. (sept 8)
    public void addEnvt(EnvtBlock block) {
        myEnvts.add(block);
         block.setBounds(400,
                0,
                block.getPreferredSize().width,
                block.getPreferredSize().height);
        add(block);
        block.doLayout();
        block.validate();
        block.repaint();


    }

    public void addPlot(String name, int x, int y) {
        PlotBlock newPlot = new PlotBlock();
        myPlots.add(newPlot);
        newPlot.setPlotName(name);
        newPlot.setBounds(400,
                0,
                newPlot.getPreferredSize().width,
                newPlot.getPreferredSize().height);
        newPlot.setLocation(x, y);
        add(newPlot);
        newPlot.doLayout();
        newPlot.validate();
        newPlot.repaint();
    }


    // Collection<typeObject> object that groups multiple elements into a single unit -A. (sept 8)
    public Collection<BreedBlock> getMyBreeds() {
        return myBreeds;
    }

    public List<PlotBlock> getMyPlots() {
        return myPlots;
    }

    public boolean getMyPlot(String name) {
        boolean check = false;
        for (PlotBlock plotBlock : this.getMyPlots()) {
            if (plotBlock.getName().equalsIgnoreCase(name)) {
                check = true;
            }
        }
        return check;
    }

//    public PlotBlock getMyPlotBlock(String name) {
//        for (PlotBlock plotBlock : this.getMyPlots()) {
//            if (plotBlock.getName().equalsIgnoreCase(name)) {
//                return plotBlock;
//            }
//        }
//    }

    public List<HistogramBlock> getMyHisto() {
        return myHisto;
    }

    public List<TraitBlockNew> getMyTraits() {
        return myTraitsNew;
    }


    public Collection<EnvtBlock> getMyEnvts() {
        return myEnvts;
    }

    //HashMap is a pair of key mapped to values. eg. (key)name1: (value)SSN1 -A. (sept 8)
    public String unPackProcedures() {
        HashMap<String, CodeBlock> procedureCollection = new HashMap<String, CodeBlock>();
        String passBack = "";

        for (BreedBlock breedBlock : myBreeds) {
            if (breedBlock.children() != null) {
                procedureCollection.putAll(breedBlock.children());
            }
        }

        for (PlotBlock plotBlock : myPlots) {
            if (plotBlock.children() != null) {
                procedureCollection.putAll(plotBlock.children());
            }
        }

        for (HistogramBlock histoBlock : myHisto) {
            if (histoBlock.children() != null) {
                procedureCollection.putAll(histoBlock.children());
            }
        }

        for (EnvtBlock envtBlock : myEnvts) {
            if (envtBlock.children() != null) {
                procedureCollection.putAll(envtBlock.children());
            }
        }

        for (String name : procedureCollection.keySet()) {
            passBack += procedureCollection.get(name).unPackAsProcedure();
        }

        return passBack;
    }

    //I think this is where you clear the window to remove everything-a.
    public void clear() {
        myBreeds.clear();
        myPlots.clear();
        myHisto.clear();
        myTraits.clear();
        myEnvts.clear();
        removeAll();
        doLayout();
        //validate();
    }

    @Override
    public void paintComponent(java.awt.Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        for (Component c : getComponents()) {
            c.setBounds(c.getBounds());

        }
    }

    public int breedCount() {
        return myBreeds.size();
    }

    public String[] getbreedNames() {
        String [] names = new String [myBreeds.size()];
        int i = 0;
        for ( BreedBlock breedBlock : myBreeds ) {
            names[i] = breedBlock.plural();
            i++;
        }

        return names;
    }

    public String[] getTraitNames() {
        String[] names = new String [myTraits.size()];
        int i = 0;
        for ( TraitBlock traitBlock : myTraits ) {
            names[i] = traitBlock.getTraitName();
            i++;
        }
        return names;
    }

    public int plotCount() {
        if (myPlots != null) {
            return myPlots.size();
        }
        return 0;
    }

    public int histoCount() {
        if (myHisto != null) {
            return myHisto.size();
        }
        return 0;
    }


    // breeds available in XML -A. (oct 5)
    public ArrayList<Breed> availBreeds() {
        return bgInfo.getBreeds();
    }

    public ArrayList<Envt> availEnvts() {
        return bgInfo.getEnvts();
    }

    public void removePlot(PlotBlock plotBlock) {
        myPlots.remove(plotBlock);
        remove(plotBlock);
    }

    public void removeHisto(HistogramBlock histoBlock) {
        myHisto.remove(histoBlock);
        remove(histoBlock);
    }

    public void removeBreed(BreedBlock breedBlock) {
        myBreeds.remove(breedBlock);
        remove(breedBlock);
    }

    public void removeEnvt(EnvtBlock envtBlock) {
        myEnvts.remove(envtBlock);
        remove(envtBlock);
    }

    public void removeTrait(TraitBlockNew traitBlock) {
        myTraitsNew.remove(traitBlock);
        remove(traitBlock);
    }

    public String library() {
        return bgInfo.getLibrary();
    }

    public ArrayList<String> getVariations () {
        ArrayList<String> tmp = new ArrayList<String>();
        for ( TraitBlock tBlock : myTraits ) {
            tmp = tBlock.varList;
        }
        return tmp;
    }

    public void addRect(String text) {
        label = new JLabel();
        label.setText(text);
        label.setBackground(Color.GRAY);
        label.setBounds(20, 40, 350, 30);
//        label.setBounds(20,
//                30,
//                this.getWidth() - 40,
//                40);
        add(label);
        validate();


    }

    public void removeRect() {
        this.remove(label);
    }
}
