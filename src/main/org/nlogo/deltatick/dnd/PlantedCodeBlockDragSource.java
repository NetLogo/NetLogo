package org.nlogo.deltatick.dnd;

import org.nlogo.deltatick.CodeBlock;

import java.awt.dnd.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwilkerson
 * Date: Mar 12, 2010
 * Time: 10:47:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlantedCodeBlockDragSource implements DragGestureListener,
        DragSourceListener {

    CodeBlock block;

    public PlantedCodeBlockDragSource(CodeBlock block) {
        DragSource dragSource = DragSource.getDefaultDragSource();
        this.block = block;

        // Create a DragGestureRecognizer and
        // register as the listener
        // TODO: ACTION_COPY_OR_MOVE
        dragSource.createDefaultDragGestureRecognizer(block, DnDConstants.ACTION_COPY, this);
    }

    // Implementation of DragGestureListener interface.
    public void dragGestureRecognized(DragGestureEvent dge) {
        dge.startDrag(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.MOVE_CURSOR), block);
        // wherever it's dropped, it'll either get copied if needed or need to go away, so this
        // component always dies.
        //block.die();
    }

    // Implementation of DragSourceListener interface
    public void dragEnter(DragSourceDragEvent dsde) {
        System.out.println("dragEnter");
    }

    public void dragOver(DragSourceDragEvent dsde) {
        System.out.println("dragOver");
    }

    public void dragExit(DragSourceEvent dse) {
        System.out.println("dragExit");
    }

    public void dropActionChanged(DragSourceDragEvent dsde) {
        System.out.println("dropActionChanged");
    }

    public void dragDropEnd(DragSourceDropEvent dsde) {
        System.out.println("dragDropEnd");
    }

}