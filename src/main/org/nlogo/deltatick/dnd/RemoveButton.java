package org.nlogo.deltatick.dnd;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwilkerson
 * Date: Sep 26, 2010
 * Time: 1:04:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class RemoveButton extends JButton {
    JPanel myParent;
    RemoveButton thisButton;

    public RemoveButton( JPanel myParent ) {
        this.myParent = myParent;
        this.thisButton = this;
        setAction(deleteAction);
        setBorder(null);
        setBorderPainted(false);
        setMargin(new java.awt.Insets(5, 5, 5, 5));
    }

    private final javax.swing.Action deleteAction =
		new javax.swing.AbstractAction( "X") {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
                Container homePanel = myParent.getParent();
            	homePanel.remove(myParent);
                homePanel.repaint();
            }
        };
}
