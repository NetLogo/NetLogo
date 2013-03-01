package org.nlogo.deltatick;

import org.nlogo.deltatick.dialogs.TraitSelector;
import org.nlogo.deltatick.reps.Piechart;
import org.nlogo.deltatick.reps.Piechart1;
import org.nlogo.deltatick.xml.Trait;
import org.nlogo.deltatick.xml.Variation;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 1/20/13
 * Time: 11:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class SpeciesInspectorPanel extends JPanel {

    BreedBlock myParent;
    JPanel topPanel = new JPanel();
    JPanel midPanel = new JPanel();
    JPanel sidePanel = new JPanel(true);
    JPanel bottomPanel = new JPanel();
    JLabel lifeSpanLabel = new JLabel("What is the max age?"); //TODO add myParent.plural()
    JTextField lifeSpanBlank = new JTextField();
    JLabel energyLabel = new JLabel("What is the max energy?"); //TODO add myParent.plural()
    JTextField energyBlank = new JTextField();
    JButton cancelButton = new JButton("Cancel");
    JButton okayButton = new JButton("Okay"); // placeholder button to test sending values - A. (Jan 22, 2013)
    JButton addTrait = new JButton("add trait");

    JFrame myFrame;

    JTabbedPane traitsTabbedPane;
    TraitSelector traitSelector;
    int countTabs = 1;

    SpeciesInspector speciesInspector = new SpeciesInspector();
    TraitPreview traitPreview;
    TraitDisplay traitDisplay = new TraitDisplay();

    public SpeciesInspectorPanel(BreedBlock myParent, JFrame myFrame) {
        this.myParent = myParent;
        this.myFrame = myFrame;
        energyBlank.setMaximumSize(new Dimension(20, 30));
        lifeSpanBlank.setMaximumSize(new Dimension(20, 30));
        updateText();
        activateButtons();
    }

    public void updateText() {
        energyBlank.setText(myParent.getMaxEnergy());
        lifeSpanBlank.setText(myParent.getMaxAge());
    }

    public void addPanels(Container pane) {

        setupTopPanel();
        setupSidePanel();
        setupMidPanel();
        setupBottomPanel();

        GroupLayout layout = new GroupLayout(pane);
        pane.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(topPanel)
                        .addGroup(layout.createSequentialGroup()
                        .addComponent(midPanel)
                        .addComponent(sidePanel))
                //.addComponent(bottomPanel)
                );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                .addComponent(topPanel)
                .addGroup(layout.createParallelGroup()
                .addComponent(midPanel)
                .addComponent(sidePanel))
                //.addComponent(bottomPanel)
        );
        validate();
    }

    public void setupTopPanel() {
        topPanel.setBackground(ColorSchemer.getColor(3));
        midPanel.setBackground(ColorSchemer.getColor(3));
        sidePanel.setBackground(ColorSchemer.getColor(3));
        bottomPanel.setBackground(ColorSchemer.getColor(3));
        TitledBorder titleTopPanel;
        titleTopPanel = BorderFactory.createTitledBorder("Set up");
        topPanel.setBorder(titleTopPanel);

        GroupLayout layout = new GroupLayout(topPanel);
        topPanel.setLayout(layout);

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGap(5)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(lifeSpanLabel)
                                .addComponent(lifeSpanBlank))
                        .addGap(5)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(energyLabel)
                                .addComponent(energyBlank))
        );

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                    //.addComponent(breedNameLabel)
                    .addComponent(lifeSpanLabel)
                    .addComponent(energyLabel))
                .addGroup(layout.createParallelGroup()
                .addGap(10)
               // .addComponent(breedName)
               )
                .addGroup(layout.createParallelGroup()
                .addComponent(lifeSpanBlank, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(energyBlank, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(50)
        );
        validate();

    }

    public void setupSidePanel() {
        TitledBorder titleSidePanel;
        titleSidePanel = BorderFactory.createTitledBorder("Display");
        sidePanel.setBorder(titleSidePanel);
        traitDisplay = new TraitDisplay();
        //traitDisplay.add((new Piechart1()).getChartPanel());
        sidePanel.add(traitDisplay);
        sidePanel.validate();
    }

    public void setupMidPanel() {
        TitledBorder titleMidPanel;
        titleMidPanel = BorderFactory.createTitledBorder("Preview Traits");
        midPanel.setBorder(titleMidPanel);
        traitPreview = new TraitPreview(myParent.plural(), traitDisplay);
        midPanel.add(traitPreview);
        traitPreview.setTraits(myParent.getTraits());
        traitPreview.showMe();
    }

    public void setupBottomPanel() {
        TitledBorder titleBottomPanel;
        Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        titleBottomPanel = BorderFactory.createTitledBorder(loweredetched, "Traits");
        bottomPanel.setBorder(titleBottomPanel);

        GroupLayout layout = new GroupLayout(bottomPanel);
        bottomPanel.setLayout(layout);
        traitsTabbedPane = new JTabbedPane();

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                .addComponent(addTrait)
                .addComponent(traitsTabbedPane)
                .addGap(10)
                .addGroup(layout.createParallelGroup()
                .addComponent(cancelButton)
                .addComponent(okayButton))
        );

        layout.setHorizontalGroup(
                layout.createParallelGroup()
                .addComponent(addTrait)
                .addComponent(traitsTabbedPane)
                .addGroup(layout.createSequentialGroup()
                	.addGap(165)
                	.addComponent(cancelButton)
                	.addGap(10)
                	.addComponent(okayButton))
        );
        layout.linkSize(SwingConstants.HORIZONTAL, cancelButton, okayButton);
        validate();

        if (countTabs == 1) {
            JPanel panel1 = new JPanel();
        }
    }

    public void populateTraitTabs() {
        for (Trait trait : speciesInspector.getSelectedTraitsList()) {
            TraitDisplayPanel traitDisplayPanel = new TraitDisplayPanel(trait);
        }
    }

    public void activateButtons() {
        addTrait.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                traitSelector = new TraitSelector();
                traitSelector.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                traitSelector.setTraits(myParent.getTraits());
                traitSelector.showMe();
                if (traitSelector.getIsTraitSelected() == true) {
                speciesInspector.selectedTraitsList.add(traitSelector.getSelectedTrait());
                TraitDisplayPanel traitDisplayPanel = new TraitDisplayPanel(traitSelector.getSelectedTrait());
                traitDisplayPanel.setPreferredSize(new Dimension(500,150));
                traitsTabbedPane.addTab(traitSelector.getSelectedTrait().getNameTrait(), traitDisplayPanel);
                }
            }
        });
        okayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

            }
        });
    }

    public String getEndListSpan() {
        return lifeSpanBlank.getText().toString();
    }

    public String getHighestEnergy() {
        return energyBlank.getText().toString();
    }

    public SpeciesInspector getSpeciesInspector() {
        return speciesInspector;
    }

    public JButton getOkayButton() {
        return okayButton;
    }

    public JFrame getMyFrame() {
        return myFrame;
    }

    public BreedBlock getMyParent() {
        return myParent;
    }

    public void setSelectedTrait() {
        speciesInspector.addToSelectedTraitsList(traitPreview.getSelectedTrait());
    }

    public void setSelectedVariations(Trait trait, Variation variation) {
        speciesInspector.addtoSelectedVariations(trait, variation);
    }
}
