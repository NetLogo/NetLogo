package org.nlogo.deltatick;

import org.nlogo.deltatick.dnd.VariationDropDown;
import org.nlogo.deltatick.xml.Trait;
import org.nlogo.deltatick.xml.Variation;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 3/16/13
 * Time: 5:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class TraitBlockNew
    extends CodeBlock
{
    JTextField textName;
    ArrayList<String> varList;
    LinkedList<Variation> variationList = new LinkedList<Variation>();
    String breedName;
    String traitName;
    JLabel name = new JLabel();

    transient Trait trait;
    transient TraitState traitState;
    transient Frame parentFrame;
    Variation variation;
    JList TraitsList;
    HashMap<String, String> varPercentage;
    HashMap<String, String> traitNumVar = new HashMap<String, String>();
    HashMap<String, Integer> varNum = new HashMap<String, Integer>();

    HashMap<String, Variation> variationHashMap = new HashMap<String, Variation>();
    HashMap<String, String> variationNamesValues = new HashMap<String, String>();

    public TraitBlockNew (BreedBlock breedBlock, TraitState traitState, HashMap<String, Variation> variationHashMap, HashMap<String, String> variationValues ) {
        super (traitState.getNameTrait(), Color.lightGray);
        flavors = new DataFlavor[]{
                DataFlavor.stringFlavor,
                traitBlockFlavor,
                CodeBlock.codeBlockFlavor};
        this.breedName = breedBlock.plural();
        this.traitState = new TraitState(traitState);
        this.traitName = this.traitState.getNameTrait();
        //this.variationHashMap = variationHashMap;
        this.variationHashMap.clear();
        this.variationHashMap.putAll(variationHashMap);
        //this.variationNamesValues = variationValues;

        java.util.List<Component> componentList = new ArrayList<Component>();
        name.setText(" of " + breedBlock.plural());
        componentList.add(name);

        //int y = 0;
        for (Component c : componentList) {
          label.add(c);
          //y += c.getPreferredSize().getHeight();
        }
        //label.setPreferredSize(new Dimension(100, y + 11));
        this.setPreferredSize(getPreferredSize());
        this.setMaximumSize(getPreferredSize());
        this.revalidate();
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

    public HashMap<String, Integer> getVarNum() {
        return varNum;
    }

    public String getMyTraitName() {
        String passback = "";
        passback += traitName + "\n ";
        return passback;
    }

    public BreedBlock getMyParent() {
        return ((BreedBlock) myParent);
    }


    public String unPackAsCommand() {
        String passBack = "";
        //String value = variationNamesValues.get(variation);

        //passBack += "if " + this.getMyParent().plural() + "-" + this.getTraitName() + " = " + value + " [\n";
//        for (CodeBlock block : myBlocks) {
//            passBack += block.unPackAsCode();
//        }
        //passBack += "] \n";
        return passBack;
    }

    public String unPackAsProcedure() {
        String passBack = "";
        return passBack;
    }

    public ArrayList<String> getVariations() {
        return varList;
    }

    public HashMap<String, Variation> getVariationHashMap() {
        return variationHashMap;
    }

    public String getBreedName() {
        //return breedName;
        return ((BreedBlock) myParent).plural();
    }

    public void hideRemoveButton() {
        //this.remove(removeButton);
        removeButton.setVisible(false);
    }

    public String getMutateCode() {
        return traitState.getMutateCode();
    }

}
