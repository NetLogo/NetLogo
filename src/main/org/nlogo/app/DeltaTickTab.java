package org.nlogo.app;

import org.nlogo.agent.Observer;
import org.nlogo.api.SimpleJobOwner;
import org.nlogo.api.CompilerException;
import org.nlogo.deltatick.*;
import org.nlogo.deltatick.dialogs.BreedTypeSelector;
import org.nlogo.deltatick.dialogs.EnvtTypeSelector;
import org.nlogo.deltatick.dialogs.TraitSelector;
import org.nlogo.deltatick.dialogs.VariationSelector;
import org.nlogo.deltatick.dnd.*;

import org.nlogo.deltatick.xml.Envt;
import org.nlogo.deltatick.xml.LibraryReader;
import org.nlogo.deltatick.xml.Breed;
import org.nlogo.plot.Plot;
import org.nlogo.plot.PlotPen;
import org.nlogo.window.GUIWorkspace;

// java.awt contains all of the classes for creating user interfaces and for painting graphics and images -A. (sept 8)
import javax.swing.*;
import java.awt.*;

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

    JSeparator separator = new JSeparator();
    JPanel contentPanel = new JPanel();
    JPanel libraryPanel;
    BuildPanel buildPanel;

    JButton addBreed;
    JButton addPlot;
    JButton addHisto;
    JButton chgEnvt;
    JButton Not;

    boolean plotsAlive = false;

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
        libraryPanel.setLayout(new GridLayout(16,1));

        //second line is making the entire buildPanel ready for stuff to be dragged -A. (sept 8)
        buildPanel = new BuildPanel( workspace );
        new BuildPanelDragSource(buildPanel);

        separator.setOrientation(SwingConstants.VERTICAL);

        java.awt.GridBagConstraints gridBagConstraints;

        contentPanel.setLayout(new java.awt.GridBagLayout());

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

        //contentPanel.pack();
    }

    // not sure what addCondition & addDragSource are doing -A. (sept 8)
    public void addCondition( ConditionBlock cBlock ) {
        new ConditionDropTarget(cBlock);
        //new PlantedCodeBlockDragSource(cBlock);
    }

    public void addDragSource( CodeBlock block ) {
        new CodeBlockDragSource( block );
    }

    // Plot, histo and breed become active only once library is chosen
    private final javax.swing.Action loadAction =
		new javax.swing.AbstractAction( "Load Behavior Library" ) {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
            	new LibraryReader( workspace.getFrame() , deltaTickTab );
                addPlot.setEnabled(true);
                addHisto.setEnabled(true);
                addBreed.setEnabled(true);
                chgEnvt.setEnabled(true);
                deltaTickTab.contentPanel.validate();
            }
        };

    
    private final javax.swing.Action addBreedAction =
		new javax.swing.AbstractAction( "Add Species" ) {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
                BreedBlock newBreed;

                // if more than 1 breed available in XML -A. (oct 5)
                if( buildPanel.availBreeds().size() > 1 ) {
                    breedTypeSelector.showMe(buildPanel.getBgInfo());
                    if (breedTypeSelector.typedBreedType() != null) {
                        traitSelector.showMe();
                        variationSelector.showMe();
                        Breed breed = buildPanel.getBgInfo().getBreeds().get(0);
                        for (String variation : variationSelector.getVariationList()) {
                        newBreed = new BreedBlock( breed , breedTypeSelector.typedBreedType(), traitSelector.printMe(), variation, workspace.getFrame() );
                        buildPanel.addBreed(newBreed);
                        newBreed.getParent().setComponentZOrder(newBreed, 0 );
                        new BreedDropTarget(newBreed, deltaTickTab);
                    } }
                    else if( breedTypeSelector.selectedBreedType() != null ) {
                        traitSelector.showMe();
                        variationSelector.showMe();
                        for( Breed breed : buildPanel.getBgInfo().getBreeds() ) {
                            if (breed.plural()  == breedTypeSelector.selectedBreedType()) {
                                System.out.println("plural:" + breed.plural());
                            for (String variation : variationSelector.getVariationList()) {
                            //if( breed.plural() == breedTypeSelector.selectedBreedType() ) {

                                //if no breeds & if more than 1 breed (people1) -A. (sept 8)
                                //if( buildPanel.breedCount() == 0 ) {
                                     {
                                    newBreed = new BreedBlock( breed , breed.plural(), traitSelector.printMe(), variation, workspace.getFrame() );

                                    //traitSelector.clearTrait();
                                //}
                            } //else {
                                //    newBreed = new BreedBlock( breed , breed.plural() + buildPanel.breedCount(), traitSelector.printMe(), variation, workspace.getFrame() );
                                //}

                                buildPanel.addBreed(newBreed);
                                newBreed.getParent().setComponentZOrder(newBreed, 0 );
                                new BreedDropTarget(newBreed, deltaTickTab);
                            }
                        }
                    //}
                    }
                } else {
                    Breed breed = buildPanel.availBreeds().get(0);
                   // for (String variation : variationSelector.getVariationList()) {
                    System.out.println(buildPanel.availBreeds());
                    for (String variation : variationSelector.getVariationList()) {
                    if( buildPanel.breedCount() == 0 ) {
                        newBreed = new BreedBlock( breed , breed.plural(), traitSelector.printMe(), variation, workspace.getFrame() );
                    } else {
                        newBreed = new BreedBlock( breed , breed.plural() + buildPanel.breedCount(), traitSelector.printMe(), variation, workspace.getFrame() );
                    }

                    buildPanel.addBreed(newBreed);
                    newBreed.getParent().setComponentZOrder(newBreed, 0 );
                    new BreedDropTarget(newBreed, deltaTickTab);
                }
                //BreedBlock newBreed = new BreedBlock("people" , "person", workspace.getFrame() );
                contentPanel.validate();
                //traitSelector.clearTrait();
            }
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
                System.out.println("A");
                for ( Envt envt: buildPanel.getBgInfo().getEnvts() ) {
                    if ( envtTypeSelector.selectedEnvt() != null ) {
                        if ( envtTypeSelector.selectedEnvt() == envt.nameEnvt()) {
                            System.out.println("B");
                            //EnvtBlock newEnvt;
                            //newEnvt = new EnvtBlock (envt, envt.nameEnvt());
                            newEnvt = new EnvtBlock (envt);
                    //if (buildPanel.getBgInfo().getEnvts().size() > 1) {

                        //envtTypeSelector.showMe(buildPanel.getBgInfo());
                        //for (Envt envt1 : buildPanel.getBgInfo().getEnvts()) {
                        //newEnvt = new EnvtBlock(envt1);

                    //}
                   // EnvtBlock newEnvt = new EnvtBlock( envt );
                   // buildPanel.chgEnvt(newEnvt);
                    new EnvtDropTarget(newEnvt, deltaTickTab );
                //}
                        buildPanel.addEnvt(newEnvt);

                }
                        contentPanel.validate();

    };
        };
                //contentPanel.validate();
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
           // };


   // pt is procedures tab of NetLogo -A. (sept 8)
    public void populateProcedures() {
        pt.innerSource( buildPanel.unPackAsCode() );
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
                addBreed = new JButton( addBreedAction );
                addBreed.setEnabled(false);
                this.add( addBreed ) ;
                addPlot = new JButton( addPlotAction );
                addPlot.setEnabled(false);
                this.add( addPlot ) ;
                addHisto = new JButton( addHistoAction );
                addHisto.setEnabled(false);
                this.add( addHisto ) ;
                chgEnvt = new JButton ( chgEnvtAction );
                chgEnvt.setEnabled(false);
                this.add( chgEnvt ) ;
                this.add( new org.nlogo.swing.ToolBar.Separator() ) ;
                this.add( new JButton( clearAction ) ) ;
                //this.add( new JButton( procedureAction ) ) ;
            }
        } ;
	}


    public JPanel getLibraryPanel() {
        return libraryPanel;
    }

    public BuildPanel getBuildPanel() {
        return buildPanel;
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

}
