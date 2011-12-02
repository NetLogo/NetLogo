package org.nlogo.deltatick.dnd;

import org.nlogo.deltatick.BreedBlock;
import org.nlogo.deltatick.BuildPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.IOException;

public class MainDropTarget
        implements DropTargetListener {
    JPanel panel;
    java.awt.dnd.DropTarget dropTarget;
    DataFlavor breedBlockFlavor = new DataFlavor(BreedBlock.class, "Breed Block");

    public MainDropTarget( JPanel panel ) {
        this.panel = panel;
        dropTarget = new java.awt.dnd.DropTarget(panel, DnDConstants.ACTION_MOVE, this, true, null);

    }

    public void dragEnter( DropTargetDragEvent dtde ) { }
    public void dragExit( DropTargetEvent dte ) {}
    public void dragOver( DropTargetDragEvent dtde ) { }
    public void dropActionChanged( DropTargetDragEvent dtde ) {}

    public void drop( DropTargetDropEvent dtde ) {
        // Check the drop action
        if ((dtde.getDropAction() & DnDConstants.ACTION_MOVE) != 0) {
            // Accept the drop and get the transfer data
            dtde.acceptDrop(dtde.getDropAction());
            Transferable transferable = dtde.getTransferable();
            
            try {
                boolean result = dropComponent(transferable, dtde);
                dtde.dropComplete(result);
            } catch (Exception e) {
                dtde.dropComplete(false);
            }
        } else {
            dtde.rejectDrop();
        }
    }

    protected boolean dropComponent(Transferable transferable, DropTargetDropEvent dtde)
            throws IOException, UnsupportedFlavorException {
        Object o = transferable.getTransferData(breedBlockFlavor);
        if (o instanceof Component) {
            System.out.println(((BuildPanel) panel).getMyBreeds().toArray()[0]);
            System.out.println(transferable.getTransferData(breedBlockFlavor));
            System.out.println(transferable.getTransferData(breedBlockFlavor).equals( ((BuildPanel) panel).getMyBreeds().toArray()[0] ));
            ((Component) o).setLocation(dtde.getLocation());
            panel.validate();
            return true;
        }
        return false;
    }

}
