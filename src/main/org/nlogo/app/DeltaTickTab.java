package org.nlogo.app;

import org.nlogo.agent.Observer;
import org.nlogo.api.SimpleJobOwner;
import org.nlogo.api.CompilerException;
import org.nlogo.deltatick.*;
import org.nlogo.deltatick.PopupMenu;
import org.nlogo.deltatick.dialogs.*;
import org.nlogo.deltatick.dnd.*;
import org.nlogo.deltatick.xml.*;
import org.nlogo.widget.NoteWidget;
import org.nlogo.window.*;


import org.nlogo.plot.PlotPen;
import org.nlogo.plot.PlotManager;

// java.awt contains all of the classes for creating user interfaces and for painting graphics and images -A. (sept 8)
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: mwilkerson
 * Date: Feb 17, 2010
 * Time: 10:24:15 PM
 * To change this template use File | Settings | File Templates.
 */
//TODO set the window to a larger size -A. 
public class DeltaTickTab
	extends javax.swing.JPanel
    implements Events.SwitchedTabsEvent.Handler {

    //final means cannot be overriden and can only be initialized once -A. (sept 8)
    //toolBar is an object of class org.nlogo.swing.Toolbar -A. (sept8)
    final org.nlogo.swing.ToolBar toolBar;


    // breedTypeSelector is an object, BreedTypeSelector is a class -A. (sept 8)
    //an object is an instantiation of a class -A. (sept 8)
    BreedTypeSelector breedTypeSelector;
    TraitSelectorOld traitSelector;
    TraitTypeSelectorOld traitTypeSelector;
    VariationSelector variationSelector;
    EnvtTypeSelector envtTypeSelector;
    OperatorBlockBuilder obBuilder;
    UserInput userInput = new UserInput();

    JSeparator separator = new JSeparator();
    JSeparator librarySeparator = new JSeparator();
    JPanel contentPanel = new JPanel();
    JPanel libraryPanel;
    BuildPanel buildPanel;
    SpeciesInspectorPanel speciesInspectorPanel;
    HashMap<BreedBlock, SpeciesInspectorPanel> speciesInspectorPanelMap = new HashMap<BreedBlock, SpeciesInspectorPanel>();


    LibraryHolder libraryHolder;

    JButton addBreed;
    JButton addPlot;
    JButton addHisto;
    JButton addTrackSpecies;
    JButton saveModelButton;
    JButton openModelButton;
    PopupMenu popup;

    //JButton addEnvt;
    JButton buildBlock;
    JButton Not;

    //JFileChooser jFileChooser;

    boolean plotsAlive = false;

    int count;   // to make sure tabbedpane doesn't get created more than once (Feb 23, 2012)
    int interfaceCount; // to make sure setup and go button doesn't get created more than once (May 13, 2012)
    int interfacePlotCount;
    int interfaceHistoCount; // to make sure extra histos are not added (Jan 15, 2013)
    int interfaceGraphCount;
    int interfaceNoteCount; //to make sure mutation note is created just once (March 29, 2013)


    HashMap<String, WidgetWrapper> plotWrappers = new HashMap<String, WidgetWrapper>();
    HashMap<String, WidgetWrapper> sliderWidgets = new HashMap<String, WidgetWrapper>();
    HashMap<String, WidgetWrapper> noteWidgets = new HashMap<String, WidgetWrapper>();

    //InterfaceTab it;
    ProceduresTab pt;
    GUIWorkspace workspace;
    InterfacePanel interfacePanel;

    DeltaTickTab deltaTickTab = this;
    PlotManager plotManager;

    LibraryReader libraryReader;
    DeltaTickModelReader deltaTickModelParser;

    public final SimpleJobOwner defaultOwner ;

    //constructor -A. (sept 8)
    public DeltaTickTab( GUIWorkspace workspace , ProceduresTab pt, InterfacePanel interfacePanel) {
        this.workspace = workspace;
        this.interfacePanel = interfacePanel;
        this.pt = pt;


        this.plotManager = workspace.plotManager();
        
        this.breedTypeSelector = new BreedTypeSelector(workspace.getFrame());
        //this.it = it;
        this.traitSelector = new TraitSelectorOld( workspace.getFrame() );
        this.traitTypeSelector = new TraitTypeSelectorOld(workspace.getFrame());
        this.variationSelector = new VariationSelector(workspace.getFrame());
        this.envtTypeSelector = new EnvtTypeSelector(workspace.getFrame());
        this.obBuilder = new OperatorBlockBuilder(workspace.getFrame());
        obBuilder.setMyParent(this);

        defaultOwner =
          new SimpleJobOwner("DeltaTick Runner", workspace.world.mainRNG,
                             Observer.class);
        //creates new ToolBar - method declared below -A. (sept 8)
        toolBar = getToolBar();

        setLayout( new java.awt.BorderLayout() ) ;
		add( toolBar , java.awt.BorderLayout.NORTH ) ;
        add( contentPanel, java.awt.BorderLayout.CENTER );

        // Instantiate the DeltaTickModelParser
        deltaTickModelParser = new DeltaTickModelReader(workspace.getFrame(), this);

        //actually instantiates the object, declaration above does not instantiate until the constructor is executed
        //-A. (sept 8)

        libraryPanel = new JPanel();
        //libraryPanel.setLayout(new GridLayout(10,1));        // (int rows, int columns)
        libraryPanel.setLayout( new BoxLayout (libraryPanel, BoxLayout.Y_AXIS));

        //second line is making the entire buildPanel ready for stuff to be dragged -A. (sept 8)
        buildPanel = new BuildPanel( workspace );
        new BuildPanelDragSource(buildPanel);

        separator.setOrientation(SwingConstants.VERTICAL);

        java.awt.GridBagConstraints gridBagConstraints;

        contentPanel.setLayout(new java.awt.GridBagLayout());   // one row, columns expand

        //librarySeparator.setOrientation(SwingConstants.HORIZONTAL);
        // Fill: used when the component's display area is larger than the
        // component's requested size. It determines whether to resize the component,
        // and if so, how.  -A. (sept 8)
        //# HORIZONTAL: Make component wide enough to fill its display area horizontally, not change height
        // VERTICAL: '' tall enough to fill its display area vertically, not change its width.
        // BOTH: Make the component fill its display area entirely.

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1;
        contentPanel.add(buildPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weightx = 0;
        gridBagConstraints.weighty = 1.0;
        contentPanel.add(separator, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        contentPanel.add(libraryPanel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.VERTICAL;

        buildPanel.addRect("Click on Load behavior library to get started!");

        //contentPanel.pack();

        count = 0;
        interfaceCount = 0;
        interfacePlotCount = 0;
        interfaceHistoCount = 0;
        interfaceGraphCount = 0; //to combine plots and histos into one
    }


    public void addCondition( ConditionBlock cBlock ) {
        new ConditionDropTarget(cBlock);
        //new PlantedCodeBlockDragSource(cBlock);
    }

    public void addDragSource( CodeBlock block ) {
        if (block instanceof TraitBlock) {
        }
        new CodeBlockDragSource( block );
    }


    public void addTrait( TraitBlock tBlock ) {
        new TraitDropTarget(tBlock);
        //tBlock.numberAgents();
    }

    public void addOperator ( OperatorBlock oBlock ) {
        new OperatorDropTarget (oBlock);
    }

    private final javax.swing.Action loadAction =
		new javax.swing.AbstractAction( "Load Behavior Library" ) {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
                openLibrary(null);
            }
        };

    public void openLibrary(String  fileName) {
        if ( count == 0 ) {
            libraryHolder = new LibraryHolder();
            libraryPanel.add(libraryHolder);
            libraryHolder.makeNewTab();

            libraryReader = new LibraryReader( workspace.getFrame() , deltaTickTab, fileName );
            libraryHolder.setTabName( buildPanel.getBgInfo().getLibrary() );
            addPlot.setEnabled(true);
            addHisto.setEnabled(true);
            addBreed.setEnabled(true);
            buildPanel.removeRect();
            if (buildPanel.getMyBreeds().size() == 0) {
                buildPanel.addRect("Click Add species to start building your model!");
                buildPanel.repaint();
                buildPanel.validate();
            }
            deltaTickTab.contentPanel.validate();
            count ++;
        }
        else if (count > 0 ) {
            //TODO: Will eventually need a list of all libraries that are open (March 30, 2013)
            libraryHolder.makeNewTab();
            libraryReader = new LibraryReader( workspace.getFrame(), deltaTickTab, null );
            libraryHolder.setTabName( buildPanel.getBgInfo().getLibrary() );
            deltaTickTab.contentPanel.validate();
        }
    }

    
    private final javax.swing.Action addBreedAction =
		new javax.swing.AbstractAction( "Add Species" ) {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
                makeBreedBlock(null, null);

                // if more than 1 breed available in XML -A. (oct 5)
                //Commented this out because I'm not using breedTypeSelector anymore -Aditi (March 31, 2013)
//                if( buildPanel.availBreeds().size() > 1 ) {
//                    breedTypeSelector.showMe(buildPanel.getBgInfo());
//                    if (breedTypeSelector.typedBreedType() != null) {
//                        Breed breed = buildPanel.getBgInfo().getBreeds().get(0);
//                        newBreed = new BreedBlock( breed, breedTypeSelector.typedBreedType(), workspace.getFrame() );
//                        buildPanel.addBreed(newBreed);
//                        userInput.addBreed(newBreed.plural());
//                        newBreed.getParent().setComponentZOrder(newBreed, 0 );
//                        new BreedDropTarget(newBreed, deltaTickTab);
//                        newBreed.inspectSpeciesButton.addActionListener(new SpeciesButtonListener(newBreed));
//                    }
//                }
//                    else if( breedTypeSelector.selectedBreedType() != null ) {
//
//                        for( Breed breed : buildPanel.getBgInfo().getBreeds() ) {
//                            if (breed.plural()  == breedTypeSelector.selectedBreedType()) {
//                                newBreed = new BreedBlock( breed, breed.plural(), workspace.getFrame() );
//
//                                buildPanel.addBreed(newBreed);
//                                userInput.addBreed(newBreed.plural());
//                                newBreed.getParent().setComponentZOrder(newBreed, 0 );
//                                new BreedDropTarget(newBreed, deltaTickTab);
//                                newBreed.inspectSpeciesButton.addActionListener(new SpeciesButtonListener(newBreed));
//                            }
//                        }
//                } else {


            }
                //newBreed.inspectSpeciesButton.addActionListener(new SpeciesButtonListener(newBreed));
            //}
      };

    public BreedBlock makeBreedBlock(String plural, String setupNumber) {
        BreedBlock newBreed = new BreedBlock();
        buildPanel.removeRect();
        Breed breed = buildPanel.availBreeds().get(0);

        if( buildPanel.breedCount() == 0 ) {
            newBreed = new BreedBlock( breed , breed.plural(), workspace.getFrame() );
        } else {
            newBreed = new BreedBlock( breed , breed.plural() + buildPanel.breedCount(), workspace.getFrame() );
        }

        // Create speciesinspectorpanel for the breedblock
        JFrame jFrame = new JFrame("Species Inspector");
        speciesInspectorPanel = new SpeciesInspectorPanel(newBreed, jFrame);

        speciesInspectorPanel.addPanels(jFrame.getContentPane());
        newBreed.setHasSpeciesInspector(true);
        jFrame.setResizable(true);
        jFrame.pack();
        jFrame.setVisible(false);

        // Add action listeners here because the action listener is a member class of deltaticktab and not accessible in breedblock
        speciesInspectorPanel.getOkayButton().addActionListener(new SpeciesPanelOkayListener(newBreed));
        speciesInspectorPanel.getCancelButton().addActionListener(new SpeciesPanelCancelListener(newBreed));
        // Put the speciesinspectorpanel in the map
        speciesInspectorPanelMap.put(newBreed, speciesInspectorPanel);

        buildPanel.addBreed(newBreed);
        userInput.addBreed(newBreed.plural());
        newBreed.getParent().setComponentZOrder(newBreed, 0 );
        new BreedDropTarget(newBreed, deltaTickTab);
        newBreed.inspectSpeciesButton.addActionListener(new SpeciesButtonListener(newBreed));

        if (plural != null) {
            newBreed.setPlural(plural);
        }
        if (setupNumber != null) {
            newBreed.setNumber(setupNumber);
        }
        contentPanel.validate();
        return newBreed;
    }


    class SpeciesButtonListener implements ActionListener {
        BreedBlock myParent;
        JFrame jFrame;

        SpeciesButtonListener(BreedBlock myParent) {
            this.myParent = myParent;
        }

        public void actionPerformed(ActionEvent e) {
            speciesInspectorPanel = speciesInspectorPanelMap.get(myParent);
            speciesInspectorPanel.updateText();
            speciesInspectorPanel.getMyFrame().setVisible(true);
            speciesInspectorPanel.updateTraitDisplay();
        }
    }

    public class SpeciesPanelOkayListener implements ActionListener {
        BreedBlock myParent;

        SpeciesPanelOkayListener(BreedBlock myParent) {
            this.myParent = myParent;
        }

        public void actionPerformed(ActionEvent e) {
            //SpeciesInspectorPanel
            speciesInspectorPanel = speciesInspectorPanelMap.get(myParent);

            myParent.setMaxAge(speciesInspectorPanel.getEndListSpan());
            myParent.setMaxEnergy(speciesInspectorPanel.getHighestEnergy());
            speciesInspectorPanel.getMyFrame().setVisible(false);

            ArrayList<TraitBlockNew> removeBlocks = new ArrayList<TraitBlockNew>();
            for (Trait trait: speciesInspectorPanel.getSpeciesInspector().getSelectedTraitsList()) {
                for (TraitBlockNew tBlock : buildPanel.getMyTraits()) {
                    if (speciesInspectorPanel.getMyParent().plural().equalsIgnoreCase(tBlock.getBreedName())
                            && (trait.getNameTrait().equalsIgnoreCase(tBlock.getName()))) {
                        removeBlocks.add(tBlock);
                    }
                }
            }

            for (TraitBlockNew tBlock : removeBlocks) {
                libraryHolder.removeTraitBlock(tBlock);
                buildPanel.removeTrait(tBlock);
                userInput.removeTrait(tBlock.getBreedName(), tBlock.getTraitName());
                speciesInspectorPanel.getSpeciesInspector().removeTrait(tBlock.getTraitName());
                //myParent.removeTraitBlockFromList(tBlock);
                myParent.removeAllTraitBlocks();
            }

            for (TraitState traitState : speciesInspectorPanel.getTraitStateMap().values()) {
                makeTraitBlock(myParent, traitState);
            }

            myParent.getTraitLabels().clear();
            for (Map.Entry<String, JCheckBox> map : speciesInspectorPanel.getTraitPreview().getLabelPanel().getCheckBoxes().entrySet()) {
                String trait = map.getKey();
                JCheckBox checkBox = map.getValue();
                if (checkBox.isSelected()) {
                    myParent.addToTraitLabels(trait);
                }
            }
                //TODO: this is a hard-coded hack because "trait" becomes null. Fix it -Aditi (Feb 22, 2013)
        }
    }

    public void makeTraitBlock(BreedBlock bBlock, TraitState traitState) {
        TraitBlockNew traitBlock;
        traitBlock = new TraitBlockNew(bBlock, traitState, traitState.getVariationHashMap(), traitState.getVariationsValuesList());
        traitBlock.setMyParent(bBlock);

        speciesInspectorPanel.getSpeciesInspector().addToSelectedTraitsList(traitState);
        userInput.addTraitAndVariations(bBlock.getName(), traitState.getNameTrait(), traitState.getVariationsList());
        buildPanel.addTrait(traitBlock);
        libraryHolder.addTraittoTab(traitBlock, buildPanel.getMyTraits().size());
        deltaTickTab.addDragSource(traitBlock);
        bBlock.addTraitBlocktoList(traitBlock);

        contentPanel.validate();
    }

    public SpeciesInspectorPanel getSpeciesInspectorPanel(BreedBlock bBlock) {
        return speciesInspectorPanelMap.get(bBlock);
    }

    public class SpeciesPanelCancelListener implements ActionListener {
        BreedBlock myParent;

        SpeciesPanelCancelListener(BreedBlock myParent) {
            this.myParent = myParent;
        }

        public void actionPerformed(ActionEvent e) {
            //SpeciesInspectorPanel
                    speciesInspectorPanel = speciesInspectorPanelMap.get(myParent);
            speciesInspectorPanel.getMyFrame().setVisible(false);
        }
    }

    private final javax.swing.Action addTrackSpeciesAction =
		new javax.swing.AbstractAction( "Track change in species" ) {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
                popup = new PopupMenu();

            }
        };


    private final javax.swing.Action addPlotAction =
		new javax.swing.AbstractAction( "Add Line Graph" ) {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
                PlotBlock newPlot = new PlotBlock(false);
                buildPanel.addPlot( newPlot );
                newPlot.getParent().setComponentZOrder(newPlot, 0 );
                new PlotDropTarget(newPlot);
                contentPanel.validate();
        }
    };

    private final javax.swing.Action clearAction =
		new javax.swing.AbstractAction( "Clear" ) {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
                buildPanel.clear();
                addBreed.setEnabled(true);
                clearPlots();
                variationSelector.getVariationList().clear();
                for (TraitBlockNew tBlock : libraryHolder.getTraitBlocks()) {
                    libraryHolder.removeTraitBlock(tBlock);
                }
                contentPanel.repaint();
        }
    };

    private final javax.swing.Action chgEnvtAction =
		new javax.swing.AbstractAction( "Add environment" ) {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
                envtTypeSelector.showMe(buildPanel.getBgInfo());
                EnvtBlock newEnvt;
                for ( Envt envt: buildPanel.getBgInfo().getEnvts() ) {
                    if ( envtTypeSelector.selectedEnvt() != null ) {
                        if ( envtTypeSelector.selectedEnvt() == envt.nameEnvt()) {
                            newEnvt = new EnvtBlock (envt);
                            new EnvtDropTarget( newEnvt, deltaTickTab );
                            buildPanel.addEnvt(newEnvt);
                        }
                        contentPanel.validate();
                    };
                };
            }
        };

    private final javax.swing.Action addHistoAction =
		new javax.swing.AbstractAction( "Add Histogram" ) {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
                PlotBlock hBlock = new PlotBlock(true);
                buildPanel.addPlot( hBlock );
                hBlock.getParent().setComponentZOrder(hBlock, 0 );
                new HistoDropTarget(hBlock);
                contentPanel.validate();
        }
    };

    private final Action toBuildBlock =
            new javax.swing.AbstractAction( "Build operator block" ) {
                public void actionPerformed ( ActionEvent e ) {
                    OperatorBlock newOBlock;
                    obBuilder.showMe(userInput);
                    if ( obBuilder.check()  == true ) {
                        newOBlock = new OperatorBlock( obBuilder.selectedBreed(), obBuilder.selectedTrait(),
                                obBuilder.selectedTrait2(),
                                userInput.getVariations(obBuilder.selectedBreed(), obBuilder.selectedTrait()),
                                userInput.getVariations(obBuilder.selectedBreed(), obBuilder.selectedTrait2()));
                        libraryHolder.addOperatortoTab(newOBlock);
                        deltaTickTab.addDragSource(newOBlock);
                    }
                }
            };

    public class XMLFilter extends FileFilter {
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = getExtension(f);
            if (extension != null) {
                if (extension.equalsIgnoreCase("xml")) {
                    return true;
                }
                else {
                    return false;
                }
            }

            return false;
        }

        public String getDescription() {
            return "XML File filter";
        }

        public String getExtension(File f) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');

            if (i > 0 &&  i < s.length() - 1) {
                ext = s.substring(i+1).toLowerCase();
            }
            return ext;
        }
    }

    private final Action opensaveModelAction =
            new AbstractAction() {
                JFileChooser fileChooser = new JFileChooser();

                public void actionPerformed (ActionEvent e) {
                    fileChooser.addChoosableFileFilter(new XMLFilter());
                    fileChooser.setAcceptAllFileFilterUsed(false);

                    if (e.getSource() == openModelButton) {
                        int returnVal = fileChooser.showOpenDialog(DeltaTickTab.this);

                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = fileChooser.getSelectedFile();
                            openModel(file);
                        }

                        //Handle save button action.
                    } else if (e.getSource() == saveModelButton) {
                        int returnVal = fileChooser.showSaveDialog(DeltaTickTab.this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = fileChooser.getSelectedFile();
                            saveModel(file);
                        }
                    }
                }
    };

    public void saveModel(File modelFile) {
        //System.out.println("del " + buildPanel.newSaveAsXML());
        deltaTickModelParser.saveModel(modelFile);

    }

    public void openModel(File modelFile) {
        deltaTickModelParser.openModel(modelFile);
        //DeltaTickModelReader modelReader = new DeltaTickModelReader( workspace.getFrame(), this , file);
    }

    public void populateProcedures() {
        pt.innerSource( buildPanel.unPackAsCode() );
        //pt.innerSource( libraryHolder.unPackAsCode() );
        //pt.select(0,pt.innerSource().length());
        pt.setIndenter(true);
    }

    public void populateInterface() {
        if (interfaceCount == 0) {
        org.nlogo.window.Widget setupWidget = interfacePanel.makeWidget("BUTTON",false);
        interfacePanel.addWidget(setupWidget, 0, 0, true, false);
        if (setupWidget instanceof org.nlogo.window.ButtonWidget) {
          org.nlogo.window.ButtonWidget button =
              (org.nlogo.window.ButtonWidget) setupWidget;
            button.displayName("setup");
            button.wrapSource("setup");
        }

        org.nlogo.window.Widget goWidget = interfacePanel.makeWidget("BUTTON",false);
        interfacePanel.addWidget(goWidget, 60, 0, true, false);

        if (goWidget instanceof org.nlogo.window.ButtonWidget) {
          org.nlogo.window.ButtonWidget button =
              (org.nlogo.window.ButtonWidget) goWidget;
            button.displayName("go");
            button.wrapSource("go");
            button.setForeverOn();
        }


            //Commented out because I don't want "draw" and "envtChooser" any more -Aditi (March 9, 2013)
//        org.nlogo.window.Widget drawWidget = interfacePanel.makeWidget("BUTTON",false);
//            interfacePanel.addWidget(drawWidget, 0, 130, true, false);
//            if (drawWidget instanceof org.nlogo.window.ButtonWidget) {
//                org.nlogo.window.ButtonWidget button =
//                    (org.nlogo.window.ButtonWidget) drawWidget;
//                button.displayName("draw");
//                button.wrapSource("draw");
//                button.setForeverOn();
//
//        }
//
//        org.nlogo.window.Widget envtChooserWidget = interfacePanel.makeWidget("CHOOSER",false);
//        interfacePanel.addWidget(envtChooserWidget, 0, 100, true, false);
//        //org.nlogo.window.ButtonWidget buttonWidget = interface
//        if (envtChooserWidget instanceof org.nlogo.window.ChooserWidget) {
//          org.nlogo.window.ChooserWidget chooser =
//              (org.nlogo.window.ChooserWidget) envtChooserWidget;
//            chooser.displayName("environment");
//            chooser.nameWrapper("environment");
//            LogoListBuilder choicesList = new LogoListBuilder();
//            choicesList.add("grass");
//            choicesList.add("water");
//            chooser.setChoices(choicesList.toLogoList());
//
//        }
            interfaceCount++;
        }
    }


    public void populateMutationSlider() {
        boolean putNoteWidget = false;
        for (BreedBlock bBlock : buildPanel.getMyBreeds()) {
            if (bBlock.getReproduceUsed() && buildPanel.getMyTraits().size() > 0) {
                putNoteWidget = true;
                for (TraitBlockNew tBlock : bBlock.getMyTraitBlocks()) {
                    SliderWidget sliderWidget = ((SliderWidget) interfacePanel.makeWidget("SLIDER", false));

                    System.out.println("deltatick max " + sliderWidget.maximum() + " " + sliderWidget.increment());
                    System.out.println("deltatick min " + sliderWidget.minimum());
                    WidgetWrapper ww = interfacePanel.addWidget(sliderWidget, 0, 120, true, false);
                    String sliderName = tBlock.getMyParent().plural() + "-" + tBlock.getTraitName() + "-mutation";
                    sliderWidget.name_$eq(sliderName);
                    sliderWidget.validate();
                    sliderWidgets.put(sliderName, ww);
                }

            }
            interfaceNoteCount++;
            revalidate();
        }
        //make a note only once (March 29, 2013)
        if (putNoteWidget) {
            NoteWidget noteWidget = ((NoteWidget) interfacePanel.makeWidget("NOTE", false));
            WidgetWrapper widgetw = interfacePanel.addWidget(noteWidget, 0, 80, true, false);
            String note = "Chance of mutation when a baby is born";
            noteWidget.setBounds(0, 80, 20, 30);
            noteWidget.text_$eq(note);
            noteWidget.validate();
            noteWidgets.put("MutationNote", widgetw);
        }
    }

    public void removeMutationSlider() {
        for (Map.Entry<String, WidgetWrapper> entry : sliderWidgets.entrySet()) {
            String p = entry.getKey();
            WidgetWrapper w = entry.getValue();
            sliderWidgets.remove(w);
            interfacePanel.removeWidget(w);
        }
        for (Map.Entry<String, WidgetWrapper> entry : noteWidgets.entrySet()) {
            WidgetWrapper w = entry.getValue();

            noteWidgets.remove(w);
            interfacePanel.removeWidget(w);
        }
        revalidate();
    }


    public void populatePlots() {
        try {
           for (PlotBlock plotBlock : buildPanel.getMyPlots().subList(interfacePlotCount, buildPanel.getMyPlots().size())) {
               org.nlogo.window.Widget plotWidget = interfacePanel.makeWidget("Plot", false);
               //WidgetWrapper ww = interfacePanel.addWidget(plotWidget, 660 + (((int) interfacePlotCount/3) * 200), 10 + ((interfacePlotCount%3)*160), true, false);
               WidgetWrapper ww = interfacePanel.addWidget(plotWidget, 660 + ((interfacePlotCount/3) * 200), 10 + ((interfacePlotCount%3)*160), true, false);
               plotWidget.displayName(plotBlock.getName());
               org.nlogo.plot.Plot newPlot = workspace.plotManager().getPlot("plot " + (interfacePlotCount + 1));
               plotWrappers.put("plot " + (interfacePlotCount + 1), ww);
               interfacePlotCount++;

               for (QuantityBlock quantBlock : plotBlock.getMyBlocks()) {
                            if (newPlot.getPen(quantBlock.getName()).toString().equals("None")) {
                                // PlotPen plotPen = newPlot.createPlotPen(quantBlock.getName(), false); // commented 20130319
                                PlotPen plotPen = newPlot.createPlotPen(quantBlock.getPenName(), false);
                                plotPen.updateCode(quantBlock.getPenUpdateCode());
                            }
               }
               //interfacePlotCount++;
           }

            for (PlotBlock plotBlock : buildPanel.getMyPlots()) {
                for (QuantityBlock qBlock : plotBlock.getMyBlocks()) {
                    if (workspace.plotManager().getPlot(plotBlock.getName()).getPen(qBlock.getName()).toString().equals("None")) {
                        PlotPen newPlotPen = workspace.plotManager().getPlot(plotBlock.getName()).createPlotPen(qBlock.getName(), false);
                        newPlotPen.updateCode(qBlock.getPenUpdateCode());
                        //newPlotPen._hidden = true;
                    }
                }
            }
            for (HistogramBlock hBlock : buildPanel.getMyHisto().subList(interfaceHistoCount, buildPanel.getMyHisto().size())) {
                org.nlogo.window.Widget plotWidget = interfacePanel.makeWidget("Plot", false);
                interfacePanel.addWidget(plotWidget, 5, 50, true, false);
                plotWidget.displayName(hBlock.getName());

                org.nlogo.plot.Plot newPlot = workspace.plotManager().getPlot("plot 1");
                PlotPen plotPen = newPlot.getPen("default").get();
                for (QuantityBlock quantBlock : hBlock.getMyBlocks()) {
                    //String penName = "";
                    plotPen.updateCode(quantBlock.getPenUpdateCode());
                }
                interfaceHistoCount++;
            }
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        revalidate();
    }

    public void removePlots() {

        try {
            for (Map.Entry<String, WidgetWrapper> entry : plotWrappers.entrySet()) {
                String p = entry.getKey();
                WidgetWrapper w = entry.getValue();
                if (buildPanel.getMyPlot(p) == false) {
                    interfacePanel.removeWidget(w);
                    interfacePlotCount--;
                    plotWrappers.remove(p);
                    workspace.plotManager().forgetPlot(workspace.plotManager().getPlot(p));
                }
            }
        }
        catch ( Exception ex ) {
            System.out.println(ex.getMessage());
        }
        revalidate();
    }

    public void removePlotPens() {

    }

    // ordering of the buttons on the deltatick ToolBar -A. (sept 8)
    org.nlogo.swing.ToolBar getToolBar() {
		return new org.nlogo.swing.ToolBar() {
            @Override
            public void addControls() {
                this.add( new JButton( loadAction ) ) ;
                this.add( new org.nlogo.swing.ToolBar.Separator() ) ;
                //this.add( new JButton( loadAction2 ) );
                addBreed = new JButton( addBreedAction );
                addBreed.setEnabled(false);
                this.add(addBreed) ;
                addPlot = new JButton( addPlotAction );
                addPlot.setEnabled(false);
                this.add(addPlot) ;
                addHisto = new JButton( addHistoAction );
                addHisto.setEnabled(false);
                this.add(addHisto) ;
                //addEnvt = new JButton ( chgEnvtAction );
                //addEnvt.setEnabled(false);
                //this.add(addEnvt) ;
                this.add( new org.nlogo.swing.ToolBar.Separator() ) ;
                saveModelButton = new JButton();
                saveModelButton.setAction(opensaveModelAction);
                saveModelButton.setText("Save Model");
                this.add(saveModelButton);
                openModelButton = new JButton();
                openModelButton.setAction(opensaveModelAction);
                openModelButton.setText("Open Model");
                this.add(openModelButton);
                //buildBlock = new JButton( toBuildBlock );
                //this.add( buildBlock );
                this.add( new org.nlogo.swing.ToolBar.Separator() ) ;
                this.add( new JButton( clearAction ) ) ;
                //this.add( new JButton( procedureAction ) ) ;
            }
        } ;
	}


    //this method populates the library panel with all the blocks from the XML (read in Library Reader) except traitBlock
    //(Feb 16, 2012)
    public JPanel getLibraryPanel() {
        return libraryPanel;
    }

    public BuildPanel getBuildPanel() {
        return buildPanel;
    }

    public LibraryHolder getLibraryHolder() {
        return libraryHolder;
    }

    public TraitSelectorOld getTraitSelector() {
        return traitSelector;
    }

    public VariationSelector getVariationSelector() {
        return variationSelector;
    }

    // this procedure might be the one responsible to updating code everytime tab is switched - A (May 4)
    public void handle( Events.SwitchedTabsEvent event ) {
        if( event.oldTab == this ) {
            populateProcedures();
            pt.setIndenter(true);
            pt.select(0, pt.innerSource().length() );
            // pt.getIndenter().handleTab();
            pt.select(0,0);
            populateInterface();
            removePlots();
            populatePlots();
            removeMutationSlider();
            populateMutationSlider();
            new org.nlogo.window.Events.CompileAllEvent()
				.raise( DeltaTickTab.this ) ;
        }
    }

    public void run( String command ) {
        try {
            workspace.evaluateCommands(defaultOwner, command);
        } catch (CompilerException e) {
            throw new RuntimeException(e);  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void go() {
        run("go");
    }

    public void setup() {
        run("setup");
    }

    public void clearLibrary() {
        libraryPanel.removeAll();
        buildPanel.clear();
        clearPlots();
        buildPanel.getBgInfo().clear();
        libraryHolder.removeAll();
        //breedTypeSelector.clear();
        libraryPanel.repaint();
    }


    public void clearPlots() {
        workspace.plotManager().forgetAll();
    }

    public void setPlotsAlive( boolean alive ) {
        plotsAlive = alive;
    }

    public String toXml() {
        return buildPanel.saveAsXML();
    }

    public void load( String theXml ) {
        //TODO...

    }

    public String libraryName() {
        return buildPanel.getBgInfo().getLibrary();
    }

    public LibraryReader getLibraryReader() {
        return this.libraryReader;
    }



class PopupListener extends MouseAdapter {
    JPopupMenu popup;
    JMenuItem menuItem;

    PopupListener(JPopupMenu popupMenu) {
        popup = popupMenu;
        menuItem = new JMenuItem("ice");
        popup.add(menuItem);
        JPanel panel = new JPanel();
        panel.add(popup);

    }

      public void mousePressed(MouseEvent ev) {
        if (ev.isPopupTrigger()) {
          popup.show(ev.getComponent(), ev.getX(), ev.getY());
        }
      }

      public void mouseReleased(MouseEvent ev) {
        if (ev.isPopupTrigger()) {
          popup.show(ev.getComponent(), ev.getX(), ev.getY());
        }
      }

      public void mouseClicked(MouseEvent ev) {
      }
    }
}


