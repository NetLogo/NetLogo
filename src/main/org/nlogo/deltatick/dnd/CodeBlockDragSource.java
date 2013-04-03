package org.nlogo.deltatick.dnd;

import ch.randelshofer.quaqua.QuaquaComboPopup;
import org.nlogo.deltatick.BehaviorBlock;
import org.nlogo.deltatick.CodeBlock;
import org.nlogo.deltatick.TraitBlock;

import javax.swing.*;
import java.awt.dnd.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwilkerson
 * Date: Feb 22, 2010
 * Time: 7:48:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class CodeBlockDragSource implements DragGestureListener,
        DragSourceListener {

    public CodeBlock block;

    public CodeBlockDragSource(CodeBlock block) {
        DragSource dragSource = DragSource.getDefaultDragSource();
        this.block = block;
        // DragSource is entity responsible for the initiation of the Drag and Drop operation -A. (sept 8)
        // Create a DragGestureRecognizer and
        // register as the listener
        // TODO: ACTION_COPY_OR_MOVE
        dragSource.createDefaultDragGestureRecognizer(block, DnDConstants.ACTION_COPY, this);
    }

    // Implementation of DragGestureListener interface.
    public void dragGestureRecognized(DragGestureEvent dge) {
        try {
            dge.startDrag(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.MOVE_CURSOR), block);
        }
        catch(Exception e) {
        }
    }

    // Implementation of DragSourceListener interface
    public void dragEnter(DragSourceDragEvent dsde) {

    }

    public void dragOver(DragSourceDragEvent dsde) {
    }

    public void dragExit(DragSourceEvent dse) {
    }

    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    public void dragDropEnd(DragSourceDropEvent dsde) {
    }

}