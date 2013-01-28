package org.nlogo.deltatick.dialogs;

import org.nlogo.app.DeltaTickTab;
import org.nlogo.deltatick.BuildPanel;
import org.nlogo.deltatick.LibraryHolder;
import org.nlogo.deltatick.UserInput;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 3/10/12
 * Time: 4:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class OperatorBlockBuilder
        extends JDialog {
    JComboBox dropDownListA;
    JComboBox dropDownListB;
    JLabel textField;
    JCheckBox checkBox;
    JRadioButton and;
    JRadioButton or;
    JRadioButton neither;
    ButtonGroup buttonGroup;
    JButton build;
    JButton cancel;
    private JScrollPane jScrollPaneBreed;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    boolean populate;

    ActionListener actionListener;

    private JList allBreeds;
    private javax.swing.JList allTraits;
    ListSelectionModel listSelectionModel;

    private JList allTraits2;
    JFrame frame;


    UserInput userInput;
    DeltaTickTab myParent;

    private JDialog thisDialog = this;

    public OperatorBlockBuilder(Frame parent) {
        super (parent, true);
        //TODO Change order here of setVisible so box doesn't show up when program is run
        initComponents();
        thisDialog.setVisible(false);
        activateButtons();

    }

    public void initComponents () {
        //thisDialog.setSize( 1000, 1000 );
        //thisDialog.setVisible(true);
        textField = new JLabel( "If" );
        textField.setSize(10, 5);
        textField.setVisible(true);

        checkBox = new JCheckBox("Add condition");
        dropDownListA = new JComboBox();
        dropDownListB = new JComboBox();

        jScrollPaneBreed = new JScrollPane();
        jScrollPane1 = new JScrollPane();
        jScrollPane2 = new JScrollPane();


        and = new JRadioButton("and");
        or = new JRadioButton("or");
        neither = new JRadioButton("neither");
        buttonGroup = new ButtonGroup();
        buttonGroup.add(and);
        buttonGroup.add(or);
        buttonGroup.add(neither);
        and.setSelected(true);
        and.setEnabled(false);
        or.setEnabled(false);
        neither.setEnabled(false);

        build = new JButton( "Build");
        cancel = new JButton( "Cancel" );

        boolean populate = false;

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);


        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addComponent(jScrollPaneBreed)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(textField)
                                .addGroup(layout.createSequentialGroup()

                                        .addComponent(checkBox)
                                )
                        )
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                .addComponent(jScrollPane1)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(and)
                                        .addComponent(or)
                                        .addComponent(neither)
                                )
                                .addComponent(jScrollPane2)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(build)
                                        .addComponent(cancel))
                        )
        );

        layout.setVerticalGroup(
                layout.createParallelGroup()
                .addComponent(jScrollPaneBreed)
                        .addGroup(layout.createSequentialGroup()

                        .addComponent(textField)
                        .addComponent(jScrollPane1)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(checkBox)
                                .addComponent(and)
                                .addComponent(or)
                                .addComponent(neither)
                        )
                        .addComponent(jScrollPane2)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(build)
                                .addComponent(cancel))
        ));
        pack();
    }

    public void activateButtons () {
        checkBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
                boolean selected = abstractButton.getModel().isSelected();
                if (selected == true) {
                    and.setEnabled(true);
                    or.setEnabled(true);
                    neither.setEnabled(true);
                    jScrollPane2.getViewport().getView().setEnabled(true);
                }
                else {
                    and.setEnabled(false);
                    or.setEnabled(false);
                    neither.setEnabled(false);
                    jScrollPane2.getViewport().getView().setEnabled(false);
                }
      }
    });
        //TODO: cancel makes the program close
        cancel.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            }
        });

        build.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                populate = true;
                thisDialog.setVisible(false);
            }
        });
    }

    public void showMe( UserInput uIPut ) {
        this.userInput = uIPut;
        allBreeds = new JList();
            final String[] breeds = uIPut.getBreeds();
        allBreeds.setModel(new AbstractListModel() {
            public int getSize() {
                return breeds.length;
            }
            public Object getElementAt(int i) {
                return breeds[i];
            }
        });
        jScrollPaneBreed.setViewportView(allBreeds);

        listSelectionModel = allBreeds.getSelectionModel();
        listSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listSelectionModel.addListSelectionListener(
                new ListSelectionHandler());

        thisDialog.setVisible(true);
    }

    class ListSelectionHandler implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel lsm = (ListSelectionModel)e.getSource();

            if (lsm.isSelectionEmpty()) {
                disableScroll();

            } else {
                updateScrollPane();
                jScrollPane1.getViewport().getView().setEnabled(true);

                int minIndex = lsm.getMinSelectionIndex();
                if (lsm.isSelectedIndex(minIndex)) {
                        System.out.println("not empty " + minIndex + " " + allBreeds.getSelectedValue());
                }
            }
        }
    }

    public void updateScrollPane() {
        //ScrollPane 1
        allTraits = new JList();
                    //final String[] strings = uIPut.getBreedTraitVariation();
                final String[] strings = userInput.getTraits(allBreeds.getSelectedValue().toString());

                allTraits.setModel(new javax.swing.AbstractListModel() {
                    public int getSize() {
                        return strings.length;
                    }
                    public Object getElementAt(int i) {
                        return strings[i];
                    }
                });
                jScrollPane1.setViewportView(allTraits);


        //ScrollPane 2
        allTraits2 = new JList();

        final String[] strings1 = userInput.getTraitVariation(allBreeds.getSelectedValue().toString());


        allTraits2.setModel(new AbstractListModel() {
            public int getSize() {
                return strings.length;
            }
            public Object getElementAt(int i) {
                return strings[i];
            }
        });
        jScrollPane2.setViewportView(allTraits2);
    }

    public void disableScroll() {
        jScrollPane1.getViewport().getView().setEnabled(false);
        jScrollPane2.getViewport().getView().setEnabled(false);
    }

    public boolean check() {
        return populate;
    }

    public String selectedTrait () {
        return allTraits.getSelectedValue().toString();
    }

    public String selectedTrait2 () {
        return allTraits2.getSelectedValue().toString();
    }

    public String selectedBreed() {
        return allBreeds.getSelectedValue().toString();
    }

    public void setMyParent(DeltaTickTab tab) {
        myParent = tab;

    }
}
