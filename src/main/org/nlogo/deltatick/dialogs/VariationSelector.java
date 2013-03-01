package org.nlogo.deltatick.dialogs;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: aditi
 * Date: 10/12/11
 * Time: 12:24 AM
 * To change this template use File | Settings | File Templates.
 */


    // TODO: Add EXIT_ON_CLOSE

public class VariationSelector
        extends JDialog {
    JPanel text;
    JButton addVariation;
    JPanel buttonsPanel;

    JLabel label;
    boolean populate;

    HashMap<String, String> numberVariation = new HashMap<String, String>();



    ArrayList<JTextField> varInputList = new ArrayList<JTextField>();
    ArrayList<String> variationList = new ArrayList<String>();
    ArrayList<JTextField> numberInputList = new ArrayList<JTextField>();
    ArrayList<String> numberList = new ArrayList<String>();

    private JDialog thisDialog = this;

    public VariationSelector(Frame parent) {
        super(parent, true);
        initComponents();
        this.setVisible(false);
        boolean populate = false;
    }

    public void showMe() {
        variationList.clear();
        numberList.clear();
        thisDialog.setVisible(true);
    }

    public void initComponents() {
        thisDialog.setSize(1000, 1000);
        JPanel text = new JPanel();
        JLabel label = new JLabel("What are the variations of this trait?");
        text.add(label);
        JLabel label1 = new JLabel();
        label1.setText("Variation");
        JLabel label2 = new JLabel();
        label2.setText("What percentage of the population has this variation?");
        label.setVisible(true);

        for (int i = 0; i < 6; i++) {
            varInputList.add(new JTextField(8));
        }

        for (int i = 0; i < 6; i++) {
            numberInputList.add(new JTextField(3));
        }

        addVariation = new JButton("Add variation");
        activateButtons();


        thisDialog.setDefaultCloseOperation(HIDE_ON_CLOSE);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setAutocreateContainerGaps(true);
        layout.setAutocreateGaps(true);

        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(text)
                        .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup()
                                        .add(label1)
                                        .add(varInputList.get(0))
                                        .add(varInputList.get(1))
                                        .add(varInputList.get(2))
                                        .add(varInputList.get(3))
                                        .add(varInputList.get(4)))
                                .add(layout.createParallelGroup()
                                        .add(label2)
                                        .add(numberInputList.get(0))
                                        .add(numberInputList.get(1))
                                        .add(numberInputList.get(2))
                                        .add(numberInputList.get(3))
                                        .add(numberInputList.get(4)))
                                .add(layout.createSequentialGroup()
                                        .add(addVariation)

                                        )
                        ));

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .add(text)
                        .add(layout.createParallelGroup()
                                .add(label1)
                                .add(label2))
                                .add(layout.createParallelGroup()
                                        .add(varInputList.get(0))
                                        .add(numberInputList.get(0)))
                                        .add(layout.createParallelGroup()
                                            .add(varInputList.get(1))
                                            .add(numberInputList.get(1)))
                                        .add(layout.createParallelGroup()
                .add(varInputList.get(2))
                .add(numberInputList.get(2)))
                .add(layout.createParallelGroup()
                .add(varInputList.get(3))
                .add(numberInputList.get(3)))
                .add(layout.createParallelGroup()
                .add(varInputList.get(4))
                .add(numberInputList.get(4)))
                .add(layout.createParallelGroup()
                .add(addVariation)
                )
                );
        pack();
    }


    public void activateButtons() {
        addVariation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                int i = 0;
                for ( JTextField textField : varInputList ) {
                    if (varInputList.get(i).getText().isEmpty() == false) {
                        variationList.add(varInputList.get(i).getText());
                    }
                    i++;
                }
                populate = true;
                getVariationList();

                int n = 0;
                for ( JTextField textfield : numberInputList ) {

                    numberList.add(numberInputList.get(n).getText());
                    n++;
                }
                getNumberList();


                data();

                int j = 0;
                for ( JTextField textField : varInputList ) {
                    textField.setText("");
                    j++;
                }

                int k = 0;
                for ( JTextField textField : numberInputList ) {
                    textField.setText("");
                    k++;
                }
                varInputList.get(0).requestFocus();
                thisDialog.setVisible(false);
            }
        }
        );
    }

    public void varName(int i) {
        variationList.add(varInputList.get(i).getText());
    }

    public ArrayList<String> getVariationList() {
        return variationList;
    }

    public void number (int i) {
        numberList.add(numberInputList.get(i).getText());
    }

    public ArrayList<String> getNumberList() {
        return numberList;
    }

    public boolean check() {
        return populate;
    }


  public HashMap<String, String> data() {

        int i = 0;
        for ( String string : variationList ) {

            numberVariation.put( string, numberList.get(i) );

            i++;

        }

        System.out.println(numberVariation);
        return numberVariation;



    }

}

