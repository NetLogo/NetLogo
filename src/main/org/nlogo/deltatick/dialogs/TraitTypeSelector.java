package org.nlogo.deltatick.dialogs;

import org.jdesktop.layout.*;
import org.nlogo.deltatick.BreedBlock;
import org.nlogo.deltatick.TraitBlock;
import org.nlogo.deltatick.xml.ModelBackgroundInfo;
import org.nlogo.deltatick.BuildPanel;
import org.nlogo.deltatick.xml.Breed;
import org.nlogo.deltatick.xml.Trait;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 8/4/12
 * Time: 12:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class TraitTypeSelector
    extends JDialog {

    //Buttons & text
    private javax.swing.JButton cancel;
    private javax.swing.JButton add;
    private javax.swing.JLabel breedText;
    private JLabel traitText;
    private JLabel variationText;

    //Scrolls & data for Breeds & Traits
    private javax.swing.JScrollPane jScrollPane1;     //for breeds
    private javax.swing.JList myBreedsList;
    private String selectedBreed;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList myTraitsList;           //for traits
    private String selectedTrait;
    private JScrollPane jScrollPane3;  // for variations
    private JList myVariationsList;

    private BreedBlock breedBlock;
    private ModelBackgroundInfo bgInfo;
    private BuildPanel bPanel;

    HashMap<String, TraitBlock> breedTraitHashMap = new HashMap<String, TraitBlock>();
    //to store breed and corresponding trait

    ListSelectionModel listSelectionModel;

    private javax.swing.JDialog thisDialog = this;


    public TraitTypeSelector(Frame parent) {
        super(parent, true);
        initComponents();
        this.setVisible(false);
    }

    public TraitTypeSelector() {
        initComponents();
        this.setVisible(true);
    }

    public void showMe(BuildPanel buildPanel, ModelBackgroundInfo backgroundInfo) {
        this.bgInfo = backgroundInfo;
        this.bPanel = buildPanel;
        myBreedsList = new JList();

        final String[] breedStrings = buildPanel.getbreedNames();
        myBreedsList.setModel(new javax.swing.AbstractListModel() {
            public int getSize() {
                return breedStrings.length;
            }
            public Object getElementAt(int i) {
                return breedStrings[i];
            }
        });
        jScrollPane1.setViewportView(myBreedsList);

        listSelectionModel = myBreedsList.getSelectionModel();
        listSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listSelectionModel.addListSelectionListener(
                new BreedListSelectionHandler());

        // Show traits from XML

        final String[] traitStrings = backgroundInfo.getTraitTypes();
        myTraitsList.setModel(new javax.swing.AbstractListModel() {
            public int getSize() {
                return traitStrings.length;
            }
            public Object getElementAt(int i) {
                return traitStrings[i];
            }
        });
        jScrollPane2.setViewportView(myTraitsList);

        listSelectionModel = myTraitsList.getSelectionModel();
        listSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listSelectionModel.addListSelectionListener(
                new TraitListSelectionHandler());

        this.setVisible(true);
    }



    class BreedListSelectionHandler implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel lsm = (ListSelectionModel)e.getSource();
            if (lsm.isSelectionEmpty()) {
                //enterTrait.setEnabled(false);
            }
            else {
                //enterTrait.setEnabled(true);
                add.setEnabled(true);
            }
        }
    }

    class TraitListSelectionHandler implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel lsm = (ListSelectionModel)e.getSource();
            myVariationsList = new JList();
            if (lsm.isSelectionEmpty()) {
                System.out.println("No trait selected");
            }
            else {
                    final String[] variationStrings = updateVariations(bgInfo, getSelectedTraitName()) ;
                    myVariationsList.setModel(new javax.swing.AbstractListModel() {
                    public int getSize() {
                        return variationStrings.length;
                    }
                public Object getElementAt(int i) {
                    return variationStrings[i];
                }
            });
                jScrollPane3.setViewportView(myVariationsList);
                jScrollPane3.setEnabled(false);
            }
        }
    }

    public String[] updateVariations(ModelBackgroundInfo backgroundInfo, String selectedTrait) {
        return backgroundInfo.getVariationTypes(selectedTrait);
    }

    public void activateButtons() {
        add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                thisDialog.setVisible(false);
            }
        });
    }

    public String getSelectedBreed() {
        selectedBreed = myBreedsList.getSelectedValue().toString();
        return selectedBreed;
    }

    public BreedBlock getSelectedBreedBlock() {
        for (BreedBlock block : bPanel.getMyBreeds()) {
                    if (myBreedsList.getSelectedValue().toString().equals(block.plural())) {
                        breedBlock = block;
                    }
                }
        return breedBlock;
    }

    public String getSelectedTraitName() {
        selectedTrait = myTraitsList.getSelectedValue().toString();
        return selectedTrait;
    }

    public Trait getSelectedTrait() {
        int i= 0;
        Trait trait = null;

        for (Trait t : bgInfo.getTraits()) {
            if (t.getNameTrait() == this.getSelectedTraitName()) {
                trait = t;
                break;
            }
        }
        return trait;
    }

    public void initComponents() {
        breedText = new JLabel("Pick a species");
        traitText = new JLabel("Pick a trait");
        variationText = new JLabel("Available variations");
        add = new JButton("Add");
        myBreedsList = new JList();
        myTraitsList = new javax.swing.JList();
        jScrollPane1 = new JScrollPane();
        jScrollPane2 = new JScrollPane();
        jScrollPane3 = new JScrollPane();
        cancel = new JButton();
        activateButtons();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(breedText)
                                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 199, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        )
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(traitText)
                                .add(jScrollPane2)
                                .add(layout.createSequentialGroup()
                                        .add(add)
                                ))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(variationText)
                                .add(jScrollPane3)));
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .add(layout.createParallelGroup()
                                .add(breedText)
                                .add(traitText)
                                .add(variationText))
                        .add(layout.createParallelGroup()
                                .add(jScrollPane1)
                                .add(jScrollPane2)
                                .add(jScrollPane3))
                        .add(layout.createParallelGroup()
                                .add(add)
                        )

        );
        pack();
    }
}


