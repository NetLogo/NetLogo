package org.nlogo.deltatick.dnd;

import org.nlogo.deltatick.BuildPanel;
import org.nlogo.deltatick.CodeBlock;

import javax.swing.*;
import java.awt.dnd.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwilkerson
 * Date: Feb 22, 2010
 * Time: 7:48:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class BuildPanelDragSource implements DragGestureListener,
        DragSourceListener {

    BuildPanel panel;

    public BuildPanelDragSource(BuildPanel panel) {
        DragSource dragSource = DragSource.getDefaultDragSource();
        this.panel = panel;



        // Create a DragGestureRecognizer and
        // register as the listener
        // TODO: ACTION_COPY_OR_MOVE
        dragSource.createDefaultDragGestureRecognizer(panel, DnDConstants.ACTION_MOVE, this);
    }

    // Implementation of DragGestureListener interface.
    public void dragGestureRecognized(DragGestureEvent dge) {
        dge.startDrag(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.MOVE_CURSOR), panel);
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