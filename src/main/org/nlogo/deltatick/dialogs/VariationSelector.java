package org.nlogo.deltatick.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import org.nlogo.deltatick.dialogs.TraitSelector;

/**
 * Created by IntelliJ IDEA.
 * User: aditi
 * Date: 10/12/11
 * Time: 12:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class VariationSelector
    extends JDialog
        {
        JPanel text;
        JButton addVariation;
        JPanel buttonsPanel;
        JButton okay;
        JLabel label;
        JTextField varInput1;
        JTextField varInput2;
        JTextField varInput3;
        String Trait;
        String Variation1;
        int counter;

        ArrayList<JTextField> varInputList = new ArrayList<JTextField>();
        ArrayList<String> variationList = new ArrayList<String>();

    private JDialog thisDialog = this;

    public VariationSelector(Frame parent) {
        super(parent, true);
        initComponents();
        this.setVisible(false);
        varInput2.setVisible(false);
        counter = 0;

    }

    public void showMe() {
        //varInput1.setText("");
        varInput2.setText("");
        varInput3.setText("");
        thisDialog.setVisible(true);
        }

    public void initComponents() {
        thisDialog.setSize(500, 500);
        JPanel text = new JPanel();
        JLabel label = new JLabel("What are the variations of this trait?");
        text.add(label);
        label.setSize(400,150);
        label.setVisible(true);

        for (int i = 0; i < 3; i ++) {
            varInputList.add(new JTextField(8));
        }
        varInputList.get(0).setName("varInput1");
        varInput2 = varInputList.get(1);
        varInput3 = varInputList.get(2);

        addVariation = new JButton("Add variation");
        //TODO change color of font appearing in TextField to gray
        okay = new JButton("Okay");
        okay.setVisible(true);
        okay.setEnabled(false);
        activateButtons();
        //okay.setEnabled(true);
        thisDialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);


org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()

                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(layout.createSequentialGroup()
                                                .add(24, 24, 24)
                                                //.add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 199, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        )
                                        .add(label))
                                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(115, Short.MAX_VALUE)
                                .add(addVariation)
                                .addContainerGap(115, Short.MAX_VALUE)
                                .add(varInputList.get(1))
                                .add(varInputList.get(0))

                                        //.add(okay)
                                        //.add(varInput1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                        //.add(cancel)
                                .addContainerGap()
                                        //.add(varInputList.get(1))

                                .add(okay)));

        //);
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .add(label)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        //.add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                        // .add(cancel)
                                        //.add(addVariation)
                                        .add(addVariation)
                                        .add(varInputList.get(0))
                                        .add(varInputList.get(1))
                                        //.varInputList.get(2).setVisible(false)
                                        //.add(okay)
                                        //.add(varInput1)
                                )

                                .add(26, 26, 26)

                                .add(okay))
        );
        //System.out.println(varInputList.get(2).getName() + " " + varInputList.size());
        //varInputList.get(0).setVisible(false);
        varInputList.get(1).setVisible(false);
        pack();
    }// </editor-fold>



    public void activateButtons() {
        //add1.: When clicked, show "Okay button and Variation2 textfield, also send value to breedblock
        addVariation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed (java.awt.event.ActionEvent evt) {
                varName(counter);
                counter++;
                varInputList.get(counter).setVisible(true);
                //okay.setEnabled(true);
                thisDialog.validate();
                //okay.setEnabled(true);
            }
        }
        );
        okay.addActionListener(new ActionListener() {
            public void actionPerformed (java.awt.event.ActionEvent evt) {
                thisDialog.setVisible(false);
            }
        });
    }
    public void varName(int i) {
            variationList.add(varInputList.get(i).getText());

                }

    public ArrayList<String> getVariationList() {
            return variationList;
            }
        }
