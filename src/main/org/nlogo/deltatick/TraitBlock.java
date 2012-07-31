package org.nlogo.deltatick;

import ch.randelshofer.quaqua.QuaquaComboPopup;
import com.sun.servicetag.SystemEnvironment;
import com.sun.tools.corba.se.idl.StringGen;
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
    //= new HashMap<String, Integer>();
    PrettyInput number;
    //JButton pickColorButton;
    String color;
    ColorButton colorButton = new ColorButton(parentFrame, this);
    VariationDropDown dropdownList;


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

         colorButton.setSize(4, 2);
        label.add(colorButton);
        //colorButton.setVisible(false);

        JPanel newPanel = new JPanel();
        name.setText(breedName);
        label.add(name);

        //dropdownList = new JComboBox(varList.toArray());
        //dropdownList.revalidate();
        dropdownList = new VariationDropDown(varList, this);
        label.add(dropdownList);
       // dropdownList.requestFocus(false);
       // dropdownList.setTransferHandler(this.getTransferHandler());


        //boolean fixit = dropdownList.getUI().isPopupVisible(dropdownList);
        //fixit = false;
       // JComboBox comboBox = ((TraitBlock) this).getDropDownList();
          //  QuaquaComboPopup popup = (QuaquaComboPopup) comboBox.getAccessibleContext().getAccessibleChild(0);
           //popup.requestFocus(false);
            //popup.setBorder(null);
        //popup.setVisible(false);
        //dropdownList.getAccessibleContext().getAccessibleChild(0).
        //dropdownList.addActionListener(this);
        newPanel.add(dropdownList);
        label.add(newPanel);
        number = new PrettyInput(this);
        newLabel();

        label.add(number);

        int i = 0;
        for (String string : varList) {
            traitNumVar.put(string, numList.get(i));
            i++;
        }



    }

    public TraitBlock ( Trait trait ) {
        super(trait.nameTrait(), ColorSchemer.getColor(3));
        this.traitName = trait.nameTrait();
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


    //public HashMap<String, Integer> numberAgents() {
    public void numberAgents() {
        int i = 0;
        int accumulatedTotal = 0;
        int totalAgents = 0;
        int numberOfVariation;
        String tmp;
        //if (myParent instanceof BreedBlock) {
        System.out.println("debugging " + ((BreedBlock) myParent).number.getText().toString());
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
        System.out.println("hashmap " + varNum);
       // return varNum;

    }

    public HashMap<String, Integer> getVarNum() {
        return varNum;
    }


    public String setup() {
        String passBack = "";
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

        }
        passBack += "ask " + breedName + "[ \n" + "if " + getName() + " = \"" + dropdownList.getSelectedItem().toString() + "\" ";

                passBack += "[\n";

                if (colorButton.gotColor() == true) {
                    passBack += "set color " + colorButton.getSelectedColorName() + "\n";
                }
                else if (colorButton.gotColor() == false) {
                    passBack += "set color gray" + "\n";
                }
        passBack += "]] \n";
        System.out.println("traitnumVar " + traitNumVar);
        System.out.println("varPercentage " + varPercentage);

          return passBack;
    }

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

    public String breedVars() {
        String passback = "";

        passback += traitName + "\n ";

        return passback;
    }

    public String unPackAsCommand() {
        String passBack = "";
        passBack += "if " + getName() + " = \"" + dropdownList.getSelectedItem().toString() + "\" ";

        passBack += "[\n";



        if (colorButton.gotColor() == true) {
            passBack += "set color " + colorButton.getSelectedColorName() + "\n";
            System.out.println("not null");
        }
        else if (colorButton.gotColor() == false) {
            passBack += "set color gray" + "\n";
            System.out.println("null");
        }

        for (CodeBlock block : myBlocks) {
            passBack += block.unPackAsCode();
        }
        passBack += "]\n";


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

        super.addBlock(block);


    }

    /*
    public VariationDropDown getDropDownList() {
        return ;
    }
    */
    /*



    public JButton makePickColor() {
        pickColorButton = new JButton();
        //new ShapeIcon(org.nlogo.shape.VectorShape.getDefaultShape()));
        //pickColorButton.setActionCommand(this.getName());
        pickColorButton.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //ShapeSelector myShapeSelector = new ShapeSelector(parentFrame, allShapes(), this);
                ColorDialog colorDialog = new ColorDialog(parentFrame, true);
                colorDialog.setVisible(true);
                System.out.println("ColorDialog");
        //breedShapeButton.setIcon(new ShapeIcon(myShapeSelector.getShape()));
        //breedShape = myShapeSelector.getChosenShape();
        color = colorDialog.getSelectedColorName();

    }

        });
        pickColorButton.setSize(10, 10);
        return pickColorButton;
    }
    */


    // when clicks on shape selection -a.

      /*
    public void dropdownListEvent() {
        dropdownList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                updateLabel();
    }
        });
    }
      */

    public void newLabel() {
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



    public HashMap addVarColorName() {
        for (String string : varList) {
            if (string.equals(dropdownList.getSelectedItem().toString())) {
                varColorName.put(string, colorButton.getSelectedColorName());
            }
        }
        return varColorName;
    }

    public HashMap addVarColor() {
        for (String string : varList) {
            if (string.equals(dropdownList.getSelectedItem().toString())) {
                varColor.put(string, colorButton.getSelectedColor());
            }
        }

        return varColor;
    }

    public void showColorButton() {
        colorButton.setVisible(true);
    }


}


