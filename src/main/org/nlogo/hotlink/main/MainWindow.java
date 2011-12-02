package org.nlogo.hotlink.main;

import org.nlogo.hotlink.controller.ModelReader;
import org.nlogo.hotlink.view.ViewPanel;
import org.nlogo.hotlink.graph.PlotPanel;
import org.nlogo.hotlink.controller.ModelRecorder;
import org.nlogo.hotlink.controller.ModelRunner;
import org.nlogo.hotlink.dialogs.DtDialog;
import org.nlogo.hotlink.dialogs.SetupWindowDialog;
import org.nlogo.hotlink.graph.Plot;

import org.nlogo.hotlink.graph.annotation.AnnotationLoader;
import org.nlogo.hotlink.graph.annotation.AnnotationLoader.LoadedAnnotation;
import java.awt.FileDialog;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JSeparator;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.nlogo.api.Shape;
import org.nlogo.workspace.AbstractWorkspace;


public final class MainWindow
        extends JPanel implements FocusListener {

    // TODO: Seth got a black screen at the end. Boo.

	/**
	 *  This is the main window that appears when
	 *  HotLinkReplay is started
	 */
	private MainWindow mainWindow = this;
	//private ViewPanel viewPanel = new ViewPanel();
	//private PlotPanel graphPanel = new PlotPanel(this);
	private static final long serialVersionUID = 1L;
	private JButton loadButton;
	
	//private FileDialog fileLoader = new FileDialog( this );
    private JFileChooser fileSaver = new JFileChooser();
	//private SetupWindowDialog setupWindow = new SetupWindowDialog(this);
    //TODO: private DtDialog dtDialog = new DtDialog( this , graphPanel );

	//private ModelRunner modelRunner = new ModelRunner( graphPanel , setupWindow );
    //private ModelRecorder modelRecorder = new ModelRecorder( this );
    private ModelReader modelReader;

    // For logging

    private String currentModel = null;
    private String userName = null;
    private Integer userId = -1;

	MainWindow( AbstractWorkspace workspace ) {
		super();

		/**
		 *   Menu setup
		 */
		JMenuBar menuBar = new JMenuBar();
		
		JMenu fileMenu = new JMenu( "File" );
		//JMenuItem loadItem = new JMenuItem( loadModelAction );
		JMenuItem recordItem = new JMenuItem( recordModelAction );
		//JMenuItem quitItem = new JMenuItem( quitModelAction );
		//fileMenu.add( loadItem );
		fileMenu.add( recordItem );
        fileMenu.add( new JSeparator() );
		//fileMenu.add( quitItem );
		
		JMenu plotMenu = new JMenu( "Plots" );
		JMenuItem deltaItem = new JMenuItem( addDeltaAction );
		//JMenuItem customPlotItem = new JMenuItem( addCustomPlotAction );
		plotMenu.add( deltaItem );
		//plotMenu.add( customPlotItem );

        JMenu exportMenu = new JMenu( "Annotations" );
		JMenuItem loadNotesItem = new JMenuItem( loadNotesAction );
		JMenuItem makeMovieItem = new JMenuItem( makeMovieAction );
		JMenuItem saveNotesItem = new JMenuItem( saveNotesAction );
		//JMenuItem customPlotItem = new JMenuItem( addCustomPlotAction );
		exportMenu.add( loadNotesItem );
		exportMenu.add( saveNotesItem );
        exportMenu.add( new JSeparator() );
		exportMenu.add( makeMovieItem );

		menuBar.add(fileMenu);
        menuBar.add(exportMenu);
		menuBar.add(plotMenu);
		//this.setJMenuBar(menuBar);
		
		/**
		 *   Main window setup
		 */
		//setTitle( "NetLogo Hotlink Replay" );
		setLayout(new java.awt.GridLayout(1,2));
	    setPreferredSize( new java.awt.Dimension( 1100 , 650 ) );
	    setMinimumSize( getPreferredSize() ) ;
	    setMaximumSize( getPreferredSize() ) ;
	    setSize( getPreferredSize() );
		//this.setDefaultCloseOperation( JDialog.EXIT_ON_CLOSE );

        Random r = new Random();
        userId = r.nextInt(99999);
	    
		//loadButton = new JButton( loadModelAction );
        //modelRunner.setButton(loadButton);
		//viewPanel.addChangeListener( graphPanel );
		this.add(loadButton);
        this.setVisible(true);
	 
	}

    /*

	private final javax.swing.Action loadModelAction =
		new javax.swing.AbstractAction( "Click To Load A Model..." ) {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
                // clear the
            	remove(viewPanel);
            	remove(graphPanel);
                add(loadButton);
                loadButton.setText("Loading your model...");

            	fileLoader.setVisible(true);

            	if( fileLoader.getFile() != null ) {
            		try {
            			remove(loadButton);
            			graphPanel.renew();
            			setupWindow.purgeModelInfo();
            			String fileName = fileLoader.getDirectory() + fileLoader.getFile();
                        currentModel = fileLoader.getFile().toString();
                        // TODO: make this work
                        loadButton.setText("Loading" + fileName + "...");
                        modelReader = new ModelReader( fileName );
            			setupWindow.pullModelInfo(modelReader);
                        
                        if( setupWindow.runModel() ) {
                            runModel( fileName );
                        } else {
                            add(loadButton);
                            loadButton.setText("Click To Load A Model...");
                        }
                    } catch (Exception ex) {
            			ex.printStackTrace();
            		}
            	}
            	repaint();
        } 
    };
    */

	private final javax.swing.Action recordModelAction =
		new javax.swing.AbstractAction( "Click To Record A Model..." ) {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
                loadButton.setText("Recording your model...");
                //modelRecorder.start();
        }
    };
          /*
    private final javax.swing.Action quitModelAction =
    new javax.swing.AbstractAction( "Quit" ) {
        public void actionPerformed( java.awt.event.ActionEvent e ) {
            mainWindow.dispose();
        }
    };      */

    private final javax.swing.Action addDeltaAction =
		new javax.swing.AbstractAction( "Approximate d/dt..." ) {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
            	//dtDialog.setVisible(true);
        }
    };

    private final javax.swing.Action makeMovieAction =
		new javax.swing.AbstractAction( "Create Movie" ) {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
            	System.out.println("Will Implement");
        }
    };

    private final javax.swing.Action loadNotesAction =
		new javax.swing.AbstractAction( "Load Annotations" ) {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
            	if( fileSaver.showOpenDialog( mainWindow ) == JFileChooser.APPROVE_OPTION ) {
                    File file = fileSaver.getSelectedFile();
                    if( file.isFile() && file.canRead() ) {
                        try {
                            AnnotationLoader loader = new AnnotationLoader(file);
                            for( String name : loader.getPlotNames() ) {
                                //Plot plot = graphPanel.getPlot(name);
                                //if( plot != null ) {
                                //    for( LoadedAnnotation annotation : loader.getLoadedAnnotations(name) ) {
                                //        plot.getPlot().getAnnotationManager().makeAnnotation(
                                //                annotation.getStart(),
                                //                annotation.getEnd(),
                                //                annotation.getText() );
                                //    }
                                //} else {
                                    // make them identify which plot to map
                                    // these annotations to
                                //}
                            }
                        } catch (Exception ex) {

                        }
                    } else {
                        // TODO: Throw something
                    }

                }
            }
    };

    private final javax.swing.Action saveNotesAction =
		new javax.swing.AbstractAction( "Export Annotations" ) { 
            public void actionPerformed( java.awt.event.ActionEvent e ) {
                if( fileSaver.showSaveDialog( mainWindow ) == JFileChooser.APPROVE_OPTION ) {
                    File file = fileSaver.getSelectedFile();
                    
                    try {
                        file.mkdir();
                        if( file.isDirectory() ) {
                            file = ( new File( file.getPath() + "/annotations.txt" ) );
                        }
                        System.out.println(file.toString());
                        file.createNewFile();
                    
                        if( file.canWrite() ) {
                            FileWriter annotationWriter = new FileWriter(file);

                            String allAnnotations = Calendar.getInstance().getTime().toString();
                            allAnnotations += "\n";
                            allAnnotations += currentModel;
                            allAnnotations += "\n";

                            userName = JOptionPane.showInputDialog(
                                    mainWindow,
                                    "Please enter your name:",
                                    "Saving your annotations...",
                                    JOptionPane.QUESTION_MESSAGE );

                            allAnnotations += userName;
                            allAnnotations += "\n";
                            allAnnotations += userId.toString();
                            allAnnotations += "\n";

                            /*for( Plot plot : graphPanel.getPlots() ) {
                                if( plot.getPlot().getAnnotationManager() != null ) {
                                    if( plot.getPlot().getAnnotationManager().anyAnnotations() ) {
                                        File plotDir = new File( file.getParent() + "/plots/" );
                                        System.out.println(plotDir.toString());
                                        plotDir.mkdir();
                                        plot.getPlot().saveWithAnnotations( plotDir.toString() );
                                        allAnnotations += plot.getPlot().getAnnotationManager().export();
                                    }
                                }
                            }*/

                            annotationWriter.write( allAnnotations );
                            annotationWriter.close();
                        } else {
                            // TODO: Handle can't write
                            System.out.println("Can't write to this file.");
                        }
                    } catch (IOException ex) {
                        // TODO: Handle this better
                        System.out.println(ex.getMessage());
                    }
                }
            }
    };

    /*
	private final javax.swing.Action addCustomPlotAction =
		new javax.swing.AbstractAction( "Add Custom Plot..." ) {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
        } 
    };*/
	
	private void runModel( String modelName ) throws Exception {

		//graphPanel.removeAll();

		// TODO: put this back once tickcount is working
        //modelRunner.setModelFilename(modelName);
        //modelRunner.run();

        //viewPanel.refresh();
        //dtDialog.calibrateSpinner();
        //this.add( viewPanel );
        //this.add( graphPanel );
        this.setVisible(true);
	}

    public ModelReader getModelReader() { return modelReader; }
	//public ViewPanel getViewPanel() { return viewPanel; }
	//public PlotPanel getGraphPanel() { return graphPanel; }
    public JButton   getLoadButton() { return loadButton; }

    public void focusGained(FocusEvent e) {
        System.out.println("ok!");
        //viewPanel.refresh();
        //dtDialog.calibrateSpinner();
        //this.add( viewPanel );
        //this.add( graphPanel );
        this.setVisible(true);
    }

    public void focusLost(FocusEvent e) {}
}