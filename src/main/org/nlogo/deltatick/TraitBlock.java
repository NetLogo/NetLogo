package org.nlogo.deltatick;

import ch.randelshofer.quaqua.QuaquaComboPopup;
import com.sun.servicetag.SystemEnvironment;
import com.sun.tools.corba.se.idl.StringGen;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.List;
import org.nlogo.deltatick.dialogs.ShapeSelector;
import org.nlogo.deltatick.dnd.ColorButton;
import org.nlogo.deltatick.dnd.PrettyInput;
import org.nlogo.deltatick.dnd.VariationDropDown;
import org.nlogo.deltatick.xml.Breed;
import org.nlogo.deltatick.xml.ModelBackgroundInfo;
import org.nlogo.deltatick.xml.Trait;
import org.nlogo.deltatick.xml.Variation;
import org.nlogo.hotlink.dialogs.ShapeIcon;
import org.nlogo.window.ColorDialog;
import org.parboiled.support.Var;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
//import javax.swing.


import org.parboiled.support.Var;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 2/4/12
 * Time: 1:30 PM
 * To change this template use File | Settings | File Templates.
 */

// this block will hold behaviors for agents with this variation
// not sure if this should be abstract -Feb 4
public strictfp class TraitBlock
        extends CodeBlock
{
    JTextField textName;
    ArrayList<String> varList;
    ArrayList<String> numList;
    LinkedList<Variation> variationList = new LinkedList<Variation>();
    String breedName;
    String traitName;
    JLabel name = new JLabel();

    transient Trait trait;
    transient Frame parentFrame;
    Variation variation;
    JList TraitsList;
    HashMap<String, String> varPercentage;
    HashMap<String, String> traitNumVar = new HashMap<String, String>();
    HashMap<String, Integer> varNum = new HashMap<String, Integer>();
    PrettyInput number;
    String color;
    ColorButton colorButton = new ColorButton(parentFrame, this);
    VariationDropDown dropdownList;

    //variables for second constructor
    HashMap<String, Variation> variationHashMap = new HashMap<String, Variation>();
    HashMap<String, String> variationNamesValues = new HashMap<String, String>();
    HashMap<String, String> variationNumbers = new HashMap<String, String>();
    HashMap<String, String> valueNumbers = new HashMap<String, String>();
    HashMap<String, String> varColorName = new HashMap<String, String>();
    HashMap<String, Color> varColor = new HashMap<String, Color>();

    // this traitBlock constructor is for when students enter trait & variation themselves (Feb 20, 2012)
    public TraitBlock(String breed, String trait, ArrayList<String> variationList, ArrayList<String> numberList,
                      HashMap<String, String> hashMap) {
        super(trait, ColorSchemer.getColor(1));
        flavors = new DataFlavor[]{
                DataFlavor.stringFlavor,
                traitBlockFlavor,
                CodeBlock.codeBlockFlavor};
        this.varList = variationList;
        this.numList = numberList;
        this.breedName = breed;
        this.traitName = trait;
        this.varPercentage = hashMap;

        varColor = new HashMap<String, Color>();
        for (String string : varList) {
            varColor.put(string, Color.lightGray);
        }

        //colorButton.setSize(4, 2);
        label.add(colorButton);

        //colorButton.setVisible(false);

        JPanel newPanel = new JPanel();
        name.setText(breedName);
        label.add(name);

        dropdownList = new VariationDropDown(varList, this);
        label.add(dropdownList);

        newPanel.add(dropdownList);
        label.add(newPanel);
        number = new PrettyInput(this);
        newLabelOld();

        label.add(number);

        int i = 0;
        for (String string : varList) {
            traitNumVar.put(string, numList.get(i));
            i++;
        }
        this.revalidate();
    }

    // this constructor is called when traits are selected from the library

    public TraitBlock (BreedBlock breedBlock, Trait trait, HashMap<String, Variation> variationHashMap) {
        super(trait.getNameTrait(), Color.lightGray);
        flavors = new DataFlavor[]{
                DataFlavor.stringFlavor,
                traitBlockFlavor,
                CodeBlock.codeBlockFlavor};
        this.breedName = breedBlock.getName();
        this.traitName = trait.getNameTrait();
        //this.variationNamesValues = variationValue;   // string variation to its numeric value in NetLogo code (Aditi, Aug 7, 2012)
        //this.variationNumbers = variationNumber;     // variation to initial number of the variation in population (Aditi, Aug 7, 2012)
        //this.valueNumbers = valueNumber;
        //this.varList = variations;
        this.variationHashMap = variationHashMap;

        varColor = new HashMap<String, Color>();
        for (Map.Entry<String, Variation> variationEntry: variationHashMap.entrySet()) {
            String variation = variationEntry.getKey();
            varColor.put(variation, Color.lightGray);
        }


        colorButton.setPreferredSize(new Dimension(30, 30));
        //label.add(colorButton);

        //JPanel newPanel = new JPanel();
        //label.add(name);

        //dropdownList = new VariationDropDown(trait.getVariationsList(), this);
        //label.add(dropdownList);
        //newPanel.add(dropdownList);
        //label.add(newPanel);
        //number = new PrettyInput(this);
        //label.add(number);


        dropdownList = new VariationDropDown(trait.getVariationsList(), this);
        number = new PrettyInput(this);
        java.util.List<Component> componentList = new ArrayList<Component>(5);
        componentList.add(name); componentList.add(dropdownList); componentList.add(number);
        int y = 0;
        //label.add(new JLabel ("If"));

        for (Component c : componentList) {
          label.add(c);
          y += c.getPreferredSize().getHeight();
        }
        label.add(colorButton);

        label.setPreferredSize(new Dimension(100, y + 11));

        newLabel();

        this.revalidate();
    }

    public void makeNumberActive() {
        number.getDocument().addDocumentListener(new myDocumentListener());
    }

    protected class myDocumentListener implements DocumentListener {
    public void insertUpdate(DocumentEvent e) {
        updateNumber();
        //System.out.println("insert");
    }
    public void removeUpdate(DocumentEvent e) {
        //updateNumber();
        //System.out.println("remove");
    }
    public void changedUpdate(DocumentEvent e) {
        //displayEditInfo(e);
        System.out.println("change");
    }
    }

    public void setMyParent(CodeBlock block) {
        myParent = block;
    }

    public String getTraitName() {
        return traitName;
    }

    public String unPackAsCode() {
        if (myParent == null) {
            return unPackAsProcedure();
        }
        return unPackAsCommand();
    }

    public void numberAgents() {
        int i = 0;
        int accumulatedTotal = 0;
        int totalAgents = 0;
        int numberOfVariation;
        String tmp;

        tmp = ((BreedBlock) myParent).number.getText().toString();
        totalAgents = Integer.parseInt(tmp);


            for (Map.Entry<String, String> entry : varPercentage.entrySet()) {
                String variationType = entry.getKey();
                String numberType = entry.getValue();

                int k = Integer.parseInt(entry.getValue());

                if (i == (varPercentage.size() - 1)) {
                    numberOfVariation = (totalAgents - accumulatedTotal);

                }
                else {
                    numberOfVariation = (int) ( ( (float) k/100.0) * (float) totalAgents);
                }

                varNum.put(variationType, numberOfVariation);
                accumulatedTotal += numberOfVariation;
                i++;
            }

    }

    public void updateNumber() {
        String name = dropdownList.getSelectedItem().toString();
        Variation tmp = variationHashMap.get(name);
        tmp.number = Integer.parseInt(number.getText());
        variationHashMap.put(name, tmp);
    }

    public HashMap<String, Integer> getVarNum() {
        return varNum;
    }

    public String getMyTraitName() {
        String passback = "";

        passback += traitName + "\n ";

        return passback;
    }


    public String setupTrial() {
        String passBack = "";

        passBack += "let all-" + breedName + "-" + traitName + " sort " + breedName + " \n";

        int i = 0;
        int startValue = 0;
        int endValue = 0;


        for (Map.Entry<String, Variation> entry : variationHashMap.entrySet()) {
            String variationType = entry.getKey();
            Variation variation = entry.getValue();


            int k = variation.number;
            endValue = startValue + k;

            passBack += "let " + traitName + i + " sublist all-" + breedName + "-" + traitName + " " + startValue + " " + endValue + "\n";
            passBack += "foreach " + traitName + i + " [ ask ? [ set " + traitName + " \"" + variation.value + " \n";
            passBack += " set color " + variation.color + " ]] \n";

            i++;
            startValue = endValue;
    }
         return passBack;
    }


    public String setupNew() {
        String passBack = "";
        int i = 1;




        passBack += "let " + traitName + i + " sublist all-" + breedName + "-" + traitName +
                                " " + traitName + "-start " + number.getText().toString() + "\n";
        passBack += "foreach " + traitName + i + " [ ask ? [ set " + traitName + " \"" + dropdownList.getSelectedVariation()
                    + "\" \n";
        i++;

        if (colorButton.gotColor() == true) {
            passBack += "set color " + colorButton.getSelectedColorName() + "\n";
            }
        else if (colorButton.gotColor() == false) {
            passBack += "set color gray" + "\n";
            }

        passBack += "] ] \n";
        passBack += "set " + traitName + "-start " + number.getText().toString() + " \n";

        return passBack;
    }


    public String setup() {

        String passBack = "";

        int i = 0;
        //for (Map.Entry<String, String> entry : traitNumVar.entrySet()) {
          //  String variationType = entry.getKey();
            //String numberType = entry.getValue();


            //int startValue = 0;
            //int endValue = 0;

            //int k = Integer.parseInt(entry.getValue());


            //endValue = startValue + k;
            //passBack += "let " + traitName + i + " sublist big-list-" + traitName + " " + traitName + "_start " +
              //      number.getText().toString() + "\n";
        passBack += "let " + traitName + i + " sublist all-" + breedName + "-" + traitName +
                                " " + traitName + "-start " + number.getText().toString() + "\n";
        passBack += "foreach " + traitName + i + " [ ask ? [ set " + traitName + " \"" + dropdownList.getSelectedVariation()
                    + "\" \n";

            //passBack += "let " + traitName + "end " + traitName + "start + " + k;
            i++;

            if (colorButton.gotColor() == true) {
                passBack += "set color " + colorButton.getSelectedColorName() + "\n";

            }
            else if (colorButton.gotColor() == false) {
                passBack += "set color gray" + "\n";

            }
            passBack += "] ] \n";
            passBack += "set " + traitName + "-start " + number.getText().toString() + " \n";

      //  }


        /*
        passBack += "let big-list-" + traitName + " sort " + breedName + " \n";

        int i = 0;
        int startValue = 0;
        int endValue = 0;


        for (Map.Entry<String, String> entry : traitNumVar.entrySet()) {
            String variationType = entry.getKey();
            String numberType = entry.getValue();

            int k = Integer.parseInt(entry.getValue());

            endValue = startValue + k;

            passBack += "let " + traitName + i + " sublist big-list-" + traitName + " " + startValue + " " + endValue + "\n";
            passBack += "foreach " + traitName + i + " [ ask ? [ set " + traitName + " \"" + variationType + "\" ]] \n";

            i++;
            startValue = endValue;

            passBack += "ask " + breedName + "[ \n" + "if " + getName() + " = \"" + dropdownList.getSelectedItem().toString() + "\" ";

            passBack += "[\n";


        }
        */


        return passBack;
    }
     // this setup is not used anymore -A (Aug 8, 2012)
    public String setupOld() {
        String passBack = "";
        System.out.println("varNum " + this.getVarNum());
        //varNum = this.numberAgents();

        for (Map.Entry<String, Integer> entry: varNum.entrySet()) {
            String variation = entry.getKey();
            Integer number = entry.getValue();
            passBack += "ask n-of " + number + breedName + " [ \n";
            if (colorButton.gotColor() == true) {
                    passBack += "set color " + colorButton.getSelectedColorName() + "\n";
                }
                else if (colorButton.gotColor() == false) {
                    passBack += "set color gray" + "\n";
                }
            passBack += "set " + traitName + " = " + variation + "\n";


            passBack += "]";
        }

          return passBack;
    }



    public String unPackAsCommand() {
        String passBack = "";

        for (CodeBlock block : myBlocks) {

            passBack += block.unPackAsCode();
        }
        return passBack;
    }

    public String unPackAsProcedure() {
        String passBack = "";

        return passBack;
    }


    public ArrayList<String> getVariations() {
        return varList;
    }



    public void addBlock(CodeBlock block) {
        myBlocks.add(block);
        this.add(block);
        block.enableInputs();

        block.showRemoveButton();
        this.add(Box.createRigidArea(new Dimension(this.getWidth(), 4)));
        block.setMyParent(this);
        block.doLayout();

        block.validate();
        block.repaint();
        if (block instanceof BehaviorBlock) {
            ((BehaviorBlock) block).updateBehaviorInput();

        }
        if (block instanceof BehaviorBlock || block instanceof ConditionBlock) {
            String tmp = ((BehaviorBlock) block).getBehaviorInputName();
            addBehaviorInputToList(tmp);
            String s = ((BehaviorBlock) block).getAgentInputName();
            addAgentInputToList(s);
        }
        doLayout();
        validate();
        repaint();

        this.getParent().doLayout();
        this.getParent().validate();
        this.getParent().repaint();
    }



    public void newLabel() {
        for (Map.Entry<String, Color> entry : varColor.entrySet()) {
            String string = entry.getKey();
            if (dropdownList.getSelectedItem().toString().equals(string)) {
                setButtonColor(entry.getValue());
            }
        }

        for (Map.Entry<String, Variation> entry : variationHashMap.entrySet()) {
            String variation = entry.getKey();
            int num = entry.getValue().number;
            if (dropdownList.getSelectedItem().toString().equals(variation)) {
                number.setText(Integer.toString(num));
            }
        }
    }

    public void enableDropDown() {
        dropdownList.setEnabled(true);
    }





   // making label for the old constructor -A (Aug 8, 2012)
    public void newLabelOld() {
        for (Map.Entry<String, Color> entry : varColor.entrySet()) {
            String string = entry.getKey();
            if (dropdownList.getSelectedItem().toString().equals(string)) {
                setButtonColor(entry.getValue());
            }
        }

        for (Map.Entry<String, String> entry : varPercentage.entrySet()) {
            String variation = entry.getKey();
            if (dropdownList.getSelectedItem().toString().equals(variation)) {
                number.setText(entry.getValue());
            }
        }
    }


    public String selectedColor() {
        return colorButton.getSelectedColorName();
    }

    public void setButtonColor( Color color ) {
        colorButton.setBackground(color);
        colorButton.setOpaque(true);
        colorButton.setBorderPainted(false);
    }


    public void addVarColor() {
        String name = dropdownList.getSelectedItem().toString();
        varColor.put(name, colorButton.getSelectedColor());
        Variation tmp = variationHashMap.get(name);
        tmp.color = colorButton.getSelectedColorName();
        variationHashMap.put(name, tmp);
    }


    public void showColorButton() {
        colorButton.setVisible(true);
    }

    public VariationDropDown getDropdownList() {
        return dropdownList;
    }

    public HashMap<String, Variation> getVariationHashMap() {
        return variationHashMap;
    }

    public String getActiveVariation() {
        return dropdownList.getSelectedItem().toString();
    }

}


