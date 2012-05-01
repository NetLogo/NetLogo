package org.nlogo.deltatick;

import org.nlogo.deltatick.dnd.PrettyInput;
import org.nlogo.deltatick.xml.Breed;
import org.nlogo.deltatick.xml.ModelBackgroundInfo;
import org.nlogo.deltatick.xml.Trait;
import org.nlogo.deltatick.xml.Variation;
import org.parboiled.support.Var;

import javax.swing.*;
import java.awt.*;
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
    LinkedList<Variation> variationList = new LinkedList<Variation>();
    String breedName;
    String traitName;
    JLabel name = new JLabel();
    JComboBox dropdownList;
    Trait trait;
    Variation variation;
    JList TraitsList;
    HashMap<String, String> numVar;
    PrettyInput number;

    // this traitBlock constructor is for when students enter trait & variation themselves (Feb 20, 2012)
    public TraitBlock(String breed, String trait, ArrayList<String> variationList, HashMap<String, String> hashMap) {
        super(trait, ColorSchemer.getColor(1));
        flavors = new DataFlavor[]{
                DataFlavor.stringFlavor,
                traitBlockFlavor,
                CodeBlock.codeBlockFlavor};
        this.varList = variationList;
        this.breedName = breed;
        this.traitName = trait;
        this.numVar = hashMap;


        JPanel newPanel = new JPanel();
        name.setText(breedName);
        label.add(name);
        dropdownList = new JComboBox(varList.toArray());
        dropdownList.revalidate();
        newPanel.add(dropdownList);
        label.add(dropdownList);
        number = new PrettyInput(this);
        for (Map.Entry<String, String> entry : numVar.entrySet()) {
            String variationType = entry.getKey();
            String numberType = entry.getValue();
            if (dropdownList.getSelectedItem().toString().equals(variationType)) {
                number.setText(numberType);
            }
        }
        label.add(number);
    }

    /*
    // experimenting with different constructors to see how to make trait library work, and whether or not to
    // give users a chance to construct their own variations (Feb 20, 2012)
    public TraitBlock(Trait trait, ArrayList<String> variationList) {
        super(trait.nameTrait(), ColorSchemer.getColor(1));
        flavors = new DataFlavor[]{
                DataFlavor.stringFlavor,
                traitBlockFlavor,
                CodeBlock.codeBlockFlavor};
        this.varList = variationList;
        this.trait = trait;

        JPanel newPanel = new JPanel();
        dropdownList = new JComboBox(varList.toArray());
        dropdownList.revalidate();
        newPanel.add(dropdownList);
        // name.add(newPanel);
        label.add(dropdownList);
    }


    // this constructor is called when you pick a trait block from library  (Feb 20, 2012)
    public TraitBlock(String traitName) {
        super(traitName, ColorSchemer.getColor(1));
        flavors = new DataFlavor[]{
                DataFlavor.stringFlavor,
                traitBlockFlavor,
                CodeBlock.codeBlockFlavor};
        this.traitName = traitName;

    }
    */
    /* Trait not taken from XML files for now
    public TraitBlock(Node traitNode) {
        super(traitNode.getAttributes().getNamedItem("name").getTextContent(), ColorSchemer.getColor(1));
        traitName = traitNode.getAttributes().getNamedItem("name").getTextContent();

        NodeList traitNodes = traitNode.getChildNodes();

        for (int i = 0; i < traitNodes.getLength(); i++) {

            if (traitNodes.item(i).getNodeName() == "variations") {
                    variationList.add(variation);
                }}

                JPanel newPanel = new JPanel();
                dropdownList = new JComboBox(variationList.toArray());
                dropdownList.revalidate();
                newPanel.add(dropdownList);
                label.add(dropdownList);
            }
            */

    public void setMyParent(CodeBlock block) {
        myParent = block;
    }

    public void makeLabel() {
        label.add(removeButton);
        JLabel name = new JLabel (getName() + " of");
        label.add(name);
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

    public String setup() {
        String passBack = "";

        passBack += "let big-list-" + traitName + " sort bugs \n";

        int i = 0;
        int startValue = 0;
        int endValue = 0;

        for (Map.Entry<String, String> entry : numVar.entrySet()) {
            String variationType = entry.getKey();
            String numberType = entry.getValue();
            int k = Integer.parseInt(entry.getValue());

            endValue = startValue + k;

            passBack += "let l" + i + " sublist big-list-" + traitName + " " + startValue + " " + endValue + "\n";
            passBack += "foreach l" + i + " [ ask ? [ set vision \"" + variationType + "\" ]] \n";

            i++;
            startValue = endValue;

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

        /*
            for( JTextField input : inputs.values() ) {
            passBack += input.getText() + " ";
        }
        */
        passBack += "[\n";
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
     /*
    // extracting name of behavior into "to go" inside ask breed [ ] -A. (sept 24)
    public String unPackAsCommand() {
        String passBack = "";

        passBack += " " + getName() + " ";
        for( JTextField input : inputs.values() ) {
            passBack += input.getText() + " ";
        }
        passBack += "\n";

        return passBack;
    }
    */

    public ArrayList<String> getVariations() {
        return varList;
    }

    public void addBlock(CodeBlock block) {
        super.addBlock(block);
    }


}
