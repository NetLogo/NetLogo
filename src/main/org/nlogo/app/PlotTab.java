package org.nlogo.app ;

import org.nlogo.api.CompilerException;
import org.nlogo.hotlink.controller.Runner;
import org.nlogo.hotlink.dialogs.DtDialog;
import org.nlogo.shape.DrawableShape;
import org.nlogo.workspace.AbstractWorkspace;
import org.nlogo.hotlink.view.ViewPanel;
import org.nlogo.hotlink.graph.PlotPanel;
import org.nlogo.window.GUIWorkspace;
import org.nlogo.hotlink.dialogs.RunExperiment;
import org.nlogo.hotlink.controller.ExperimentRunner;
import org.nlogo.hotlink.controller.Recorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import org.nlogo.swing.ToolBarMenu;
import javax.swing.JButton;
import javax.swing.JCheckBox;

public strictfp class PlotTab
	extends javax.swing.JPanel
	implements
		Events.SwitchedTabsEvent.Handler ,
		org.nlogo.swing.Printable {

    GUIWorkspace workspace;
    PlotTab plotTab = this;
    ViewPanel viewPanel = new ViewPanel( this );
    PlotPanel plotPanel = new PlotPanel( this );
    JLabel clearNote = new JLabel("Please run or record a model.");
    JPanel contentPanel = new JPanel();
    RunExperiment runExperiment = new RunExperiment();
    ExperimentRunner experimentRunner;
    Runner runner;
    int focusRunNumber = 1;
    int totalRunCount = 0;

    public DeltaTickTab dT;
    DtDialog dtDialog;


    ExecutorService pool = Executors.newCachedThreadPool();


    /*
     * Toolbar buttons
     */
    ToolBarRunMenu runMenu = new ToolBarRunMenu();
    JButton stepButton = new JButton( new StepAction() ) ;
    JButton recordButton = new JButton( new RecordAction() ) ;
    JButton newRunButton = new JButton( new NewRunAction() );
    //JButton runButton = new JButton( new RunAction() ) ;
    JButton clearButton = new JButton( new ClearAction() ) ;
    JButton intervalButton = new JButton( new IntervalAction() ) ;
    JButton saveButton = new JButton( new SaveAction() ) ;
    JButton movieButton = new JButton( new MovieAction() ) ;
    JCheckBox runsCheckBox = new JCheckBox( "Plot All Runs" );

	final org.nlogo.swing.ToolBar toolBar ;

    public boolean sameModelRun = false;

	PlotTab( GUIWorkspace workspace , DeltaTickTab dT ) {
        this.workspace = workspace;
        this.dT = dT;
        dtDialog = new DtDialog( plotPanel );
        experimentRunner = new ExperimentRunner( this , workspace );
        viewPanel.addChangeListener( plotPanel );
		setLayout( new java.awt.BorderLayout() ) ;

		clearContentPanel();

        toolBar = getToolBar();

		add( toolBar , java.awt.BorderLayout.NORTH ) ;
        add( contentPanel, java.awt.BorderLayout.CENTER );
    }

    public void clearContentPanel() {
        //System.out.println("clearContentPanel");
        contentPanel.removeAll();
        plotPanel = new PlotPanel(this);
        dT.setPlotsAlive(false);

        clearButton.setEnabled(false);
        intervalButton.setEnabled(false);
        saveButton.setEnabled(false);
        movieButton.setEnabled(false);
        focusRunNumber = 1;
        totalRunCount = 0;

		clearTmpDirectory();

        contentPanel.setLayout( new java.awt.FlowLayout() );
        contentPanel.add( clearNote );
        contentPanel.setVisible(true);
        setVisible(true);
        this.repaint();
        //workspace.getFrame().repaint();
    }

    public void populateContentPanel() {
        //System.out.println("populateContentPanel");
        contentPanel.removeAll();

        // TODO: BIG HACK
        //clearButton.setEnabled(true);
        //intervalButton.setEnabled(true);
        //saveModelButton.setEnabled(true);
        //movieButton.setEnabled(true);

        dT.setPlotsAlive(true);
        //focusRunNumber = 1;
        //totalRunCount = 0;

        java.awt.GridBagConstraints gridBagConstraints;
        contentPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weightx =0;
        contentPanel.add(viewPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        contentPanel.add(plotPanel, gridBagConstraints);

/*
        contentPanel.setLayout(new java.awt.GridLayout(1,2));
	    contentPanel.setPreferredSize( new java.awt.Dimension( 1100 , 650 ) );
	    contentPanel.setMinimumSize( getPreferredSize() ) ;
	    contentPanel.setMaximumSize( getPreferredSize() ) ;
	    contentPanel.setSize( getPreferredSize() );
	    

        contentPanel.add( viewPanel );
        contentPanel.add( plotPanel );
*/
        contentPanel.setVisible(true);
        setVisible(true);
        focusRunNumber = totalRunCount;
        plotPanel.rePaint();
        contentPanel.repaint();
        viewPanel.refresh();
        this.repaint();
        workspace.getFrame().repaint();
    }

    public int print( java.awt.Graphics g , java.awt.print.PageFormat
					  pageFormat , int pageIndex , org.nlogo.swing.PrinterManager printer )
	{ return 0; }

    public ViewPanel getViewPanel() {
        return viewPanel;
    }

    public PlotPanel getPlotPanel() {
        return plotPanel;
    }

    public Frame getAppFrame() {
        return workspace.getFrame();
    }

    org.nlogo.swing.ToolBar getToolBar()
	{
		return new org.nlogo.swing.ToolBar()
			{
				@Override
				public void addControls()
				{
		            this.add( stepButton ) ;
		            this.add( recordButton ) ;
                    this.add( newRunButton ) ;
                    this.add( clearButton ) ;
					this.add( new org.nlogo.swing.ToolBar.Separator() ) ;
                    this.add( intervalButton ) ;
                    this.add( runMenu );
                    runsCheckBox.setSelected(true);
                    this.add( runsCheckBox );
					this.add( new org.nlogo.swing.ToolBar.Separator() ) ;
                    saveButton.setEnabled(false);
                    this.add( saveButton ) ;
                    movieButton.setEnabled(false);
                    this.add( movieButton ) ;
				}
			} ;
	}

    private class RecordAction
		extends javax.swing.AbstractAction
	{
		RecordAction()
		{
			super( "Run" ) ;
			putValue
				( javax.swing.Action.SMALL_ICON ,
				  new javax.swing.ImageIcon
				  ( PlotTab.class.getResource
					( "/images/hotlink/go.png" ) ) ) ;
		}
		public void actionPerformed( java.awt.event.ActionEvent e )
		{
            if( recordButton.getText().equals( "Run" ) ) {
                if( !sameModelRun ) {
                    addToRunCount(1);
                    sameModelRun = true;
                }

                recordButton.setIcon( new javax.swing.ImageIcon(
                                            PlotTab.class.getResource(
                                                    "/images/hotlink/Stop.png" ) ) );
                recordButton.setText( "Pause" );
                stepButton.setEnabled(false);
                clearButton.setEnabled(false);
                newRunButton.setEnabled(false);
                runner = new Runner( plotTab , workspace );
                pool.submit(runner);
            } else {
                if( runner != null ) {
                    runner.end();
                    runner = null;
                }
                recordButton.setIcon( new javax.swing.ImageIcon(
                                            PlotTab.class.getResource(
                                                    "/images/hotlink/go.png" ) ) );
                recordButton.setText( "Run" );

                stepButton.setEnabled(true);
                clearButton.setEnabled(true);
                newRunButton.setEnabled(true);
                //populateContentPanel();
            }
		}
	}

    private class NewRunAction
		extends javax.swing.AbstractAction
	{
		NewRunAction()
		{
			super( "New Run" ) ;
			putValue
				( javax.swing.Action.SMALL_ICON ,
				  new javax.swing.ImageIcon
				  ( PlotTab.class.getResource
					( "/images/hotlink/Run.png" ) ) ) ;
		}
		public void actionPerformed( java.awt.event.ActionEvent e )
		{
            sameModelRun = false;
            stepButton.doClick();
		}
	}

    private class StepAction
		extends javax.swing.AbstractAction
	{
		StepAction()
		{
			super( "Step" ) ;
			putValue
				( javax.swing.Action.SMALL_ICON ,
				  new javax.swing.ImageIcon
				  ( PlotTab.class.getResource
					( "/images/hotlink/goonce.png" ) ) ) ;
		}
		public void actionPerformed( java.awt.event.ActionEvent e )
		{
            // if it's a new run, do the stuff
            if( !sameModelRun ) {
                addToRunCount(1);
            }
            //System.out.println("new runner?");
            if( runner != null ) {
                runner.end();
                runner = null;
            }

            clearButton.setEnabled(true);
            runner = new Runner( plotTab , workspace );
            runner.step(true);
            pool.submit(runner);
            sameModelRun = true;
            //viewPanel.refresh();
            //viewPanel.goToLastFrame();

            //System.out.println("isAlive" + runner.isAlive());
            /*try {
                runner.join();
            } catch (InterruptedException e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }*/
            //System.out.println("isAlive" + runner.isAlive());
		}
	}

    private class RunAction
		extends javax.swing.AbstractAction
	{
		RunAction()
		{
			super( "Experiment" ) ;
			putValue
				( javax.swing.Action.SMALL_ICON ,
				  new javax.swing.ImageIcon
				  ( PlotTab.class.getResource
					( "/images/hotlink/Run.png" ) ) ) ;
		}
		public void actionPerformed( java.awt.event.ActionEvent e )
		{
            runExperiment.setVisible(true);
            //pool.submit(experimentRunner);
            //experimentRunner.start();
            experimentRunner = new ExperimentRunner( plotTab , workspace );
		}
	}

    private class IntervalAction
		extends javax.swing.AbstractAction
	{
		IntervalAction()
		{
			super( "Interval" ) ;
			putValue
				( javax.swing.Action.SMALL_ICON ,
				  new javax.swing.ImageIcon
				  ( PlotTab.class.getResource
					( "/images/hotlink/Approx.png" ) ) ) ;
		}
		public void actionPerformed( java.awt.event.ActionEvent e )
		{
			dtDialog.setVisible(true);
		}
	}

    private class MovieAction
		extends javax.swing.AbstractAction
	{
		MovieAction()
		{
			super( "Make Movie" ) ;
			putValue
				( javax.swing.Action.SMALL_ICON ,
				  new javax.swing.ImageIcon
				  ( PlotTab.class.getResource
					( "/images/hotlink/Movie.png" ) ) ) ;
		}
		public void actionPerformed( java.awt.event.ActionEvent e )
		{
			//new org.nlogo.window.Events.CompileAllEvent()
			//	.raise( ProceduresTab.this ) ;
		}
	}

    private class SaveAction
		extends javax.swing.AbstractAction
	{
		SaveAction()
		{
			super( "Save Notes" ) ;
			putValue
				( javax.swing.Action.SMALL_ICON ,
				  new javax.swing.ImageIcon
				  ( PlotTab.class.getResource
					( "/images/hotlink/Save.png" ) ) ) ;
		}
		public void actionPerformed( java.awt.event.ActionEvent e )
		{
			//new org.nlogo.window.Events.CompileAllEvent()
			//	.raise( ProceduresTab.this ) ;
		}
	}

    private class ClearAction
		extends javax.swing.AbstractAction
	{
		ClearAction()
		{
			super( "Clear" ) ;
			putValue
				( javax.swing.Action.SMALL_ICON ,
				  new javax.swing.ImageIcon
				  ( PlotTab.class.getResource
					( "/images/hotlink/Clear.png" ) ) ) ;
		}
		public void actionPerformed( java.awt.event.ActionEvent e )
		{
            if( runner != null ) {
                runner.end();
            }
            runner = null;
            sameModelRun = false;
            clearContentPanel();
            //dT.clearPlots();
	    }
    }

    public void clearTmpDirectory() {
        //String tmpDir = System.getProperty("java.io.tmpdir") + "dthl/";
        //java.io.File filesystemWorker = new java.io.File(tmpDir);
        java.io.File filesystemWorker = new java.io.File("tmp");

        if( filesystemWorker.mkdir() ) {
        } else {
            for( java.io.File file : filesystemWorker.listFiles() ) {
                if( file.isDirectory() ) {
                    for( java.io.File file2 : file.listFiles() ) {
                        file2.delete();
                    }
                }
                file.delete();
            }
        }
    }

    public int getNumberOfRuns() {
        return totalRunCount;
    }

    public int getNumberOfTicks() {
        return runExperiment.getTickCount();
    }

    public ArrayList<DrawableShape> getBreedShapes() {
        ArrayList<DrawableShape> modelBreedShapes = new ArrayList<DrawableShape>();

        if( workspace.world().getBreeds().size() > 0 ) {
            //.println("Breeds are declared.");
            for( Object breed : workspace.world().getBreeds().keySet() ) {

                String breedShapeName =
                        workspace.world().turtleBreedShapes.breedShape(
                            workspace.world().getBreed( breed.toString() ) );
                modelBreedShapes.add( (DrawableShape)
                        workspace.world().turtleShapeList().shape(
                            breedShapeName ) );
            }
        } else {
            //System.out.println("No breeds declared.");

            String breedShapeName =
                    workspace.world().turtleBreedShapes.breedShape(
                        workspace.world().turtles() );
            modelBreedShapes.add( (DrawableShape)
                    workspace.world().turtleShapeList().shape(
                        breedShapeName ) );
        }

        return modelBreedShapes;
    }

    public int getFocusRunNumber() {
        return focusRunNumber;
    }

    class ToolBarRunMenu extends ToolBarMenu {

        ToolBarRunMenu() {
            super("Pick a Run Number");
        }

        @Override protected void populate( javax.swing.JPopupMenu menu ) { // menu is PlotTab.this

            if( totalRunCount > 0 ) {
                for( int i = 1 ; i <= totalRunCount ; i++ )
                {
                    javax.swing.JMenuItem item = new javax.swing.JMenuItem( Integer.toString(i) , i ) ;
                    final int runNumber = i;
                    item.addActionListener
                        ( new java.awt.event.ActionListener() {
                                public void actionPerformed( java.awt.event.ActionEvent e ) {
                                    menuSelection( runNumber ) ; } } ) ;
                    menu.add( item ) ;
                }
            } else {
                JMenuItem emptyItem = new javax.swing.JMenuItem( "<No Runs Available>" );
                emptyItem.setEnabled(false);
                menu.add( emptyItem );
            }
        }

        protected void menuSelection( int runNumber )
        {
            focusRunNumber = runNumber;
            viewPanel.getView().showImage( viewPanel.getTimeSlider().getValue() );
            plotPanel.rePaint();
        }
	}

    public boolean showAllRuns() {
        return runsCheckBox.isSelected();    
    }

    public RunExperiment getRunExperiment() {
        return runExperiment;
    }

    public void addToRunCount( int count ) {
        totalRunCount += count;
        focusRunNumber = totalRunCount;
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public void handle( Events.SwitchedTabsEvent event ) {
        if( event.newTab == this ) {
            if( recordButton.getText() == "Stop Recording" ) {
                recordButton.getAction().actionPerformed(new ActionEvent( this , 0 , "" ));
            }
            this.newRunButton.doClick();
        }

        if( event.oldTab == this && runner != null ) {
            recordButton.setIcon( new javax.swing.ImageIcon(
                                        PlotTab.class.getResource(
                                                "/images/hotlink/go.png" ) ) );
            recordButton.setText( "Run" );
        }
    }

    public DtDialog getDtDialog() {
        return dtDialog;
    }

    public void go() {
        dT.go();
    }

    public void setup() {
        dT.setup();
    }

    //TODO public int maxImageCount() {
    //    for( File dir : )
    //}
}
