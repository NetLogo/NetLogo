package org.nlogo.app;

import org.nlogo.agent.Observer;
import org.nlogo.api.SimpleJobOwner;
import org.nlogo.api.CompilerException;
import org.nlogo.deltatick.*;
import org.nlogo.deltatick.dialogs.*;
import org.nlogo.deltatick.dnd.*;

import org.nlogo.deltatick.xml.Envt;
import org.nlogo.deltatick.xml.LibraryReader;
import org.nlogo.deltatick.xml.LibraryReader2;
import org.nlogo.deltatick.xml.Breed;
import org.nlogo.plot.Plot;
import org.nlogo.plot.PlotPen;
import org.nlogo.window.GUIWorkspace;

// java.awt contains all of the classes for creating user interfaces and for painting graphics and images -A. (sept 8)
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

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

    //ButtonWidget setup;
    //ButtonWidget go; // for emergency shutdown on tab switch
    //ArrayList<WidgetWrapper> plotWrappers = new ArrayList<WidgetWrapper>();

    //InterfaceTab it;
    ProceduresTab pt;
    GUIWorkspace workspace;

    DeltaTickTab deltaTickTab = this;

    public final SimpleJobOwner defaultOwner ;

    //constructor -A. (sept 8)
    public DeltaTickTab( GUIWorkspace workspace , ProceduresTab pt ) {

        this.workspace = workspace;
        this.pt = pt;
        
        this.breedTypeSelector = new BreedTypeSelector(workspace.getFrame());
        //this.it = it;
        this.traitSelector = new TraitSelector( workspace.getFrame() );
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
    }


    public void addCondition( ConditionBlock cBlock ) {
        new ConditionDropTarget(cBlock);
        //new PlantedCodeBlockDragSource(cBlock);
    }

    public void addDragSource( CodeBlock block ) {
        new CodeBlockDragSource( block );
    }


    public void addTrait( TraitBlock tBlock ) {
        new TraitDropTarget(tBlock);
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
                traitSelector.showMe( buildPanel );
                variationSelector.showMe();

                //if (variationSelector.getVariationList() != null) {
                if (variationSelector.check() == true) {
                newTraitBlock = new TraitBlock( traitSelector.selectedBreed(), traitSelector.traitName(),
                        variationSelector.getVariationList(), variationSelector.data() );

                    libraryPanel.add(newTraitBlock);
                    userInput.addTraitAndVariations( traitSelector.selectedBreed(), traitSelector.traitName(),
                            variationSelector.getVariationList());

                libraryHolder.addLibrarytoTab(newTraitBlock);
                 deltaTickTab.addDragSource(newTraitBlock);
                    buildPanel.addTrait(newTraitBlock);
                    new TraitDropTarget(newTraitBlock);
                 contentPanel.validate();
            }
            }
        };



    private final javax.swing.Action addPlotAction =
		new javax.swing.AbstractAction( "Add Graph" ) {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
                PlotBlock newPlot = new PlotBlock();
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
                            System.out.println("B");
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

                PlotBlock newPlot = new PlotBlock( true );
                buildPanel.addPlot( newPlot );
                newPlot.getParent().setComponentZOrder(newPlot, 0 );
                new PlotDropTarget(newPlot);
                contentPanel.validate();

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
                        System.out.println(obBuilder.selectedBreed());
                        //newOBlock = new OperatorBlock( obBuilder, userInput );
                        newOBlock = new OperatorBlock( obBuilder.selectedBreed(), obBuilder.selectedTrait(),
                                obBuilder.selectedTrait2(),
                                userInput.getVariations(obBuilder.selectedBreed(), obBuilder.selectedTrait()),
                                userInput.getVariations(obBuilder.selectedBreed(), obBuilder.selectedTrait2()));

                        //newOBlock = new OperatorBlock( obBuilder.selectedBreed(), obBuilder.selectedTrait(),
                               // obBuilder.selectedTrait2(), userInput );
                        libraryPanel.add(newOBlock);
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

    public void populatePlots() {
        try {
            // for each plot block
            for( PlotBlock plotBlock : buildPanel.getMyPlots() ) {
                // if there's a netlogo plot with the same name
                if( plotBlock.getNetLogoPlot() == null ) { // if it hasn't been assigned yet, make one
                    Plot newPlot = workspace.plotManager().newPlot(plotBlock.getName());
                    for( QuantityBlock quantBlock : plotBlock.getMyBlocks() ) {
                        String penName = "";
                        penName += quantBlock.getName() + " ";
                        for( JTextField input : quantBlock.getInputs().values() ) {
                            penName += input.getText() + " ";
                            System.out.println(penName);
                        }
                        PlotPen newPen = newPlot.createPlotPen( penName , false );
                        newPen.defaultColor_$eq( quantBlock.getPenColor().getRGB() );
                        //enters this loop when pressed Add histo and went to Run -A. (sept 26)
                        if( plotBlock.histogram() ) {
                            newPen.defaultMode_$eq( 1 );
                            System.out.println("trying to create histogram");
                        }
                        //System.out.println("It's not entering that loop");
                    }
                    plotBlock.setNetLogoPlot(newPlot);
                } else { // otherwise it has a plot already, update that guy
                    //enters this when the plot block already exists, but another quantity block is added
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
                            System.out.println("plot loop 2");
                            if( plotBlock.histogram() ) {
                                newPen.defaultMode_$eq( 1 );
                                //System.out.println("Aditi");
                            }
                        }
                        plotBlock.getNetLogoPlot().getPen( penName ).get().defaultColor_$eq( quantBlock.getPenColor().getRGB() );
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

    public void handle( Events.SwitchedTabsEvent event ) {
        if( event.oldTab == this ) {
            populateProcedures();
            pt.setIndenter(true);
            pt.select(0, pt.innerSource().length() );
            // pt.getIndenter().handleTab();
            pt.select(0,0);
            populatePlots();
            new org.nlogo.window.Events.CompileAllEvent()
				.raise( DeltaTickTab.this ) ;
            workspace.plotManager().compileAllPlots();
            //for( Plot plot : workspace.plotManager().getPlots() ) {
            //    print( plot.getPens() );
            //}
            //setup();
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
        System.out.println("Deltatick tab " + buildPanel.getBgInfo().getLibrary());
        return buildPanel.getBgInfo().getLibrary();

    }



}


