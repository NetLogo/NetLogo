package org.nlogo.deltatick.dialogs;

import javax.swing.*;

import org.nlogo.api.JavaLibraryPath;
import org.nlogo.deltatick.xml.Breed;

import org.nlogo.deltatick.xml.ModelBackgroundInfo;

import org.nlogo.deltatick.dnd.PrettyInput;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.PublicKey;
import java.util.LinkedList;
import java.awt.Component;

/**
 * Created by IntelliJ IDEA.
 * User: aditi
 * Date: 10/4/11
 * Time: 2:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class TraitSelector
        extends JDialog
       // implements ActionListener
        {
    private javax.swing.JButton add;
    JPanel buttonsPanel;
    JButton cancel;
    JLabel label;
    JTextField enterTrait;
    String Trait;
    String printMe;

    JFrame frame;
    VariationSelector variationSelector;



    private JDialog thisDialog = this;

    public TraitSelector (java.awt.Frame parent) {
        super(parent, true);
        createDialogBox();
        activateButtons();
        this.setVisible(false);
    }

    public void activateButtons() {
        add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //String userInput = enterTrait.getText();
                //System.out.println(userInput);
                traitName();
                thisDialog.setVisible(false);
                //clearTrait();
                //enterTrait.setText("");
                //VariationSelector variationSelector = new VariationSelector(java.awt.Frame );
                
            }
            //enterTrait.setText("");
        });
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //thisDialog.setDefaultCloseOperation(HIDE_ON_CLOSE);
                //enterTrait.clear();
                enterTrait.setText("");
                thisDialog.setVisible(false);
                thisDialog.dispose();
            }

        }
        );
    }

    public void createDialogBox() {
        thisDialog.setSize(300, 150);

        //Panel
        buttonsPanel = new JPanel();
        thisDialog.add(buttonsPanel);
        buttonsPanel.setSize(300, 150);
        buttonsPanel.setBackground(Color.white);
        buttonsPanel.setOpaque(true);

        label = new JLabel("What trait would you like to model in this species?");
        buttonsPanel.add(label);
        label.setSize(10, 10);
        label.setLocation(20, 0);
        label.setVisible(true);

        //buttons
        add = new JButton("Add");
        buttonsPanel.add(add);
        add.setVisible(true);
        add.setSize(10, 10);
        add.setLocation(100, 200);


        cancel = new JButton("Cancel");
        buttonsPanel.add(cancel);
        cancel.setVisible(true);
        cancel.setSize(10, 10);
        cancel.setLocation(200, 200);

        //Text area
        //enterText = new JTextArea(userInput);
        enterTrait = new JTextField(10);
        buttonsPanel.add(enterTrait);
        //System.out.println("Creating new button");
        //enterTrait.setText("");
        enterTrait.setLocation(250, 100);
        enterTrait.setSize(300, 20);
        enterTrait.setVisible(true);
        enterTrait.requestFocus();
        /*
        enterTrait.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                traitName();
                         
            }

        });
        */
        
    }

    public String traitName() {

        Trait = enterTrait.getText();
        System.out.println(Trait);
        return Trait;
        //return (String) enterTrait.getText();
    }

    public void showMe() {
        clearTrait();
        thisDialog.setVisible(true);
        //System.out.println("showMe");
        traitName();
    }

    public String printMe() {
        return Trait;

    }

    public void clearTrait() {
        enterTrait.setText("");
    }

}

