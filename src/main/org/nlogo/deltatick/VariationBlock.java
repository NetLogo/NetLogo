package org.nlogo.deltatick;

import org.nlogo.deltatick.dnd.PrettyInput;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 3/16/13
 * Time: 6:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class VariationBlock
    extends CodeBlock
{
    String variationName = new String();
    String traitName = new String();
    BehaviorBlock myParent;

    public VariationBlock (String traitName, String myName, BehaviorBlock bBlock) {
        super(myName, ColorSchemer.getColor(4));
        this.variationName = myName;
        this.traitName = traitName;
        this.myParent = bBlock;
        //this.setMaximumSize(new Dimension(50, 10));
        updateLabel();
    }

    public void updateLabel() {
        if (myParent.getBehaviorInputs().size() > 0) {
            for (Map.Entry<String, PrettyInput> entry: myParent.getBehaviorInputs().entrySet()) {
                this.addBehaviorInput(entry.getKey(), entry.getValue().getText(), entry.getValue().getToolTipText());
            }
        }
        if (myParent.getAgentInputs().size() > 0) {
            for (Map.Entry<String, PrettyInput> entry: myParent.getAgentInputs().entrySet()) {
                this.addAgentInput(entry.getKey(), entry.getValue().getText());
            }
        }
        if (myParent.getInputs().size() > 0) {
            for (Map.Entry<String, PrettyInput> entry : myParent.getInputs().entrySet()) {
                this.addInput(entry.getKey(), entry.getValue().getText());
            }
        }
    }
}
