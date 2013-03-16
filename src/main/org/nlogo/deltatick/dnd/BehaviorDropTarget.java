package org.nlogo.deltatick.dnd;

import org.nlogo.deltatick.*;

import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 3/16/13
 * Time: 6:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class BehaviorDropTarget
        extends DropTarget {

    public BehaviorDropTarget(BehaviorBlock bBlock) {
        super(bBlock);
    }

    protected boolean dropComponent(Transferable transferable)
            throws IOException, UnsupportedFlavorException {
        Object o = transferable.getTransferData(CodeBlock.codeBlockFlavor);
        if (o instanceof Component) {
            if (o instanceof TraitBlockNew) {
                for (String name : ((TraitBlockNew) o).getVariationHashMap().keySet()) {
                    VariationBlock vBlock = new VariationBlock(((TraitBlockNew) o).getTraitName(), name);
                    addCodeBlock((VariationBlock) vBlock);
                }

                return true;
            }
        }
        return false;
    }
}

