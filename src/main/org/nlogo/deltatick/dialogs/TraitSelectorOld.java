package org.nlogo.deltatick.dialogs;


import javax.swing.*;
import javax.swing.GroupLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.layout.*;
import org.nlogo.api.JavaLibraryPath;
import org.nlogo.deltatick.BreedBlock;
import org.nlogo.deltatick.BuildPanel;
import org.nlogo.deltatick.TraitBlock;
import org.nlogo.deltatick.xml.Breed;

import org.nlogo.deltatick.xml.ModelBackgroundInfo;

import org.nlogo.deltatick.dnd.PrettyInput;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.PublicKey;
import java.util.*;
import java.awt.Component;

/**
 * Created by IntelliJ IDEA.
 * User: aditi
 * Date: 10/4/11
 * Time: 2:26 PM
 * To change this template use File | Settings | File Templates.
 */
 // We don't need this class if learners are selecting a pre-determined list of traits

public class TraitSelectorOld
        extends JDialog

{
    javax.swing.JButton add;
    JButton cancel;
    JLabel label;
    JTextField enterTrait;
    String Trait;


    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList myBreedsList;
    JFrame frame;
    HashMap<String, TraitBlock>breedTraitHashMap = new HashMap<String, TraitBlock>();

    private JDialog thisDialog = this;

    ListSelectionModel listSelectionModel;

    public TraitSelectorOld(java.awt.Frame parent) {
        super(parent, true);
        initDialogBox();
        activateButtons();
    }

    public void activateButtons() {
        add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                traitName();
                thisDialog.setVisible(false);
            }
        });
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enterTrait.setText("");
                thisDialog.setVisible(false);
                thisDialog.dispose();
            }
        });
    }

    public void initDialogBox() {
        thisDialog.setSize(500, 450);
        jScrollPane1 = new JScrollPane();

        label = new JLabel("What trait would you like to model in this species?");
        label.setSize(10, 10);
        label.setVisible(true);

        //buttons
        add = new JButton("Add");
        add.setVisible(true);
        add.setSize(10, 10);
        add.setEnabled(false);

        cancel = new JButton("Cancel");
        cancel.setVisible(true);
        cancel.setSize(10, 10);

        //Text area
        enterTrait = new JTextField(10);
        enterTrait.setSize(300, 20);
        enterTrait.setVisible(true);
        enterTrait.setEnabled(false);
        enterTrait.requestFocus();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(label)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1)
                                .addGroup(layout.createParallelGroup()
                                        .addComponent(enterTrait)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(add)
                                                .addComponent(cancel)))
                                )

        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(label)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(jScrollPane1)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(enterTrait)
                                        .addGroup(layout.createParallelGroup()
                                                .addComponent(add)
                                                .addComponent(cancel))))

        );
        pack();
    }

    public String traitName() {
        return Trait;
    }

    public void showMe( BuildPanel buildPanel ) {
        clearTrait();
        myBreedsList = new JList();

        final String[] strings = buildPanel.getbreedNames();
        myBreedsList.setModel(new javax.swing.AbstractListModel() {
            public int getSize() {
                return strings.length;
            }
            public Object getElementAt(int i) {
                return strings[i];
            }
        });
        jScrollPane1.setViewportView(myBreedsList);

        listSelectionModel = myBreedsList.getSelectionModel();
        listSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listSelectionModel.addListSelectionListener(
                new ListSelectionHandler());

        thisDialog.setVisible(true);
        Trait = enterTrait.getText();
    }

    class ListSelectionHandler implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel lsm = (ListSelectionModel)e.getSource();
            if (lsm.isSelectionEmpty()) {
                enterTrait.setEnabled(false);
            }
            else {
                enterTrait.setEnabled(true);
                add.setEnabled(true);
            }
        }
    }

    public void clearTrait() {
        enterTrait.setText("");
    }

    public String selectedBreed() {
        return myBreedsList.getSelectedValue().toString();
    }

}

