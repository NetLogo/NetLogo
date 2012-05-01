package org.nlogo.deltatick.dnd;

import org.nlogo.deltatick.*;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: mwilkerson
 * Date: Mar 1, 2010
 * Time: 9:15:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class DropTarget
        implements DropTargetListener {

    CodeBlock block;
    java.awt.dnd.DropTarget dropTarget;

    public DropTarget(CodeBlock block) {
        this.block = block;
        dropTarget = new java.awt.dnd.DropTarget(block, DnDConstants.ACTION_COPY, this, true, null);
    }

    public void dragEnter(DropTargetDragEvent dtde) {
    }

    public void dragExit(DropTargetEvent dte) {
    }

    public void dragOver(DropTargetDragEvent dtde) {
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    public void drop(DropTargetDropEvent dtde) {
        // Check the drop action
        if ((dtde.getDropAction() & DnDConstants.ACTION_COPY) != 0) {
            // Accept the drop and get the transfer data
            dtde.acceptDrop(dtde.getDropAction());
            Transferable transferable = dtde.getTransferable();
            try {
                boolean result = dropComponent(transferable);
                dtde.dropComplete(result);
            } catch (Exception e) {
                dtde.dropComplete(false);
            }
        } else {
            dtde.rejectDrop();
        }
    }

    protected boolean dropComponent(Transferable transferable)
            throws IOException, UnsupportedFlavorException {

        return false;
    }


    void addCodeBlock(CodeBlock codeBlock) {
        block.addBlock(codeBlock);
        // mhw just added
        new PlantedCodeBlockDragSource(codeBlock);
        //block.revalidate();
        block.doLayout();
        block.validate();
        block.repaint();
        block.enableInputs();
    }
}
