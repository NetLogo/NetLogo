package org.nlogo.deltatick.dnd;

import org.nlogo.deltatick.*;

import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 2/10/12
 * Time: 7:58 PM
 * To change this template use File | Settings | File Templates.
 */


public class TraitDropTarget
        extends DropTarget {

    public TraitDropTarget(TraitBlock tBlock) {
        super(tBlock);
    }

    protected boolean dropComponent(Transferable transferable)
            throws IOException, UnsupportedFlavorException {
        Object o = transferable.getTransferData(CodeBlock.codeBlockFlavor);

        if (o instanceof Component) {
            if (o instanceof BehaviorBlock) {
                addCodeBlock((BehaviorBlock) o);
                return true;
            }
            if (o instanceof ConditionBlock) {
                addCodeBlock((ConditionBlock) o);
                new ConditionDropTarget((ConditionBlock) o);
                return true;
            }
            //return false; - commented out by A. (nov 27)


        }
        return false;
        //not sure about return line of code above -A. (Feb 14, 2012)
        // TODO: Figure out if line of code above is correct
    }
}


