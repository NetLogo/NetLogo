package org.nlogo.app;

import apple.laf.JRSUIConstants;
import ch.randelshofer.quaqua.QuaquaComboPopup;
import com.sun.xml.internal.bind.v2.model.core.MaybeElement;
import org.nlogo.agent.Observer;
import org.nlogo.api.LogoList;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.SimpleJobOwner;
import org.nlogo.api.CompilerException;
import org.nlogo.deltatick.*;
import org.nlogo.deltatick.dialogs.*;
import org.nlogo.deltatick.dnd.*;
import org.nlogo.deltatick.xml.*;
import org.nlogo.window.*;

import org.nlogo.plot.Plot;
import org.nlogo.plot.PlotPen;
import org.nlogo.plot.PlotManager;

// java.awt contains all of the classes for creating user interfaces and for painting graphics and images -A. (sept 8)
import javax.accessibility.Accessible;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
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
    TraitSelector traitSelector;
    TraitTypeSelector traitTypeSelector;
    VariationSelector variationSelector;
    EnvtTypeSelector envtTypeSelector;
    OperatorBlockBuilder obBuilder;
    UserInput userInput = new UserInput();

    JSeparator separator = new JSeparator();
    JSeparator librarySeparator = new JSeparator();
    JPanel contentPanel = new JPanel();
    JPanel libraryPanel;
    BuildPanel buildPanel;

    JPanel libraryPanel2;

    LibraryHolder libraryHolder;

    JButton addBreed;
    JButton addTraits;
    JButton addPlot;
    JButton addHisto;
    JButton chgEnvt;
    JButton buildBlock;
    JButton Not;

    boolean plotsAlive = false;

    int count;   // to make sure tabbedpane doesn't get created more than once (Feb 23, 2012)
    int interfaceCount; // to make sure setup and go button doesn't get created more than once (May 13, 2012)
    int interfacePlotCount;
    int interfaceHistoCount; // to make sure extra histos are not added (Jan 15, 2013)
    int interfaceGraphCount;


    HashMap<String, WidgetWrapper> plotWrappers = new HashMap<String, WidgetWrapper>();

    //InterfaceTab it;
    ProceduresTab pt;
    GUIWorkspace workspace;
    InterfacePanel interfacePanel;

    DeltaTickTab deltaTickTab = this;
    PlotManager plotManager;

    public final SimpleJobOwner defaultOwner ;

    //constructor -A. (sept 8)
    public DeltaTickTab( GUIWorkspace workspace , ProceduresTab pt, InterfacePanel interfacePanel) {
        this.workspace = workspace;
        this.pt = pt;
        this.interfacePanel = interfacePanel;

        this.plotManager = workspace.plotManager();
        
        this.breedTypeSelector = new BreedTypeSelector(workspace.getFrame());
        //this.it = it;
        this.traitSelector = new TraitSelector( workspace.getFrame() );
        this.traitTypeSelector = new TraitTypeSelector(workspace.getFrame());
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

        //actually instantiates the object, declaration above does not instantiate until the constructor is executed
        //-A. (sept 8)

        libraryPanel = new JPanel();
        //libraryPanel.setLayout(new GridLayout(10,1));        // (int rows, int columns)
        libraryPanel.setLayout( new BoxLayout (libraryPanel, BoxLayout.Y_AXIS));


       // jf.setPreferredSize(new Dimension(300, 300));
        //jf.setVisible(true);


        libraryPanel2 = new JPanel();
        libraryPanel2.setLayout(new GridLayout(3, 1));

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


        // TODO: Figure out separator between two libraries
        /*
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        //gridBagConstraints.weightx = 1.0;
        //gridBagConstraints.weighty = 0;
        contentPanel.add(librarySeparator, gridBagConstraints);
        */

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.VERTICAL;
        contentPanel.add(libraryPanel2);
        libraryPanel2.setForeground(Color.green);
        libraryPanel2.setVisible(true);

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
            //((TraitBlock) block).getDropDownList().requestFocus(false);
            //((TraitBlock) block).getDropDownList().getUI().isFocusTraversable();
            // ((TraitBlock) block).getDropDownList().getUI().isFocusTraversable(((TraitBlock) block).getDropDownList());
           // JComboBox comboBox = ((TraitBlock) block).getDropDownList();
            //QuaquaComboPopup popup = (QuaquaComboPopup) comboBox.getAccessibleContext().getAccessibleChild(0);
            //popup.requestFocus(false);
            //popup.setBorder(null);
            //int i = 3;
            //System.out.println("dragging");


        }
        new CodeBlockDragSource( block );

    }


    public void addTrait( TraitBlock tBlock ) {
        new TraitDropTarget(tBlock);
        tBlock.numberAgents();
    }


    public void addOperator ( OperatorBlock oBlock ) {
        new OperatorDropTarget (oBlock);
    }

    /*
    public void addOperator (OperatorBlock oBlock) {
        new OperatorDropTarget(oBlock);
        buildPanel.addOperatorBlock(oBlock);
    }
    */



    private final javax.swing.Action loadAction =
		new javax.swing.AbstractAction( "Load Behavior Library" ) {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
                if ( count == 0 ) {
                    libraryHolder = new LibraryHolder();
                    libraryPanel.add(libraryHolder);
                    libraryHolder.makeNewTab();



                    new LibraryReader( workspace.getFrame() , deltaTickTab );
                    libraryHolder.setTabName( buildPanel.getBgInfo().getLibrary() );
                    addPlot.setEnabled(true);
                    addHisto.setEnabled(true);
                    addBreed.setEnabled(true);
                    addTraits.setEnabled(false);
                    chgEnvt.setEnabled(true);

                    deltaTickTab.contentPanel.validate();
                    count ++;
                }
                 else if (count > 0 ){
                   //TODO: Make sure additional libraries are going to right panels
                {  libraryHolder.makeNewTab();
                    new LibraryReader( workspace.getFrame(), deltaTickTab );
                    libraryHolder.setTabName( buildPanel.getBgInfo().getLibrary() );
                    deltaTickTab.contentPanel.validate();
                }
                }
            }
        };

    
    private final javax.swing.Action addBreedAction =
		new javax.swing.AbstractAction( "Add Species" ) {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
                BreedBlock newBreed;
                addTraits.setEnabled(true);

                // if more than 1 breed available in XML -A. (oct 5)
                // can remove the next line of code because there will always be more than one type of available breeds
                // (feb 4)
                if( buildPanel.availBreeds().size() > 1 ) {
                    breedTypeSelector.showMe(buildPanel.getBgInfo());
                    if (breedTypeSelector.typedBreedType() != null) {
                        Breed breed = buildPanel.getBgInfo().getBreeds().get(0);
                        newBreed = new BreedBlock( breed, breedTypeSelector.typedBreedType(), workspace.getFrame() );
                        buildPanel.addBreed(newBreed);
                        userInput.addBreed(newBreed.plural());
                        newBreed.getParent().setComponentZOrder(newBreed, 0 );
                        new BreedDropTarget(newBreed, deltaTickTab);
                    }
                }
                    else if( breedTypeSelector.selectedBreedType() != null ) {

                        for( Breed breed : buildPanel.getBgInfo().getBreeds() ) {
                            if (breed.plural()  == breedTypeSelector.selectedBreedType()) {
                                newBreed = new BreedBlock( breed, breed.plural(), workspace.getFrame() );

                                buildPanel.addBreed(newBreed);
                                userInput.addBreed(newBreed.plural());
                                newBreed.getParent().setComponentZOrder(newBreed, 0 );
                                new BreedDropTarget(newBreed, deltaTickTab);

                    } }
                } else {
                    Breed breed = buildPanel.availBreeds().get(0);

                    if( buildPanel.breedCount() == 0 ) {
                        newBreed = new BreedBlock( breed , breed.plural(), workspace.getFrame() );
                    } else {
                        newBreed = new BreedBlock( breed , breed.plural() + buildPanel.breedCount(), workspace.getFrame() );
                    }

                    buildPanel.addBreed(newBreed);
                    userInput.addBreed(newBreed.plural());
                    newBreed.getParent().setComponentZOrder(newBreed, 0 );
                    new BreedDropTarget(newBreed, deltaTickTab);
                }
                    contentPanel.validate();
            }
      };

    private final javax.swing.Action addTraitsAction =
		new javax.swing.AbstractAction( "Add Traits" ) {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
                TraitBlock newTraitBlock;
                traitTypeSelector.showMe(buildPanel, buildPanel.getBgInfo());


                /*     not needed if users are picking traits from a list (Aditi, Aug 7, 2012)
                traitSelector.showMe( buildPanel );
                variationSelector.showMe();
                if (variationSelector.check() == true) {
                newTraitBlock = new TraitBlock( traitSelector.selectedBreed(), traitSelector.traitName(),
                        variationSelector.getVariationList(), variationSelector.getNumberList(), variationSelector.data() );
                        */

                newTraitBlock = new TraitBlock(traitTypeSelector.getSelectedBreedBlock(), traitTypeSelector.getSelectedTrait(),
                        traitTypeSelector.getSelectedTrait().getVariationHashMap());
                    /*   not necessary if user selects a trait - Aditi (Aug 7, 2012)
                    userInput.addTraitAndVariations( traitSelector.selectedBreed(), traitSelector.traitName(),
                            variationSelector.getVariationList());
                            */
                userInput.addTraitAndVariations( traitTypeSelector.getSelectedBreed(), traitTypeSelector.getSelectedTraitName(),
                        traitTypeSelector.getSelectedTrait().getVariationsList());
                buildPanel.addTrait(newTraitBlock);
                libraryHolder.addTraittoTab(newTraitBlock, buildPanel.getMyTraits().size());
                deltaTickTab.addDragSource(newTraitBlock);
                new TraitDropTarget(newTraitBlock);
                contentPanel.validate();
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
                new PlotDropTarget(hBlock);
                contentPanel.validate();

                /*   commented out to combine histo and plot blocks - Aditi (Jan 15, 2013)
                HistogramBlock newHisto = new HistogramBlock();
                buildPanel.addHisto( newHisto );
                newHisto.getParent().setComponentZOrder(newHisto, 0);
                new HistoDropTarget(newHisto);
                contentPanel.validate();
                */

        }
    };

    private final Action toBuildBlock =
            new javax.swing.AbstractAction( "Build operator block" ) {
                public void actionPerformed ( ActionEvent e ) {
                    OperatorBlock newOBlock;
                    // show OperatorBlock dialog
                    //trial.initComponents();
                    //trial.activateButtons();
                    obBuilder.showMe(userInput);
                    if ( obBuilder.check()  == true ) {

                        //newOBlock = new OperatorBlock( obBuilder, userInput );
                        newOBlock = new OperatorBlock( obBuilder.selectedBreed(), obBuilder.selectedTrait(),
                                obBuilder.selectedTrait2(),
                                userInput.getVariations(obBuilder.selectedBreed(), obBuilder.selectedTrait()),
                                userInput.getVariations(obBuilder.selectedBreed(), obBuilder.selectedTrait2()));

                        //newOBlock = new OperatorBlock( obBuilder.selectedBreed(), obBuilder.selectedTrait(),
                               // obBuilder.selectedTrait2(), userInput );
                        //libraryPanel.add(newOBlock);
                        libraryHolder.addOperatortoTab(newOBlock);
                        deltaTickTab.addDragSource(newOBlock);
                    }
                }
            };


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
        //org.nlogo.window.ButtonWidget buttonWidget = interface
        if (setupWidget instanceof org.nlogo.window.ButtonWidget) {
          org.nlogo.window.ButtonWidget button =
              (org.nlogo.window.ButtonWidget) setupWidget;
            button.displayName("setup");
            button.wrapSource("setup");
        }

        org.nlogo.window.Widget goWidget = interfacePanel.makeWidget("BUTTON",false);
        interfacePanel.addWidget(goWidget, 60, 0, true, false);
        //org.nlogo.window.ButtonWidget buttonWidget = interface
        if (goWidget instanceof org.nlogo.window.ButtonWidget) {
          org.nlogo.window.ButtonWidget button =
              (org.nlogo.window.ButtonWidget) goWidget;
            button.displayName("go");
            button.wrapSource("go");
            //(ButtonWidget) button.forever_=(!button.forever());
            button.setForeverOn();
        }

        org.nlogo.window.Widget drawWidget = interfacePanel.makeWidget("BUTTON",false);
        interfacePanel.addWidget(drawWidget, 0, 130, true, false);
        //org.nlogo.window.ButtonWidget buttonWidget = interface
        if (drawWidget instanceof org.nlogo.window.ButtonWidget) {
          org.nlogo.window.ButtonWidget button =
              (org.nlogo.window.ButtonWidget) drawWidget;
            button.displayName("draw");
            button.wrapSource("draw");
            button.setForeverOn();
        }

        org.nlogo.window.Widget envtChooserWidget = interfacePanel.makeWidget("CHOOSER",false);
        interfacePanel.addWidget(envtChooserWidget, 0, 100, true, false);
        //org.nlogo.window.ButtonWidget buttonWidget = interface
        if (envtChooserWidget instanceof org.nlogo.window.ChooserWidget) {
          org.nlogo.window.ChooserWidget chooser =
              (org.nlogo.window.ChooserWidget) envtChooserWidget;
            chooser.displayName("environment");
            chooser.nameWrapper("environment");
            LogoListBuilder choicesList = new LogoListBuilder();
            choicesList.add("grass");
            choicesList.add("water");
            chooser.setChoices(choicesList.toLogoList());
        }
            interfaceCount++;
        }
    }

    public void populatePlots() {
        try {
            // for each plot block
            for( PlotBlock plotBlock : buildPanel.getMyPlots() ) {
                if( plotBlock.getNetLogoPlot() == null ) { // if this is the first time this plot has been created
                    System.out.println("first time I'm going to interface");
                    Plot newPlot = workspace.plotManager().newPlot(plotBlock.getName());
                    for( QuantityBlock quantBlock : plotBlock.getMyBlocks() ) {
                        String penName = "";
                        penName += quantBlock.getName() + " ";
                        //newPlot.setupCode() = "setup";
                        for( JTextField input : quantBlock.getInputs().values() ) {
                            penName += input.getText() + " ";
                        }
                        PlotPen newPen = newPlot.createPlotPen( penName , false );
                        //newPen.defaultColor_$eq( quantBlock.getPenColor().getRGB() );
                        //enters this loop when pressed Add histo and went to Run -A. (sept 26)
                        if( plotBlock.isHistogram() ) {
                            newPen.defaultMode_$eq( 1 );
                        }
                    }
                    plotBlock.setNetLogoPlot(newPlot);
                } else { // otherwise it has a plot already, update that guy
                    //enters this when the plot block already exists, but another quantity block is added
                    System.out.println("n time I'm going to interface");
                    plotBlock.getNetLogoPlot().name( plotBlock.getName() );
                    // additional pens needed?
                    for( QuantityBlock quantBlock : plotBlock.getMyBlocks() ) {
                        String penName = "";
                        penName += quantBlock.getName() + " ";
                        for( JTextField input : quantBlock.getInputs().values() ) {
                            penName += input.getText() + " ";
                        }
                        if( ! plotBlock.getNetLogoPlot().getPen( penName ).isDefined() ) {
                            PlotPen newPen = plotBlock.getNetLogoPlot().createPlotPen( penName , false );
                            if( plotBlock.isHistogram() ) {
                                newPen.defaultMode_$eq( 1 );

                            }
                        }
                        //plotBlock.getNetLogoPlot().getPen( penName ).get().defaultColor_$eq( quantBlock.getPenColor().getRGB() );
                    }
                }
            }
        } catch ( Exception ex ) {
            System.out.println(ex.getMessage());
        }
        revalidate() ;
    }

    public void populatePlotsTest() {
        try {
           for (PlotBlock plotBlock : buildPanel.getMyPlots().subList(interfacePlotCount, buildPanel.getMyPlots().size())) {
               org.nlogo.window.Widget plotWidget = interfacePanel.makeWidget("Plot", false);
               WidgetWrapper ww = interfacePanel.addWidget(plotWidget, 5, 50, true, false);
               plotWidget.displayName(plotBlock.getName());
               org.nlogo.plot.Plot newPlot = workspace.plotManager().getPlot("plot " + (interfacePlotCount + 1));
               plotWrappers.put("plot" + (interfacePlotCount + 1), ww);

               for (QuantityBlock quantBlock : plotBlock.getMyBlocks()) {
                            if (newPlot.getPen(quantBlock.getName()).toString().equals("None")) {

                                PlotPen plotPen = newPlot.createPlotPen(quantBlock.getName(), false);
                                plotPen.updateCode(quantBlock.getPenUpdateCode());
                            }
               }
               interfacePlotCount++;
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


           /*
            for (PlotBlock plotBlock : buildPanel.getMyPlots().subList(interfacePlotCount, buildPanel.getMyPlots().size())) {
                for (Widget w : interfacePanel.getWidgetsForSaving()) {
                    System.out.println("DTT ln 588 " + w.getDisplayName());
                    if (w.getDisplayName().equals(plotBlock.getName())) {
                        for (QuantityBlock quantBlock : plotBlock.getMyBlocks()) {
                            if (newPlot.getPen(quantBlock.getName()).toString().equals("None")) {
                                PlotPen plotPen = newPlot.createPlotPen(quantBlock.getName(), false);
                                plotPen.updateCode(quantBlock.getPenUpdateCode());
                            }
                            //else if (newPlot.getPen(quantBlock.getName()).toString().matches())
                        }
                    }
                }
                for (QuantityBlock quantBlock : plotBlock.getMyBlocks()) {
                    if (newPlot.getPen(quantBlock.getName()).toString().equals("None")) {
                        PlotPen plotPen = newPlot.createPlotPen(quantBlock.getName(), false);
                        plotPen.updateCode(quantBlock.getPenUpdateCode());
                    }
                }
                interfacePlotCount++;
            }
            */

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
            for (Widget widget : interfacePanel.getWidgetsForSaving()) {
                if (widget instanceof PlotWidget) {
                    for (Map.Entry<String, WidgetWrapper> entry : plotWrappers.entrySet()) {
                    String p = entry.getKey();
                    WidgetWrapper w = entry.getValue();
                        if (buildPanel.getMyPlot(p) == false) {
                            interfacePanel.removeWidget(w);
                            interfacePlotCount--;
                        }
                    }
                }
            }
        }
        catch ( Exception ex ) {
            System.out.println(ex.getMessage());
        }
        revalidate() ;
    }

    public void removePlotPens() {

    }

    public void populatePlotsInterface() {
        try {
            for( PlotBlock plotBlock : buildPanel.getMyPlots() ) {
                if (plotBlock.getNetLogoPlot() == null ) {
                    Plot newPlot = workspace.plotManager().newPlot(plotBlock.getName());

                    PlotPen newPen;
                    for( QuantityBlock quantBlock : plotBlock.getMyBlocks() ) {
                        String penName = "";
                        penName += quantBlock.getName() + " ";
                        String penUpdate = quantBlock.getPenUpdateCode();
                        for( JTextField input : quantBlock.getInputs().values() ) {
                            penName += input.getText() + " ";

                        }
                        newPen = newPlot.createPlotPen( penName , false, "setup ", penUpdate );

                        //workspace.plotManager().compilePlot(newPlot);

                    }

                    //interfacePanel.makePlotWidget(plotManager, newPlot, newPen);
                    Widget plotWidget = (Widget) new PlotWidget(newPlot, workspace.plotManager());
                    plotWidget.displayName(plotBlock.getName());
                    interfacePanel.addWidget(plotWidget, 30, 0, false, false);

                    //plotManager.compilePlot(newPlot); -commented out by Aditi
                    //workspace.plotManager().compilePlot(newPlot);

                    plotBlock.setNetLogoPlot(newPlot);

                   // TODO: think about this
                //org.nlogo.window.Widget plotWidget = interfacePanel.makeWidget("Plot",false);
                 //org.nlogo.window.plotWidget_$eq
                    //org.nlogo.
                   // org.nlogo.window.Widget plotWidget = new plotWidget("Plot", false);



                //interfacePanel.addWidget(plotWidget, 10, 30, true, false);
                    //plotWidget.plot_$eq(workspace.plotManager().newPlot(plotBlock.getName()));



                   // PlotPen newPen = plotWidget.createPlotPen( "Adi" , false );


                /*
                if (plotWidget instanceof org.nlogo.window.PlotWidget) {
                    org.nlogo.window.PlotWidget plot =
                    (org.nlogo.window.PlotWidget) plotWidget;
                    plot.displayName(plotBlock.getName());
                    //plot.wrapSource("setup");
        }
        */
               // if( plotBlock.getNetLogoPlot() == null ) {





                } else { // otherwise it has a plot already, update that guy
                    //enters this when the plot block already exists, but another quantity block is added


                    plotBlock.getNetLogoPlot().name( plotBlock.getName() );
                    // additional pens needed?
                    for( QuantityBlock quantBlock : plotBlock.getMyBlocks() ) {
                        String penName = "";
                        penName += quantBlock.getName() + " ";
                        String penUpdateCode ="";
                        penUpdateCode += quantBlock.getPenUpdateCode();
                        for( JTextField input : quantBlock.getInputs().values() ) {
                            penName += input.getText() + " ";
                        }
                        if( ! plotBlock.getNetLogoPlot().getPen( penName ).isDefined() ) {
                            PlotPen newPen = plotBlock.getNetLogoPlot().createPlotPen( penName , false );
                            newPen.updateCode(penUpdateCode);
                            if( plotBlock.isHistogram() ) {
                                newPen.defaultMode_$eq( 1 );

                            }
                        }
                       // plotBlock.getNetLogoPlot().getPen( penName ).get().defaultColor_$eq( quantBlock.getPenColor().getRGB() );
                    }
                }
            }
        } catch ( Exception ex ) {
            System.out.println(ex.getMessage());
        }
        revalidate() ;
    }

    public void populateHisto() { // this method is not used
        try {
            // for each plot block
            for( HistogramBlock histoBlock : buildPanel.getMyHisto() ) {
                if( histoBlock.getNetLogoPlot() == null ) { // if this is the first time this plot has been created
                    System.out.println("DTT 668 I'm reaching here");
                    Plot newPlot = workspace.plotManager().newPlot(histoBlock.getName());
                    for( QuantityBlock quantBlock : histoBlock.getMyBlocks() ) {
                        String penName = "";
                        penName += quantBlock.getName() + " ";
                        for( JTextField input : quantBlock.getInputs().values() ) {
                            penName += input.getText() + " ";

                        }
                        PlotPen newPen = newPlot.createPlotPen( penName , false );

                        //newPen.defaultColor_$eq( quantBlock.getPenColor().getRGB() );
                        //enters this loop when pressed Add histo and went to Run -A. (sept 26)
                        if( histoBlock.histogram() ) {
                            newPen.defaultMode_$eq( 1 );

                        }

                    }
                    histoBlock.setNetLogoPlot(newPlot);
                } else { // otherwise it has a plot already, update that guy
                    //enters this when the plot block already exists, but another quantity block is added

                    //histoBlock.getNetLogoPlot().name( histoBlock.getName() );
                    histoBlock.getName();
                    System.out.println("DTT ln 692 " + histoBlock.getName());
                    // additional pens needed?
                    for( QuantityBlock quantBlock : histoBlock.getMyBlocks() ) {
                        String penName = "";
                        penName += quantBlock.getName() + " ";
                        for( JTextField input : quantBlock.getInputs().values() ) {
                            penName += input.getText() + " ";
                        }
                        if( ! histoBlock.getNetLogoPlot().getPen( penName ).isDefined() ) {
                            PlotPen newPen = histoBlock.getNetLogoPlot().createPlotPen( penName , false );
                            /*
                            if( plotBlock.histogram() ) {
                                newPen.defaultMode_$eq( 1 );

                            }
                            */
                        }
                        //histoBlock.getNetLogoPlot().getPen( penName ).get().defaultColor_$eq( quantBlock.getPenColor().getRGB() );
                    }
                }
            }
        } catch ( Exception ex ) {
            System.out.println(ex.getMessage());
        }
        revalidate() ;
    }

    public void populateHistoInterface() {
        try {
            for( HistogramBlock hBlock : buildPanel.getMyHisto() ) {
                if (hBlock.getNetLogoPlot() == null ) {
                org.nlogo.window.Widget plotWidget = interfacePanel.makeWidget("Plot",false);
                interfacePanel.addWidget(plotWidget, 0, 20, true, false);

                if (plotWidget instanceof org.nlogo.window.PlotWidget) {
                    org.nlogo.window.PlotWidget plot = (org.nlogo.window.PlotWidget) plotWidget;
                    plot.displayName(hBlock.getName());
                    //plot.wrapSource("setup");
                }

                Plot newPlot = workspace.plotManager().newPlot(hBlock.getName());
                for( QuantityBlock quantBlock : hBlock.getMyBlocks() ) {
                    String penName = "";
                    penName += quantBlock.getName() + " ";
                    for( JTextField input : quantBlock.getInputs().values() ) {
                        penName += input.getText() + " ";
                        }
                    PlotPen newPen = newPlot.createPlotPen( penName , false );

                        //newPen.defaultColor_$eq( quantBlock.getPenColor().getRGB() );
                        //enters this loop when pressed Add histo and went to Run -A. (sept 26)
                        /*
                        if( plotBlock.histogram() ) {
                            newPen.defaultMode_$eq( 1 );

                        }
                        */
                }
                hBlock.setNetLogoPlot(newPlot);

                } else { // otherwise it has a plot already, update that guy
                    //enters this when the plot block already exists, but another quantity block is added

                    org.nlogo.window.Widget plotWidget = interfacePanel.makeWidget("Plot",false);
                interfacePanel.addWidget(plotWidget, 0, 0, true, false);

                if (plotWidget instanceof org.nlogo.window.PlotWidget) {
                    org.nlogo.window.PlotWidget plot =
                    (org.nlogo.window.PlotWidget) plotWidget;
                    plot.displayName(hBlock.getName());
                    //plot.wrapSource("setup");
                }

                    hBlock.getNetLogoPlot().name( hBlock.getName() );
                    // additional pens needed?
                    for( QuantityBlock quantBlock : hBlock.getMyBlocks() ) {
                        String penName = "";
                        penName += quantBlock.getName() + " ";
                        for( JTextField input : quantBlock.getInputs().values() ) {
                            penName += input.getText() + " ";
                        }
                        if( ! hBlock.getNetLogoPlot().getPen( penName ).isDefined() ) {
                            PlotPen newPen = hBlock.getNetLogoPlot().createPlotPen( penName , false );
                            if( hBlock.histogram() ) {
                                newPen.defaultMode_$eq( 1 );

                            }
                        }
                       // hBlock.getNetLogoPlot().getPen( penName ).get().defaultColor_$eq( quantBlock.getPenColor().getRGB() );
                    }
                }
            }
        } catch ( Exception ex ) {
            System.out.println(ex.getMessage());
        }
        revalidate() ;
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
                addTraits = new JButton ( addTraitsAction );
                addTraits.setEnabled(false);
                this.add(addTraits);
                addPlot = new JButton( addPlotAction );
                addPlot.setEnabled(false);
                this.add(addPlot) ;
                addHisto = new JButton( addHistoAction );
                addHisto.setEnabled(false);
                this.add(addHisto) ;
                chgEnvt = new JButton ( chgEnvtAction );
                chgEnvt.setEnabled(false);
                this.add(chgEnvt) ;
                this.add( new org.nlogo.swing.ToolBar.Separator() ) ;
                buildBlock = new JButton( toBuildBlock );
                this.add( buildBlock );
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

    public JPanel getLibraryPanel2() {
        return libraryPanel2;
    }

    public BuildPanel getBuildPanel() {
        return buildPanel;
    }

    public LibraryHolder getLibraryHolder() {
        return libraryHolder;
    }

    public TraitSelector getTraitSelector() {
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
            populatePlotsTest();
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

    public void clearLibrary2() {
        libraryPanel2.removeAll();
        buildPanel.clear();
        clearPlots();
        buildPanel.getBgInfo().clear();
        //breedTypeSelector.clear();
        libraryPanel2.repaint();
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



}


