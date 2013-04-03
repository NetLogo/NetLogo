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

    BehaviorBlock behBlock;

    public BehaviorDropTarget(BehaviorBlock bBlock) {
        super(bBlock);
        this.behBlock = bBlock;
    }

    protected boolean dropComponent(Transferable transferable)
            throws IOException, UnsupportedFlavorException {
        Object o = transferable.getTransferData(CodeBlock.codeBlockFlavor);
        if (o instanceof Component) {
            if (o instanceof TraitBlockNew) {
                addCodeBlock((TraitBlockNew) o);
                ((TraitBlockNew) o).setMyParent(behBlock.getMyBreedBlock());
                ((TraitBlockNew) o).hideRemoveButton();
                behBlock.setIsTrait(true);
                behBlock.removeBehaviorInput(); // assuming only one behaviorInput so will correspond to trait (March 25, 2013)
                behBlock.setTrait((TraitBlockNew) o);
                behBlock.getMyBreedBlock().addBlock((TraitBlockNew) o);// so BreedBlock knows it has a traitBlock in one of its behBlocks (March 25, 2013)
                return true;
            }
        }
        return false;
    }
}

