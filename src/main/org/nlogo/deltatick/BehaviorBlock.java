package org.nlogo.deltatick;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.util.HashMap;
import java.util.Map;

import org.nlogo.deltatick.dnd.PrettyInput;
import org.nlogo.deltatick.xml.Variation;

import javax.swing.*;

// strictfp: When applied to a class, all calculations inside the class use strict floating-point math.-a.

public strictfp class BehaviorBlock
        extends CodeBlock {

    boolean isTrait;
    boolean isMutate;
    TraitBlockNew tBlockNew = null; // TODO need this to have trait Block work as an input in code (March, 25, 2013)
    CodeBlock container = null;
    BreedBlock myBreedBlock = null;
    private JToolTip toolTip;

    public BehaviorBlock(String name) {
        super(name, ColorSchemer.getColor(0).brighter());
        flavors = new DataFlavor[]{
                DataFlavor.stringFlavor,
                CodeBlock.behaviorBlockFlavor,
                CodeBlock.codeBlockFlavor,
        };

    }


    //codeBlockFlavor in Condition Block, Beh Block is what makes it a valid block for Breed   -A.

    public String unPackAsCode() {
        if (myParent == null) {
            return unPackAsProcedure();
        }
        return unPackAsCommand();
    }
      //TODO: Box bracket appears when it need not (March 9)
    //extracting the argument to be passed after name of procedure (March 2)
      public String unPackAsProcedure() {
          String passBack = "";
          passBack += "to " + getName() + " ";

          if ((agentInputs.size() > 0) || (inputs.size() > 0) || (behaviorInputs.size() > 0 || percentInputs.size() > 0) || (isTrait == true) ) {
              passBack += "[ ";
          }
          if (inputs.size() > 0) {
              for (String input : inputs.keySet()) {
                  passBack += input + " ";
              }
          }
          if (agentInputs.size() > 0) {
              for (String s : agentInputs.keySet()) {
                  passBack += s + " ";
              }
          }
          if (behaviorInputs.size() > 0) {
              for (String s : behaviorInputs.keySet()) {
                  passBack += s + " ";
              }
          }
          if (percentInputs.size() > 0) {
              for (String s : percentInputs.keySet()) {
                  passBack += s + " ";
              }
          }
          //when traitname appears as an input, it clashes with the trait defined as a breed-variable so using behaviorinput
          //instead (March 29, 2013)
//          if (isTrait == true) {
//              passBack += tBlockNew.getTraitName() + " ";
//          }
          if ((agentInputs.size()  > 0) || (inputs.size() > 0) || (behaviorInputs.size() > 0 || (percentInputs.size() > 0)) || (isTrait == true) ) {
              passBack += " ]";
          }

          if ( ifCode != null ) {
              passBack += "\n" + ifCode + "[\n" + code + "\n" + "]";
          }
          else {
              passBack += "\n" + code + "\n";
              if (isMutate == true) {
                  if (myBreedBlock != null) {
                      for (TraitBlockNew traitBlock :  myBreedBlock.getMyTraitBlocks()) {
                          String traitName = traitBlock.getTraitName();
                          //TODO: this is likely to throw a bug if reproduce is in condition block (April 1, 2013)
                          //passBack += "if random 100 <= " + this.getMyBreedBlock().plural() + "-" + traitBlock.getTraitName() + "-mutation [\n";
                          //passBack += "set " + traitName + " " + traitName + " * 0.01 \n]]]\n";
                          passBack += "ifelse random 2 = 0 \n";
                          passBack += "[set " + traitName + " (" + traitName + " - " + this.getMyBreedBlock().plural() + "-" +
                                                        traitBlock.getTraitName() + "-mutation)]\n";
                          passBack += "[set " + traitName + " (" + traitName + " + " + this.getMyBreedBlock().plural() + "-" +
                                                        traitBlock.getTraitName() + "-mutation)]";
                          passBack += "\n]]\n";
                      }
                      //If no trait or mutate code is being used, close brackets for reproduce block
                      if (myBreedBlock.getMyTraitBlocks().size() == 0) {
                          passBack += "]]\n";
                      }
                  }
              }
          }
          if (energyInputs.size() > 0) {
              for (JTextField inputName : energyInputs.values()) {
                  passBack += "set energy energy " + inputName.getText() + "\n";
              }
          }
          passBack += " end\n\n";

          return passBack;
      }

    // extracting name of behavior into "to go" -A. (sept 24)
    public String unPackAsCommand() {
        String passBack = "";

        passBack += " " + getName() + " ";
        for (JTextField input : inputs.values()) {
            passBack += input.getText() + " ";
        }

        for (JTextField agentInput : agentInputs.values()) {
            passBack += agentInput.getText() + " ";
        }
        if (isTrait) {
            passBack += tBlockNew.getTraitName();
        }
        else {
            for (JTextField behaviorInput : behaviorInputs.values()) {
                passBack += behaviorInput.getText() + " ";
            }
        }
        for (JTextField percentInput : percentInputs.values()) {
            passBack += percentInput.getText() + " ";
        }

        passBack += "\n";

        return passBack;
    }

    public void die() {
        super.die();
        if (isMutate) {
            isMutate = false;
            myBreedBlock.setReproduceUsed(false);
        }
    }

    public void updateBehaviorInput() {
        Container parent = getParent();
        if (parent instanceof TraitBlock) {
            HashMap<String, Variation> hashMap = ((TraitBlock) parent).variationHashMap;
            String selectedVariationName = ((TraitBlock) parent).getDropdownList().getSelectedItem().toString();
            String trait = ((TraitBlock) parent).getName();
            Variation tmp = hashMap.get(selectedVariationName);

            String value = tmp.value;
            for ( String s : behaviorInputs.keySet()) {
                if (s.equals(trait)) {
                    JTextField textField = behaviorInputs.get(s);
                    textField.setText(value);
                }
            }
        }
    }

    //remove behaviorInput from block if a TraitBlock has been added -(March 25, 2013)
    //assumption that there's only one behaviorInput per block
    public void removeBehaviorInput() {
        for ( Map.Entry<String, PrettyInput> map : behaviorInputs.entrySet()) {
            String s = map.getKey();
            PrettyInput j = map.getValue();
            //remove(j); // TODO: prefer that it is removed entirely because it can't be used alone again (March 25, 2013)
            j.setVisible(false);
            revalidate();
            repaint();
            //behaviorInputs.remove(s);  // need the behavior input to generate code esp when trait blocks are used (March 29,2013)
        }
    }

    // will work only for one behaviorInput per block -A. (Aug 10, 2012)
    public String getBehaviorInputName() {
        String behaviorInputName = new String();
        for ( String s : behaviorInputs.keySet()) {
            behaviorInputName = s;
            }
        return behaviorInputName;
    }

    public String getAgentInputName() {
        String agentInputName = new String();
        for ( String s : agentInputs.keySet()) {
            agentInputName = s;
            }
        return agentInputName;
    }

    public String getPercentInputName() {
        String percentInputName = new String();
        for ( String s : percentInputs.keySet()) {
            percentInputName = s;
            }
        return percentInputName;
    }

    public boolean getIsTrait() {
        return isTrait;
    }

    public void setIsTrait(boolean value) {
        isTrait = value;
    }

    public void setTrait(TraitBlockNew traitBlockNew) {
        tBlockNew = traitBlockNew;
    }

    // check if I'm a reproduce block -(March 25, 2013)
    public void setIsMutate(boolean value) {
        isMutate = value;
    }

    public boolean getIsMutate() {
        return isMutate;
    }

    //container to access the BreedBlock in which I'm dropped - March 26, 2013
    public void setMyBreedBlock(BreedBlock breedBlock) {
        myBreedBlock = breedBlock;
    }

    public BreedBlock getMyBreedBlock() {
        return myBreedBlock;
    }

    public JPanel getLabel() {
        return label;
    }

    public JToolTip createToolTip() {
        toolTip = super.createToolTip();
        toolTip.setBackground(Color.white);
        toolTip.setForeground(Color.black);
        return toolTip;
    }


}
