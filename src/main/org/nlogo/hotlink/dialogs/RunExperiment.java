package org.nlogo.hotlink.dialogs;

import javax.swing.*;
import java.awt.event.*;

public class RunExperiment extends JDialog {
    //private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JSlider numberOfRunsSlider;
    private JSlider tickSlider;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    public boolean ok;

    public RunExperiment() {
        initComponents();
        setSize( getPreferredSize() );
        //setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
    }

    private void onOK() {
// add your code here
        ok = true;
        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        ok = false;
        dispose();
    }

    public int getRunCount() {
        return numberOfRunsSlider.getValue();
    }

    public int getTickCount() {
        return tickSlider.getValue();
    }

    public boolean ok() {
        return ok;
    }

    // code from NetBeans GUI designer
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        numberOfRunsSlider = new javax.swing.JSlider();
        tickSlider = new javax.swing.JSlider();
        buttonCancel = new javax.swing.JButton();
        buttonOK = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Select how many ticks and repetitions to run the model.");

        jLabel2.setText("Ticks per Run");

        jLabel3.setText("Number of Runs");

        numberOfRunsSlider.setMajorTickSpacing(9);
        numberOfRunsSlider.setMaximum(10);
        numberOfRunsSlider.setMinimum(1);
        numberOfRunsSlider.setMinorTickSpacing(1);
        numberOfRunsSlider.setPaintLabels(true);
        numberOfRunsSlider.setPaintTicks(true);

        tickSlider.setMaximum(200);
        tickSlider.setMinimum(50);
        tickSlider.setMinorTickSpacing(5);
        tickSlider.setPaintLabels(true);
        tickSlider.setPaintTicks(true);
        tickSlider.setSnapToTicks(true);

        buttonCancel.setText("Cancel");

        buttonOK.setText("OK");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel3)
                        .add(8, 8, 8)
                        .add(numberOfRunsSlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE))
                    .add(jLabel1)
                    .add(layout.createSequentialGroup()
                        .add(jLabel2)
                        .add(34, 34, 34)
                        .add(tickSlider, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(buttonOK, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 96, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(buttonCancel)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(tickSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 43, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 44, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(numberOfRunsSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(buttonCancel)
                    .add(buttonOK))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }
}
