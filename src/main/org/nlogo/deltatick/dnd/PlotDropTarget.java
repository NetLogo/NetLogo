package org.nlogo.deltatick.dnd;

import org.nlogo.deltatick.*;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.IOException;

public class PlotDropTarget
        extends DropTarget {

    public PlotDropTarget(PlotBlock block) {
        super(block);
    }

    protected boolean dropComponent(Transferable transferable)
            throws IOException, UnsupportedFlavorException {
        Object o = transferable.getTransferData(CodeBlock.quantityBlockFlavor);
        if (o instanceof QuantityBlock) {
            if (((QuantityBlock) o).getHisto() == false) {
            addCodeBlock((QuantityBlock) o);
            return true;
            }
            else {
                String message = new String(((QuantityBlock) o).getName() + " is a block for histograms, not line graphs.");
                JOptionPane.showMessageDialog(null, message, "Oops!", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        return false;
    }
}