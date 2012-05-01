package org.nlogo.deltatick;

import org.nlogo.deltatick.dialogs.OperatorBlockBuilder;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 3/10/12
 * Time: 4:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class OperatorBlock
    extends CodeBlock {
    ArrayList<String> variationList;
    ArrayList<String> variationList2;
    String andOr;

    JLabel name;
    OperatorBlockBuilder obBuilder;
    UserInput userInput;
    JComboBox dropdownList;
    JComboBox dropdownList2;
    String traitA;
    String traitB;
    String selectedBreed;


    public OperatorBlock ( String breed, String trait1, String trait2, ArrayList<String> list1,
                           ArrayList<String> list2 ) {
        super("OB", ColorSchemer.getColor(1));
        this.selectedBreed = breed;
        //this.andOr = operator;
        this.traitA = trait1;
        this.traitB = trait2;
        this.variationList = list1;
        this.variationList2 = list2;

        System.out.println(list1.toString() + " " + list2.toString());

        JPanel newPanel = new JPanel();
        //label.add(name);
        dropdownList = new JComboBox(variationList.toArray());
        dropdownList.revalidate();
        newPanel.add(dropdownList);
        label.add(dropdownList);

        dropdownList2 = new JComboBox(variationList2.toArray());
        dropdownList2.revalidate();
        newPanel.add(dropdownList2);
        label.add(dropdownList2);

    }

    /*



    public OperatorBlock ( OperatorBlockBuilder builder, UserInput uInput ) {
        super("OB", ColorSchemer.getColor(1));
        //this.obBuilder = builder;
        this.userInput = uInput;

        this.traitA = obBuilder.selectedTrait();
        this.traitB = obBuilder.selectedTrait2();

        variationList = userInput.getVariations( obBuilder.selectedBreed(), traitA );
        variationList2 = userInput.getVariations( obBuilder.selectedBreed(), traitB );

        JPanel newPanel = new JPanel();
        //label.add(name);
        dropdownList = new JComboBox(variationList.toArray());
        dropdownList.revalidate();
        newPanel.add(dropdownList);
        label.add(dropdownList);

        dropdownList2 = new JComboBox(variationList2.toArray());
        dropdownList2.revalidate();
        newPanel.add(dropdownList2);
        label.add(dropdownList2);

    }
    */
    /*
    public OperatorBlock ( String breed, String trait1, String trait2, UserInput uInput ) {
        super("OB", ColorSchemer.getColor(1));
        //this.obBuilder = builder;
        this.userInput = uInput;



        variationList = userInput.getVariations( breed, trait1 );
        variationList2 = userInput.getVariations( breed, trait2 );

        JPanel newPanel = new JPanel();
        //label.add(name);
        dropdownList = new JComboBox(variationList.toArray());
        dropdownList.revalidate();
        newPanel.add(dropdownList);
        label.add(dropdownList);

        dropdownList2 = new JComboBox(variationList2.toArray());
        dropdownList2.revalidate();
        newPanel.add(dropdownList2);
        label.add(dropdownList2);

    }
    */

    public String unPackAsCode() {
        if (myParent != null) {
            return unPackAsCommand();
        }
        return unPackAsProcedure();
    }


    // this shows up under "to go" (Feb 15, 2012)
    public String unPackAsCommand() {
        String passBack = "";

        passBack += "if " + traitA + " = \"" + dropdownList.getSelectedItem().toString() + "\" and " + traitB +
                " = \"" + dropdownList.getSelectedItem().toString() + "\"";
        passBack += " [\n";
        for (CodeBlock block : myBlocks) {
            passBack += block.unPackAsCode();
        }
        passBack += "]\n";

        return passBack;
    }


    //this shows up as a separate procedure (Feb 15, 2012)
    public String unPackAsProcedure() {
        String passBack = "";

        return passBack;
    }

    public void addBlock(CodeBlock block) {
        super.addBlock(block);
        //this.validate();
    }


}
