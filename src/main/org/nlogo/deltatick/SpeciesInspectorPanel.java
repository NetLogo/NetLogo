package org.nlogo.deltatick;

import org.nlogo.deltatick.dialogs.TraitSelector;
import org.nlogo.deltatick.xml.Trait;
import org.nlogo.deltatick.xml.Variation;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

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
    JLabel lifeSpanLabel = new JLabel();
    JTextField lifeSpanBlank = new JTextField();
    JLabel energyLabel = new JLabel("What is the max energy?"); //TODO add myParent.plural()
    JTextField energyBlank = new JTextField();
    JButton cancelButton = new JButton("Cancel"); //actionListener in deltaticktab - March 2, 2013
    JButton okayButton = new JButton("Okay"); // actionListener in deltaticktab -March 2, 2013

    JFrame myFrame;

    SpeciesInspector speciesInspector = new SpeciesInspector();
    TraitPreview traitPreview;
    TraitDisplay traitDisplay = new TraitDisplay();
    LabelPanel labelPanel;


    public SpeciesInspectorPanel(BreedBlock myParent, JFrame myFrame) {
        this.myParent = myParent;
        this.myFrame = myFrame;
        energyBlank.setMaximumSize(new Dimension(20, 30));
        lifeSpanBlank.setMaximumSize(new Dimension(20, 30));
        updateText();
        //activateButtons();


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
                .addComponent(bottomPanel)
                );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                .addComponent(topPanel)
                .addGroup(layout.createParallelGroup()
                        .addComponent(midPanel)
                        .addComponent(sidePanel))
                .addComponent(bottomPanel)
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

        lifeSpanLabel.setText("How old do " + myParent.plural() + " live to be?");

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
        sidePanel.setPreferredSize(new Dimension(300,200));
        traitDisplay = new TraitDisplay(sidePanel, myFrame);
        sidePanel.add(traitDisplay);
        sidePanel.validate();
    }

    public void setupMidPanel() {
        midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.Y_AXIS));
        midPanel.setPreferredSize(new Dimension(500, 200));

        TitledBorder titleMidPanel;
        titleMidPanel = BorderFactory.createTitledBorder("Preview Traits");
        midPanel.setBorder(titleMidPanel);
        labelPanel = new LabelPanel();
        TitledBorder titleLabelPanel;
        titleLabelPanel = BorderFactory.createTitledBorder("Set Labels");
        labelPanel.setBorder(titleLabelPanel);
        traitPreview = new TraitPreview(myParent.plural(), traitDisplay, labelPanel, myFrame);
        //labelPanel.initiComponents();

        midPanel.add(traitPreview);
        midPanel.add(labelPanel);
        traitPreview.setTraits(myParent.getTraits());

        //midPanel.add(traitPreview);
        traitPreview.showMe();

        labelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        traitPreview.setAlignmentX(Component.LEFT_ALIGNMENT);

        midPanel.validate();
    }

    public void setupBottomPanel() {
        GroupLayout layout = new GroupLayout(bottomPanel);
        bottomPanel.setLayout(layout);

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                .addGap(10)
                .addGroup(layout.createParallelGroup()
                .addComponent(cancelButton)
                .addComponent(okayButton))
        );

        layout.setHorizontalGroup(
                layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                        .addGap(165)
                        .addComponent(cancelButton)
                        .addGap(10)
                        .addComponent(okayButton))
        );
        layout.linkSize(SwingConstants.HORIZONTAL, cancelButton, okayButton);
        validate();
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

    public JButton getCancelButton() {
        return cancelButton;
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

    public HashMap<String, TraitState> getTraitStateMap() {
        return traitPreview.getTraitStateMap();
    }

    public TraitPreview getTraitPreview() {
        return traitPreview;
    }

}
