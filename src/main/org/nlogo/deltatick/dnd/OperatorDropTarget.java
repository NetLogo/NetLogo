package org.nlogo.deltatick.dnd;

import org.nlogo.deltatick.BehaviorBlock;
import org.nlogo.deltatick.CodeBlock;
import org.nlogo.deltatick.ConditionBlock;
import org.nlogo.deltatick.PatchBlock;
import org.nlogo.deltatick.OperatorBlock;

import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 4/21/12
 * Time: 11:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class OperatorDropTarget
extends DropTarget {

    public OperatorDropTarget(OperatorBlock oBlock) {
        super(oBlock);
    }

    protected boolean dropComponent(Transferable transferable)
            throws IOException, UnsupportedFlavorException {
        Object o = transferable.getTransferData(CodeBlock.codeBlockFlavor);
        if (o instanceof Component) {
            if (o instanceof BehaviorBlock) {
                addCodeBlock((BehaviorBlock) o);
                return true;
            }

            //return false; - commented out by A. (nov 27)
        }
        return false;
    }
}
