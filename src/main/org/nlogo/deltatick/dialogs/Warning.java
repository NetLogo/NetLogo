package org.nlogo.deltatick.dialogs;


import org.apache.log4j.Layout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 3/4/13
 * Time: 2:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class Warning extends JDialog {
    private JButton okay;
    private JLabel message;
    private JPanel panel;

    private JDialog dialog = this;

    public Warning(String breedName, String traitName) {
        panel = new JPanel();
        message = new JLabel();
        message.setText(traitName + " is not a trait of " + breedName);
        panel.add(message);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));


        okay = new JButton("Okay");
        panel.add(okay);
        okay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dispose();
            }
        });
        this.add(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        //JOptionPane.showMessageDialog();

        this.setPreferredSize(new Dimension(50, 40));
        this.setVisible(true);
    }
}
