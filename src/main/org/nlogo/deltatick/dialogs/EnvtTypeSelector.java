package org.nlogo.deltatick.dialogs;

import org.nlogo.deltatick.xml.ModelBackgroundInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: aditi
 * Date: 10/15/11
 * Time: 1:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class EnvtTypeSelector
        extends JDialog {
    private javax.swing.JButton cancel;
    private javax.swing.JButton add;
    private javax.swing.JLabel infoText;
    private javax.swing.JList envtList;
    private javax.swing.JScrollPane jScrollPane1;
    private String selectedEnvt;

    private javax.swing.JDialog thisDialog = this;


    public EnvtTypeSelector (Frame parent) {
        super(parent, true);
        initComponents();
        this.setVisible(false);
    }

    public void showMe(ModelBackgroundInfo backgroundInfo) {
        final String[] strings = backgroundInfo.getEnvtTypes();
        envtList.setModel(new javax.swing.AbstractListModel() {
            public int getSize() {
                //System.out.println(strings.length);
                return strings.length;
            }

            public Object getElementAt(int i) {
                //System.out.println(strings[i]);
                return strings[i];
            }
        });
        jScrollPane1.setViewportView(envtList);
        //System.out.println(strings);
        this.setVisible(true);
    }


    public void activateButtons() {
        add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getSelectedEnvt();
                System.out.println("EnvtTypeSelector");
                thisDialog.setVisible(false);
    }
        });

        //cancel.addActionListener(new java);
    }

    public String getSelectedEnvt() {
        selectedEnvt = envtList.getSelectedValue().toString();
        return selectedEnvt;

    }

    public String selectedEnvt() {
        //System.out.println(envtList.getSelectedValue().toString());
        selectedEnvt = envtList.getSelectedValue().toString();
        return selectedEnvt;

    }
    public void initComponents() {
        infoText = new JLabel("What type of environment would you like to add?");
        add = new JButton("Add");
        envtList = new javax.swing.JList();
        jScrollPane1 = new JScrollPane();
        cancel = new JButton();
        activateButtons();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                    .addContainerGap()
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                    .add(24, 24, 24)
                                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 199, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            )
                            .add(infoText))
                    // .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            )
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(115, Short.MAX_VALUE)
                .add(add)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                //.add(cancel)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(infoText)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    //.add(cancel)
                    .add(add))
                .add(26, 26, 26))
        );

        pack();
    }// </editor-fold>
    }

