package org.nlogo.deltatick;

import com.sun.java.swing.plaf.nimbus.LoweredBorder;
import org.nlogo.api.Shape;
import org.nlogo.app.DeltaTickTab;
import org.nlogo.deltatick.buttons.DottedRect;
import org.nlogo.deltatick.dialogs.ShapeSelector;
import org.nlogo.deltatick.dialogs.Warning;
import org.nlogo.deltatick.dnd.PrettierInput;
import org.nlogo.deltatick.xml.Breed;
import org.nlogo.deltatick.xml.OwnVar;
import org.nlogo.deltatick.dnd.PrettyInput;
import org.nlogo.deltatick.xml.Trait;
import org.nlogo.deltatick.xml.Variation;
import org.nlogo.hotlink.dialogs.ShapeIcon;
import org.nlogo.shape.VectorShape;
import org.nlogo.shape.editor.ImportDialog;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.*;
import java.util.List;

import org.nlogo.deltatick.dialogs.TraitSelectorOld;
//import sun.jvm.hotspot.code.CodeBlob;

// BreedBlock contains code for how whatever happens in BreedBlock is converted into NetLogo code -A. (aug 25)

public strictfp class BreedBlock
        extends CodeBlock
        implements java.awt.event.ActionListener,
        ImportDialog.ShapeParser,
        MouseMotionListener,
        MouseListener {

    // "transient" means the variable's value need not persist when the object is stored  -a.
    String breedShape = "default";
    transient Breed breed;
    transient VectorShape shape = new VectorShape();
    transient Frame parentFrame;
    transient ShapeSelector selector;
    transient JButton breedShapeButton;
    public transient JButton inspectSpeciesButton;
    transient PrettyInput number;
    //transient PrettyInput plural;
    transient PrettierInput plural;
    // HashMap<String, Variation> breedVariationHashMap = new HashMap<String, Variation>(); // assuming single trait -A. (Aug 8, 2012)
    HashSet<String> myUsedBehaviorInputs = new HashSet<String>();
    List<String> myUsedAgentInputs = new ArrayList<String>();
    List<String>myUsedPercentInputs = new ArrayList<String>();
    List<TraitBlockNew>myTraitBlocks = new ArrayList<TraitBlockNew>(); // to have setupTrait code once trait is defined in SpeciesInspector (March 26, 2013)
    String maxAge;
    String maxEnergy;
    String colorName = new String("gray");
    //ShapeSelector myShapeSelector;
    int id;
    transient String trait;
    JTextField traitLabel;
    transient String variation;
    HashSet<String> myUsedTraits = new HashSet<String>();
    boolean hasSpeciesInspector;
    ArrayList<String> traitLabels = new ArrayList<String>();

    JPanel rectPanel;
    boolean removedRectPanel = false;
    boolean reproduceUsed = false;

    // Each breedblock has a species inspector panel
    // Deltaticktab simple 'gets' this panel when inspect species button is clicked and makes it visible
    SpeciesInspectorPanel speciesInspectorPanel;

    //dummy constructor - Aditi (Jan 27, 2013)
   public BreedBlock() {

   }
    // constructor for breedBlock without trait & variation
    public BreedBlock(Breed breed, String plural, Frame frame) {
        super(plural, ColorSchemer.getColor(3));
        this.parentFrame = frame;
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.setLocation(0, 0);
        this.setForeground(color);
        this.breed = breed;
        this.maxAge = breed.getOwnVarMaxReporter("age");
        this.maxEnergy = breed.getOwnVarMaxReporter("energy");
        number.setText(breed.getStartQuant());

        //myShapeSelector = new ShapeSelector( parentFrame , allShapes() , this );
        setBorder(org.nlogo.swing.Utils.createWidgetBorder());

        flavors = new DataFlavor[]{
                DataFlavor.stringFlavor,
                codeBlockFlavor,
                breedBlockFlavor,
                //patchBlockFlavor
        };
    }



    public void addBlock(CodeBlock block) {
        if (block instanceof TraitBlockNew) {
            addTraitBlock(block);
        }
        else {
        myBlocks.add(block);
        this.add(block);
        block.enableInputs();
        block.showRemoveButton();
        this.add(Box.createRigidArea(new Dimension(this.getWidth(), 4)));

        if (removedRectPanel == false) {     //checking if rectPanel needs to be removed
            remove(rectPanel);
            removedRectPanel = true;
            }

        block.setMyParent(this);
        block.doLayout();
        block.validate();
        block.repaint();
//        if (block instanceof TraitBlock) {
//            addTraitBlock(block);
//        }
        if (block instanceof BehaviorBlock) {
            String tmp = ((BehaviorBlock) block).getBehaviorInputName();
            addBehaviorInputToList(tmp);
            String s = ((BehaviorBlock) block).getAgentInputName();
            addAgentInputToList(s);
            String p = ((BehaviorBlock) block).getPercentInputName();
            addPercentInputToList(p);
        }
        else if (block instanceof ConditionBlock) {
            String tmp = ((ConditionBlock) block).getBehaviorInputName();
            addBehaviorInputToList(tmp);
            String s = ((ConditionBlock) block).getAgentInputName();
            addAgentInputToList(s);
            ((ConditionBlock) block).addRect();

        }
        doLayout();
        validate();
        repaint();
        this.getParent().doLayout();
        this.getParent().validate();
        this.getParent().repaint();
    }
    }


    public void addTraitBlock(CodeBlock block) {
        if (((TraitBlockNew) block).getBreedName().equalsIgnoreCase(this.plural()) == false) {// if traitBlock is put in a breedBlock that it's not defined for

            String message = new String(((TraitBlockNew) block).getTraitName() + " is not a trait of " + this.getName());
            JOptionPane.showMessageDialog(null, message, "Oops!", JOptionPane.INFORMATION_MESSAGE);
        }
        else {
            myUsedTraits.add(((TraitBlockNew) block).getTraitName());
            // commented out to test TraitBlockNew (March 25, 2013)
            //((TraitBlockNew) block).enableDropDown();
            //((TraitBlockNew) block).colorButton.setEnabled(true);
            //((TraitBlock) block).addRect("Add blocks here");
            myBlocks.add(block);
//            this.add(block);
//            block.enableInputs();
//            block.showRemoveButton();
//            this.add(Box.createRigidArea(new Dimension(this.getWidth(), 4)));
//            block.setMyParent(this);
//            block.doLayout();
//            block.validate();
//            block.repaint();
//            if (removedRectPanel == false) {     //checking if rectPanel needs to be removed
//            remove(rectPanel);
//            removedRectPanel = true;
//            }
//            doLayout();
//            validate();
//            repaint();
//            this.getParent().doLayout();
//            this.getParent().validate();
//            this.getParent().repaint();
        }
    }

    //TODO: Figure out how breed declaration always shows up first in code
    public String declareBreed() {
        return "breed [ " + plural() + " " + singular() + " ]\n";
    }

    public String declareBreedXML() {
        return "<breedBlock singular=\"" + singular() + "\" plural=\"" + plural() + "\" number=\"" + number.getText() + "\"></breedBlock>\n";
    }

    //this is where breeds-own variables show up in NetLogo code -A. (aug 25)
    public String breedVars() {
        String code = "";
        if (breed.getOwnVars().size() > 0) {
            code += plural() + "-own [\n";
            for (OwnVar var : breed.getOwnVars()) {
                code += "  " + var.name + "\n";
            }
            code += "\n";
        }
        return code;
    }

    // code to setup in NetLogo code window. This method is called in MBgInfo -A.
    public String setup() {
        String code = "";
        if (breed.needsSetupBlock()) {
            code += "create-" + plural() + " " + number.getText() + " [\n";
            if (breed.getSetupCommands() != null) {
                code += breed.getSetupCommands();
            }
            for (OwnVar var : breed.getOwnVars()) {
                if (var.setupReporter != null) {
                    if (var.name.equalsIgnoreCase("energy")) {
                        code += "set " + var.name + " " + "random" + " " + maxEnergy + "\n";
                    }
                    if (var.name.equalsIgnoreCase("age")) {
                        code += "set " + var.name + " " + "random" + " " + maxAge + "\n";
                    }
                }
            }
            code += "set color " + colorName + '\n';
            code += setupTrait();
            code += setupTraitLabels();
            code += "]\n";
            code += setBreedShape();
            int i;
        }
        return code;
    }

    public String setupTrait() {
        String code = "";
        ArrayList<String> setTraits = new ArrayList<String>();  // to make sure setupTrait is called only once

        //for (CodeBlock block: myBlocks) {
        for (TraitBlockNew block : myTraitBlocks) {
            if (block instanceof TraitBlockNew) {
                String traitName =  block.getTraitName();
                if (setTraits.contains(traitName) != true) {
                    code += "let all-" + plural() + "-" + traitName + " sort " + plural() + " \n";
                    setTraits.add(traitName);

                    int i = 0;
                    int startValue = 0;
                    int endValue = 0;

                    //for (Map.Entry<String, Variation> entry : ((TraitBlockNew) block).getVariationHashMap().entrySet()) {
                    Iterator it = block.variationHashMap.entrySet().iterator();
                    while (it.hasNext()) {
//                        String variationType = entry.getKey();
//                        Variation variation = entry.getValue();
                        //String variationType = entry.getKey();
                        Map.Entry entry = (Map.Entry) it.next();
                        Variation variation = (Variation) entry.getValue();

                        // System.out.println("TraitName: " + traitName + " Variation: " + variationType + " Value: " + variation.value);
                        //int k = variation.percent;
                        //int k =  (int) Math.round(((double) (variation.percent/100.0)) * Double.parseDouble(number.getText()));
                        int k =  (int) Math.round(((variation.percent/100.0)) * Double.parseDouble(number.getText()));

                        if (it.hasNext()) {
                            endValue = startValue + k - 1;
                        }
                        else {
                            //endValue = (int) (Integer.parseInt(number.getText()) - 1);
                            endValue = (Integer.parseInt(number.getText()) - 1);
                        }


                        if (endValue > (Integer.parseInt(number.getText()) - 1)) {
                            endValue = Integer.parseInt(number.getText()) - 1;
                        }

                        code += "let " + traitName + i + " sublist all-" + plural() + "-" + traitName +
                                " " + startValue + " " + endValue + "\n";
                        //code += "foreach " + traitName + i + " [ ask ? [ set " + plural() + "-" + traitName + " " + variation.value + " \n";
                        code += "foreach " + traitName + i + " [ ask ? [ set " + traitName + " " + variation.value + " \n";
                        code += " ]] \n";

                        i++;
                        startValue = endValue + 1;
                    }

                }
            }// if
        }
        return code;

    }

    public String setupTraitLabels() {
        String code = "";
        //int i = 0;
        if (traitLabels.size() >= 1) {
            code += "ask " + plural() + "[";
            code += "set label (word ";
            for (int i = 0; i < traitLabels.size(); i++) {
                code += traitLabels.get(i);
                if (traitLabels.size() > 1) {
//                if (i++ == traitLabels.lastIndexOf(traitLabels.get(i)) == false) { // if this is not the last item
                    code += "\"-\"";
                    i--;
                }
            }
            code += " )] \n";
        }
         return code;
    }

    // moves Update Code from XML file to procedures tab - A. (feb 14., 2012)
    public String update() {
        String code = "";
        if (breed.needsUpdateBlock()) {
            code += "ask " + plural() + " [\n";
            if (breed.getUpdateCommands() != null) {
                code += breed.getUpdateCommands();
            }
            for (OwnVar var : breed.getOwnVars()) {
                if (var.updateReporter != null) {
                    code += "set " + var.name + " " + var.updateReporter + "\n";
                }
            }
            code += "]\n";
        }
        return code;
    }

    // very smart! singular is just prefixed plural -A.
    public String singular() {
        return "one-of-" + plural.getText();
    }


    // get text students have entered in prettyInput, plural (march 1)
    public String plural() {
        return plural.getText();
    }

    public void setPlural(String plural) {
        this.plural.setText(plural);
    }

    public void setNumber(String number) {
        this.number.setText(number);
    }

    public void setMaxAge(String age) {
        maxAge = age;
    }
    public String getMaxAge() {
        return maxAge;
    }

    public void setMaxEnergy(String energy) {
        maxEnergy = energy;
    }
    public String getMaxEnergy() {
        return maxEnergy;
    }

    public void setColorName(String color) {
        colorName = color;
    }

    public void addToTraitLabels(String trait) {
        traitLabels.add(trait);
    }

    public ArrayList<String> getTraitLabels() {
        return traitLabels;
    }


    public Object getTransferData(DataFlavor dataFlavor)
            throws UnsupportedFlavorException {
        if (isDataFlavorSupported(dataFlavor)) {
            if (dataFlavor.equals(breedBlockFlavor)) {
                return this;
            }
            if (dataFlavor.equals(envtBlockFlavor)) {
                return this;
            }
            if (dataFlavor.equals(DataFlavor.stringFlavor)) {
                return unPackAsCode();
            }
            if (dataFlavor.equals(traitBlockFlavor)) {
                return unPackAsCode();
            }

        } else {
            return "Flavor Not Supported";
        }
        return null;
    }


    public String unPackAsCode() {
        String passBack = "";

        passBack += "ask " + plural() + " [\n";
        for (CodeBlock block : myBlocks) {
            passBack += block.unPackAsCode();
        }
        passBack += "]\n";

        return passBack;
    }

    public void makeLabel() {
        JPanel label = new JPanel();
        // TODO: This is a hard coded hack for now. Fix it.
//        label.add(removeButton);
        this.showRemoveButton();

        label.add(new JLabel("Ask"));

        number = new PrettyInput(this); // number of turtles of a breed starting with -a.
        number.setText("100");
        label.add(number);

        //plural = new PrettyInput(this);
        plural = new PrettierInput(this);
        plural.setText(getName());
        label.add(plural);

        label.add(makeBreedShapeButton());
        inspectSpeciesButton = new InspectSpeciesButton(this);
        label.add(inspectSpeciesButton);

        addRect();
        label.setBackground(getBackground());
        add(label);
        add(rectPanel);

    }
     //being called from deltaticktab (march 31, 2013)
//    public SpeciesInspectorPanel createSpeciesInspectorPanel() {
//        JFrame jFrame = new JFrame("Species Inspector");
//        //jFrame.setPreferredSize(new Dimension(1000, 700)); // testing jframe size
//        speciesInspectorPanel = new SpeciesInspectorPanel(this, jFrame);
//
//        //speciesInspectorPanelMap.put(myParent, speciesInspectorPanel); // should be in deltaticktab
//        speciesInspectorPanel.addPanels(jFrame.getContentPane());
//        this.setHasSpeciesInspector(true);
//        jFrame.setResizable(true);
//        jFrame.pack();
//        jFrame.setVisible(false);
//        return speciesInspectorPanel;
//    }

    public void addRect() {
        rectPanel = new JPanel();
        rectPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        rectPanel.setPreferredSize(new Dimension(this.getWidth(), 40));
        JLabel label = new JLabel();
        label.setText("Add blocks here");
        rectPanel.add(label);
    }

    public String[] getTraitTypes() {
            String[] traitTypes = new String[breed.getTraitsArrayList().size()];
            int i = 0;
            for (Trait trait : breed.getTraitsArrayList()) {
                traitTypes[i] = trait.getNameTrait();
                i++;
            }
            return traitTypes;
        }

    public ArrayList<Trait> getTraits() {
        return breed.getTraitsArrayList();
    }

    public String[] getVariationTypes(String traitName) {
        String [] variations = null;
        for (Trait trait : breed.getTraitsArrayList()) {
            if (trait.getNameTrait().equals(traitName)) {
                variations = new String[trait.getVariationsList().size()];
                trait.getVariationsList().toArray(variations);
            }
        }
        return variations;
    }

    private final javax.swing.Action pickBreedShape =
            new javax.swing.AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                }
            };

    public JButton makeBreedShapeButton() {
        breedShapeButton = new JButton(new ShapeIcon(org.nlogo.shape.VectorShape.getDefaultShape()));
        breedShapeButton.setActionCommand(this.getName());
        breedShapeButton.addActionListener(this);
        breedShapeButton.setSize(30, 30);
        breedShapeButton.setToolTipText("Change shape");
        return breedShapeButton;
    }

    public class InspectSpeciesButton extends JButton {
        BreedBlock myParent;

        public InspectSpeciesButton(BreedBlock bBlock) {
            this.myParent = bBlock;
            setPreferredSize(new Dimension(30, 30));
            try {
            Image img = ImageIO.read(getClass().getResource("/images/magnify.gif"));
            setIcon(new ImageIcon(img));
            }
            catch (IOException ex) {
             }
            setForeground(java.awt.Color.gray);
            setBorderPainted(true);
            setMargin(new java.awt.Insets(1, 1, 1, 1));
            setToolTipText("Edit species");
        }
    }

    // when clicks on shape selection -a.
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        ShapeSelector myShapeSelector = new ShapeSelector(parentFrame, allShapes(), this);
        myShapeSelector.setVisible(true);
        if (myShapeSelector.getChosenValue() >= 0) {
            ShapeIcon shapeIcon = new ShapeIcon(myShapeSelector.getShape());
            shapeIcon.setColor(myShapeSelector.getSelectedColor());
            breedShapeButton.setIcon(shapeIcon);
            breedShape = myShapeSelector.getChosenShape();
        }
    }

    // getting shapes from NL -a.
    String[] allShapes() {
        String[] defaultShapes =
                org.nlogo.util.Utils.getResourceAsStringArray
                        ("/system/defaultShapes.txt");
        String[] libraryShapes =
                org.nlogo.util.Utils.getResourceAsStringArray
                        ("/system/libraryShapes.txt");
        String[] mergedShapes =
                new String[defaultShapes.length + 1 + libraryShapes.length];
        System.arraycopy(defaultShapes, 0,
                mergedShapes, 0,
                defaultShapes.length);
        mergedShapes[defaultShapes.length] = "";
        System.arraycopy(libraryShapes, 0,
                mergedShapes, defaultShapes.length + 1,
                libraryShapes.length);
        return defaultShapes; // NOTE right now just doing default
    }

    public java.util.List<Shape> parseShapes(String[] shapes, String version) {
        return org.nlogo.shape.VectorShape.parseShapes(shapes, version);
    }

    public String setBreedShape() {
        if (breedShape != null) {
            return "set-default-shape " + plural() + " \"" + breedShape + "\"\n";
        }
        return "";
    }

    public Breed myBreed() {
        return breed;
    }

    public void addTraittoBreed(TraitBlock traitBlock) { // not used -A. (Aug 10, 2012)
        traitBlock.showColorButton();
        traitBlock.doLayout();
        traitBlock.validate();
        traitBlock.repaint();
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


    public void mousePressed(java.awt.event.MouseEvent evt) {
        Point point = evt.getPoint();
        javax.swing.SwingUtilities.convertPointToScreen(point, this);
        beforeDragX = point.x;
        beforeDragY = point.y;
        beforeDragXLoc = getLocation().x;
        beforeDragYLoc = getLocation().y;
    }


    public void mouseDragged(java.awt.event.MouseEvent evt) {
        Point point = evt.getPoint();
        javax.swing.SwingUtilities.convertPointToScreen(point, this);
        this.setLocation(
                point.x - beforeDragX + beforeDragXLoc,
                point.y - beforeDragY + beforeDragYLoc);
    }

    public void repaint() {
        if (parentFrame != null) {
            parentFrame.repaint();
        }
        super.repaint();
    }

// not used any more
// parent.removeTraitBlock() Not needed because traitblock does not directly go inside breedblock (like it used to) april 3, 2013
//    public void removeTraitBlock(TraitBlockNew traitBlock) {
//        remove(traitBlock);
////        breedVariationHashMap.clear(); // Added March 2, 2013
//    }

    public boolean getHasSpeciesInspector () {
        return hasSpeciesInspector;
    }

    public void setHasSpeciesInspector(boolean value) {
        hasSpeciesInspector = value;
    }

    public HashSet<String> getMyUsedTraits() {
        return myUsedTraits;
    }

    public void addTraitBlocktoList(TraitBlockNew block) {
        myTraitBlocks.add(block);
    }
    public void removeAllTraitBlocks() {
        myTraitBlocks.clear();
    }

    public List<TraitBlockNew> getMyTraitBlocks() {
        return myTraitBlocks;
    }

    public void setReproduceUsed (boolean value) {
        reproduceUsed = value;
    }

    public boolean getReproduceUsed () {
        return reproduceUsed;
    }

    public String getNumber() {
        return number.getText();
    }

    public String getBreedShape() {
        return breedShape;
    }

    public String getColorName() {
        return colorName;
    }

    public List<CodeBlock> getMyBlocks() {
        return myBlocks;
    }
}

