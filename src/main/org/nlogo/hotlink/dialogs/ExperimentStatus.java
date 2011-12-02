package org.nlogo.hotlink.dialogs;

import javax.swing.*;
import org.nlogo.hotlink.controller.ExperimentRunner;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ExperimentStatus extends JDialog {
    private JButton abortButton;
    private JProgressBar experimentProgress;
    private JProgressBar tickProgress;
    ExperimentRunner runner;

    public ExperimentStatus( ExperimentRunner runner ) {
        initComponents();
        setSize( this.getPreferredSize() );
        
        //setContentPane(contentPane);
        setModal(false);
        this.runner = runner;

        abortButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onAbort();
            }
        });
    }

    public void setProgress( int value , int run ) {
        experimentProgress.setValue( value );
        experimentProgress.setString( "Run " + Integer.toString(run + 1) );
    }

    public void setTickProgress( int value , int tick ) {
        tickProgress.setValue( value );
        tickProgress.setString( "Tick " + Integer.toString(tick) );
    }

    private void onAbort() {
        //runner.interrupt();
        this.setVisible(false);
    }

    private void initComponents() {

        experimentProgress = new javax.swing.JProgressBar();
        tickProgress = new javax.swing.JProgressBar();
        abortButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        abortButton.setText("Cancel Experiment");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(experimentProgress, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(tickProgress, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(74, 74, 74)
                        .add(abortButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(16, Short.MAX_VALUE)
                .add(experimentProgress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(3, 3, 3)
                .add(tickProgress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(abortButton)
                .add(15, 15, 15))
        );

        pack();
    }
}
