package org.nlogo.deltatick.dnd;

import org.nlogo.app.DeltaTickTab;
import org.nlogo.deltatick.*;

import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class BreedDropTarget
        extends DropTarget {

    DeltaTickTab deltaTickTab;

    public BreedDropTarget(BreedBlock block, DeltaTickTab deltaTickTab) {
        super(block);
        this.deltaTickTab = deltaTickTab;
        boolean breedwithTrait;
    }

    protected boolean dropComponent(Transferable transferable)
            throws IOException, UnsupportedFlavorException {
        Object o = transferable.getTransferData(CodeBlock.codeBlockFlavor);
        if (o instanceof Component) {
            if (o instanceof ConditionBlock) {
                addCodeBlock((ConditionBlock) o);
                deltaTickTab.addCondition((ConditionBlock) o);
                return true;
            } else if (o instanceof BehaviorBlock) {
                addCodeBlock((BehaviorBlock) o);
                return true;
            } else if (o instanceof TraitBlock) {
                addCodeBlock((TraitBlock) o);
                deltaTickTab.addTrait((TraitBlock) o);
                return true;
            } else if (o instanceof OperatorBlock) {
                addCodeBlock((OperatorBlock) o);
                deltaTickTab.addOperator((OperatorBlock) o);
                return true;
            }


            //commented this out because I don't need patchBlocks to be dropped into breedBlocks -A. (Nov 27)
            //else if( o instanceof PatchBlock ) {
            //  addCodeBlock( (PatchBlock) o);
            //return true;
            // }
        }
        return false;
    }
}