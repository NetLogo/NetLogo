package org.nlogo.deltatick.dnd;

import org.nlogo.api.Patch;
import org.nlogo.deltatick.BehaviorBlock;
import org.nlogo.deltatick.CodeBlock;
import org.nlogo.deltatick.ConditionBlock;
import org.nlogo.deltatick.PatchBlock;

import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class ConditionDropTarget
        extends DropTarget {

    public ConditionDropTarget(ConditionBlock cBlock) {
        super(cBlock);
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
            if (o instanceof PatchBlock) {
                addCodeBlock((PatchBlock) o);
                //new ConditionDropTarget((PatchBlock) o);
                return true;
            }
            //return false; - commented out by A. (nov 27)
        }
        return false;
    }
}