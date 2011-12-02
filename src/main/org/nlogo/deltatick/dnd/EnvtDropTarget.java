package org.nlogo.deltatick.dnd;

import org.nlogo.app.DeltaTickTab;
import org.nlogo.deltatick.*;

import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: aditi
 * Date: 9/8/11
 * Time: 5:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class EnvtDropTarget
        extends DropTarget {

    DeltaTickTab deltaTickTab;

    public EnvtDropTarget(EnvtBlock block, DeltaTickTab deltaTickTab) {
        super ( block );
        this.deltaTickTab = deltaTickTab;
    }

    protected boolean dropComponent(Transferable transferable)
            throws IOException, UnsupportedFlavorException {
        Object o = transferable.getTransferData( CodeBlock.codeBlockFlavor );
        if (o instanceof Component) {
            if( o instanceof PatchBlock) {
                addCodeBlock( (PatchBlock) o );
                //deltaTickTab.addCondition( (ConditionBlock) o );
                return true;
            }
            if( o instanceof ConditionBlock) {
                addCodeBlock( (ConditionBlock) o );
                deltaTickTab.addCondition( (ConditionBlock) o );
                return true;
        }
        //return false;
    }
        return false;
}
}


