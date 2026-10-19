package bsearch.app ;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.SwingUtilities;

import org.xml.sax.SAXException;

import bsearch.algorithms.SearchMethod;
import bsearch.algorithms.SearchMethodLoader;
import bsearch.nlogolink.NetLogoLinkException;
import bsearch.representations.ChromosomeFactory;
import bsearch.representations.ChromosomeTypeLoader;
import bsearch.space.ParameterSpec;
import bsearch.space.SearchSpace;
import bsearch.util.GeneralUtils;
import java.awt.FlowLayout;



/*
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class BehaviorSearchGUI extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;

    private static String getWindowTitleSuffix()
    {
        return " - BehaviorSearch " + GeneralUtils.getVersionString();
    }

    {
        //Set Look & Feel
        try {

            if( System.getProperty( "os.name" ).startsWith( "Mac" ) ||
                    System.getProperty( "os.name" ).startsWith( "Windows" ))
            {
                javax.swing.UIManager.setLookAndFeel
                    ( javax.swing.UIManager.getSystemLookAndFeelClassName() ) ;
            }
            else if (System.getProperty( "swing.defaultlaf" ) == null )
            {
                // On Linux, I prefer the Nimbus LAF... but users can override by setting
                // the swing.defaultlaf property.
                javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private JMenuBar jMenuBar;
    private JMenu jMenuFile;
    private JMenuItem jMenuItemNew ;
    private JMenuItem jMenuItemSaveAs;
    private JMenuItem jMenuItemSave;
    private JMenuItem jMenuItemOpen;
    private JMenuItem jMenuItemExit ;
    private JSeparator jSeparator1;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JLabel jLabel7;
    private JLabel jLabel8;
    private JLabel jLabel10;
    private JLabel jLabel12;
    private JLabel jLabelSep;
    private JPanel jPanelSampling;
    private JPanel jPanelMeasureAfter;
    private JScrollPane jScrollPane1;
    private JTable jTableSearchMethodParams;
    private JPanel jPanel1;
    private JButton jButtonHelpSearchSpace;
    private JLabel jLabelDerivWRT;
    private JLabel jLabelDerivDELTA;
    private JTextField jTextFieldFitnessDerivativeDelta;
    private JComboBox<String> jComboBoxFitnessDerivativeParameter;
    private JCheckBox jCheckBoxTakeDerivative;
    private JCheckBox jCheckBoxFitnessDerivativeUseAbs;
    private JPanel jPanelDeriv;
    private JPanel jPanelDeriv2;
    private JButton jButtonHelpSearchSpaceRepresentation;
    private JSeparator jSeparator2;
    private JMenuItem jMenuItemOpenExample;
    private JMenuItem jMenuItemAbout;
    private JMenuItem jMenuItemTutorial;
    private JMenu jMenuHelp;
    private JTextField jTextFieldBestChecking;
    private JLabel jLabel19;
    private JCheckBox jCheckBoxCaching;
    private JButton jButtonSuggestParamRanges;
    private JButton jButtonHelpEvaluation;
    private JButton jButtonHelpSearchMethod;
    private JTextField jTextFieldEvaluationLimit;
    private JLabel jLabel18;
    private JLabel jLabel9;
    private JTextField jTextFieldFitnessSamplingRepetitions;
    private JComboBox<String> jComboBoxFitnessSamplingMethod;
    private JComboBox<String> jComboBoxSearchMethodType;
    private JTextField jTextFieldModelStepLimit;
    private JTextArea jTextAreaParamSpecs;
    private JButton jButtonBrowseModel;
    private JTextField jTextFieldMeasureIf;
    private JLabel jLabel17;
    private JComboBox<String> jComboBoxFitnessCombineReplications;
    private JLabel jLabel16;
    private JLabel jLabel15;
    private JLabel jLabel14;
    private JComboBox<String> jComboBoxFitnessCollecting;
    private JLabel jLabel13;
    private JComboBox<String> jComboBoxChromosomeType;
    private JLabel jLabel11;
    private JScrollPane jScrollPane2;
    private JTextField jTextFieldModelFile;
    private JTextField jTextFieldModelStopCondition;
    private JComboBox<String> jComboBoxFitnessMinMax;
    private JTextField jTextFieldFitnessMetric;
    private JTextField jTextFieldModelStepCommands;
    private JTextField jTextFieldModelSetupCommands;
    private JButton jButtonRunNow;

  public static void main(final String[] args) {
    mainWithAppHandler(args, null);
  }

    public static void mainWithAppHandler(final String[] args, Object appHandler) {
    initAppHandler(appHandler);
    SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                BehaviorSearchGUI bgui = new BehaviorSearchGUI();
                bgui.setLocationRelativeTo(null);
        bgui.setVisible(true);
        readyAppHandler(appHandler, bgui);
                if (args.length > 0)
                {
                    File f = new File(args[0]);
                    if (f.exists())
                    {
                        bgui.openFile(f);
                    }
                }
            }
        });
    }

  public static void initAppHandler(Object appHandler) {
    try {
      if (appHandler != null) {
        appHandler.getClass().getDeclaredMethod("init").invoke(appHandler);
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
      System.err.println("failed to init appHandler, continuing...");
    }
  }

  public static void readyAppHandler(Object appHandler, Object bgui) {
    try {
      if (appHandler != null) {
        appHandler.getClass().getDeclaredMethod("ready", Object.class).invoke(appHandler, bgui);
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
      System.err.println("failed to ready appHandler, continuing...");
    }
  }

    public BehaviorSearchGUI() {
        super();
        initGUI(); // mainly Jigloo-generated UI code
        finishInitWork(); // custom stuff
    }

    private void initGUI() {
        try {
            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            this.addWindowListener(new WindowListener() {
                public void windowActivated(WindowEvent arg0) { }
                public void windowClosed(WindowEvent arg0) { }
                public void windowClosing(WindowEvent arg0) {   actionExit();   }
                public void windowDeactivated(WindowEvent arg0) { }
                public void windowDeiconified(WindowEvent arg0) { }
                public void windowIconified(WindowEvent arg0) { }
                public void windowOpened(WindowEvent arg0) { }
            });
            GridBagLayout thisLayout = new GridBagLayout();
            getContentPane().setLayout(thisLayout);
            this.setTitle("BehaviorSearch - Experiment Editor");
            thisLayout.rowWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.0, 0.0, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.0, 0.1, 0.0};
            thisLayout.rowHeights = new int[] {7, 25, 10, 25, 25, 25, 25, 20, 25, 10, 25, 25, 25, 25, 20, 20, 20, 23, 20, 7};
            thisLayout.columnWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0, 0.0};
            thisLayout.columnWidths = new int[] {7, 300, 29, 200, 207, 7};
            {
                jMenuBar = new JMenuBar();
                setJMenuBar(jMenuBar);
                {
                    jMenuFile = new JMenu();
                    jMenuBar.add(jMenuFile);
                    jMenuFile.setText("File");
                    {
                        jMenuItemNew = new JMenuItem();
                        jMenuFile.add(jMenuItemNew);
                        jMenuItemNew.setText("New");
                        jMenuItemNew.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                actionNew();
                            }
                        });
                    }
                    {
                        jMenuItemOpen = new JMenuItem();
                        jMenuFile.add(jMenuItemOpen);
                        jMenuItemOpen.setText("Open...");
                        jMenuItemOpen.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                actionOpen();
                            }
                        });
                    }
                    {
                        jMenuItemSave = new JMenuItem();
                        jMenuFile.add(jMenuItemSave);
                        jMenuItemSave.setText("Save");
                        jMenuItemSave.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                actionSave();
                            }
                        });
                    }
                    {
                        jMenuItemSaveAs = new JMenuItem();
                        jMenuFile.add(jMenuItemSaveAs);
                        jMenuItemSaveAs.setText("Save as...");
                        jMenuItemSaveAs.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                actionSaveAs();
                            }
                        });
                    }
                    {
                        jSeparator1 = new JSeparator();
                        jMenuFile.add(jSeparator1);
                    }
                    {
                        jMenuItemOpenExample = new JMenuItem();
                        jMenuFile.add(jMenuItemOpenExample);
                        jMenuItemOpenExample.setText("Open Example...");
                        jMenuItemOpenExample.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                actionOpenExample();
                            }
                        });
                    }
                    {
                        jSeparator2 = new JSeparator();
                        jMenuFile.add(jSeparator2);
                    }
                    {
                        jMenuItemExit = new JMenuItem();
                        jMenuFile.add(jMenuItemExit);
                        jMenuItemExit.setText("Exit");
                        jMenuItemExit.setMnemonic( java.awt.event.KeyEvent.VK_X);
                        jMenuItemExit.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                actionExit();
                            }
                        });
                    }
                }
                {
                    jMenuHelp = new JMenu();
                    jMenuBar.add(jMenuHelp);
                    jMenuHelp.setText("Help");
                    {
                        jMenuItemTutorial = new JMenuItem();
                        jMenuHelp.add(jMenuItemTutorial);
                        jMenuItemTutorial.setText("Tutorial");
                        jMenuItemTutorial.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                actionHelpTutorial();
                            }
                        });

                    }
                    {
                        jMenuItemAbout = new JMenuItem();
                        jMenuHelp.add(jMenuItemAbout);
                        jMenuItemAbout.setText("About...");
                        jMenuItemAbout.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                actionHelpAbout();
                            }
                        });
                    }
                }
            }
            {
                jLabel2 = new JLabel();
                getContentPane().add(jLabel2, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jLabel2.setText("Setup:  ");
                jLabel2.setFont(new java.awt.Font("Tahoma",1,11));
            }
            {
                jTextFieldModelSetupCommands = new JTextField();
                getContentPane().add(jTextFieldModelSetupCommands, new GridBagConstraints(4, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
                jTextFieldModelSetupCommands.setText("setup");
                jTextFieldModelSetupCommands.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                jTextFieldModelSetupCommands.setFont(new java.awt.Font("Monospaced",0,11));
            }
            {
                jTextFieldModelStepCommands = new JTextField();
                getContentPane().add(jTextFieldModelStepCommands, new GridBagConstraints(4, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
                jTextFieldModelStepCommands.setText("go");
                jTextFieldModelStepCommands.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                jTextFieldModelStepCommands.setFont(new java.awt.Font("Monospaced",0,11));
            }
            {
                jLabel3 = new JLabel();
                getContentPane().add(jLabel3, new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jLabel3.setText("Step:  ");
                jLabel3.setFont(new java.awt.Font("Tahoma",1,11));
            }
            {
                jLabel10 = new JLabel();
                getContentPane().add(jLabel10, new GridBagConstraints(3, 7, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jLabel10.setText("Stop If:  ");
                jLabel10.setFont(new java.awt.Font("SansSerif",1,11));
            }
            {
                jTextFieldModelStopCondition = new JTextField();
                getContentPane().add(jTextFieldModelStopCondition, new GridBagConstraints(4, 7, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
                jTextFieldModelStopCondition.setFont(new java.awt.Font("Monospaced",0,11));
                jTextFieldModelStopCondition.setText("count turtles > 100");
                jTextFieldModelStopCondition.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            }
            {
                jLabel4 = new JLabel();
                getContentPane().add(jLabel4, new GridBagConstraints(3, 5, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jLabel4.setText("Measure:  ");
                jLabel4.setFont(new java.awt.Font("SansSerif",1,11));
            }
            {
                jTextFieldFitnessMetric = new JTextField();
                getContentPane().add(jTextFieldFitnessMetric, new GridBagConstraints(4, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
                jTextFieldFitnessMetric.setText("mean [energy] of turtles");
                jTextFieldFitnessMetric.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                jTextFieldFitnessMetric.setFont(new java.awt.Font("Monospaced",0,11));
            }
            {
                jLabelSep = new JLabel();
                getContentPane().add(jLabelSep, new GridBagConstraints(1, 9, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                jLabelSep.setBackground(new java.awt.Color(0,0,0));
                jLabelSep.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
                jLabelSep.setPreferredSize(new java.awt.Dimension(0, 5));
            }
            {
                jLabel6 = new JLabel();
                getContentPane().add(jLabel6, new GridBagConstraints(1, 10, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jLabel6.setText("Search Method Configuration");
                jLabel6.setFont(new java.awt.Font("SansSerif",1,14));
                jLabel6.setBounds(197, 32, 138, 19);
            }
            {
                jLabel7 = new JLabel();
                getContentPane().add(jLabel7, new GridBagConstraints(3, 10, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jLabel7.setText("Objective / Fitness Function");
                jLabel7.setFont(new java.awt.Font("SansSerif",1,14));
            }
            {
                jPanelSampling = new JPanel();
                getContentPane().add(jPanelSampling, new GridBagConstraints(3, 13, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jPanelSampling.setOpaque(false);
                {
                    ComboBoxModel<String> jComboBoxSamplingMethodModel =
                        new DefaultComboBoxModel<String>(
                                new String[] { "Fixed Sampling" }); //, "Adaptive Sampling" });
                    jComboBoxFitnessSamplingMethod = new JComboBox<String>();
                    jPanelSampling.add(jComboBoxFitnessSamplingMethod);
                    jComboBoxFitnessSamplingMethod.setModel(jComboBoxSamplingMethodModel);
                    jComboBoxFitnessSamplingMethod.setEditable(false);
                    jComboBoxFitnessSamplingMethod.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent arg0) {
                            actionSamplingMethodChanged();
                        }
                        });
                    }
                {
                    jTextFieldFitnessSamplingRepetitions = new JTextField();
                    jPanelSampling.add(jTextFieldFitnessSamplingRepetitions);
                    jTextFieldFitnessSamplingRepetitions.setText("10");
                    jTextFieldFitnessSamplingRepetitions.setPreferredSize(new java.awt.Dimension(50, 20));
                    jTextFieldFitnessSamplingRepetitions.setFont(new java.awt.Font("Monospaced",0,11));
                    jTextFieldFitnessSamplingRepetitions.setToolTipText("How many times should the model be run, for a given setting of the parameters?");
                }
                {
                    jLabel8 = new JLabel();
                    jPanelSampling.add(jLabel8);
                    jLabel8.setText("replicates");
                }
            }
            {
                jScrollPane1 = new JScrollPane();
                getContentPane().add(jScrollPane1, new GridBagConstraints(1, 12, 1, 4, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                jScrollPane1.setPreferredSize(new java.awt.Dimension(202, 58));
                {
                    TableModel jTableSearchMethodParamsModel =
                        new DefaultTableModel(
                                new String[][] { { "population", "100" }, { "mutation-rate", "1.0" } , { "crossover-rate", "0.70" }},
                                    new String[] { "Parameter", "Value" });
                    jTableSearchMethodParams = new JTable();
                    jScrollPane1.setViewportView(jTableSearchMethodParams);
                    jTableSearchMethodParams.setModel(jTableSearchMethodParamsModel);
                    jTableSearchMethodParams.getColumn(jTableSearchMethodParams.getColumnName(0)).setPreferredWidth(120);
                }
            }
            {
                jButtonRunNow = new JButton();
                getContentPane().add(jButtonRunNow, new GridBagConstraints(3, 18, 2, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
                jButtonRunNow.setText("Run BehaviorSearch");
                jButtonRunNow.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        actionRunNow();
                    }
                });
            }
            {
                ComboBoxModel<String> jComboBoxMinMaxModel =
                    new DefaultComboBoxModel<String>(
                            new String[] { "Minimize Fitness", "Maximize Fitness" });
                jComboBoxFitnessMinMax = new JComboBox<String>();
                getContentPane().add(jComboBoxFitnessMinMax, new GridBagConstraints(4, 11, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jComboBoxFitnessMinMax.setModel(jComboBoxMinMaxModel);
            }
            {
                jPanelMeasureAfter = new JPanel();
                getContentPane().add(jPanelMeasureAfter, new GridBagConstraints(4, 8, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                jPanelMeasureAfter.setOpaque(false);
                {
                    jTextFieldModelStepLimit = new JTextField();
                    jPanelMeasureAfter.add(jTextFieldModelStepLimit);
                    jTextFieldModelStepLimit.setText("100");
                    jTextFieldModelStepLimit.setPreferredSize(new java.awt.Dimension(50, 20));
                    jTextFieldModelStepLimit.setFont(new java.awt.Font("Monospaced",0,11));
                }
                {
                    jLabel5 = new JLabel();
                    jPanelMeasureAfter.add(jLabel5);
                    jLabel5.setText("model steps");
                }
            }
            {
                jLabel12 = new JLabel();
                getContentPane().add(jLabel12, new GridBagConstraints(1, 2, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                jLabel12.setBackground(new java.awt.Color(0,0,0));
                jLabel12.setPreferredSize(new java.awt.Dimension(0,5));
                jLabel12.setBorder(new SoftBevelBorder(BevelBorder.LOWERED,null,null,null,null));
            }
            {
                jTextFieldModelFile = new JTextField();
                getContentPane().add(jTextFieldModelFile, new GridBagConstraints(1, 1, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                jTextFieldModelFile.setText("Model.nlogo");
                jTextFieldModelFile.setToolTipText("Path to .nlogo file - may be specified relative to the folder containing the '.bsearch' file");
            }
            {
                jButtonBrowseModel = new JButton();
                getContentPane().add(jButtonBrowseModel, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
                jButtonBrowseModel.setText("Browse for model...");
                jButtonBrowseModel.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        actionBrowseModel();
                    }
                });

            }
            {
                jScrollPane2 = new JScrollPane();
                getContentPane().add(jScrollPane2, new GridBagConstraints(1, 4, 1, 4, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                {
                    jTextAreaParamSpecs = new JTextArea();
                    jTextAreaParamSpecs.setText("[\"variable1\" [0 1 10]] \n[\"variable2\" [0.0 \"C\" 5.0]] \n[\"variable3\" \"moore\" \"vonN\"] \n[\"variable4\" true false]");
                    jTextAreaParamSpecs.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                    jTextAreaParamSpecs.setFont(new java.awt.Font("Monospaced",0,11));
                    jTextAreaParamSpecs.setAutoscrolls(true);
                    jScrollPane2.setViewportView(jTextAreaParamSpecs);
                }

            }
            {
                jLabel11 = new JLabel();
                getContentPane().add(jLabel11, new GridBagConstraints(1, 17, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jLabel11.setText("Search Encoding Representation");
                jLabel11.setFont(new java.awt.Font("SansSerif",1,11));
            }
            {
                ComboBoxModel<String> jComboBoxChromosomeTypeModel =
                    new DefaultComboBoxModel<String>(
                            new String[] { "MixedTypeChromosome" });
                jComboBoxChromosomeType = new JComboBox<String>();
                getContentPane().add(jComboBoxChromosomeType, new GridBagConstraints(1, 18, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jComboBoxChromosomeType.setModel(jComboBoxChromosomeTypeModel);
            }
            {
                jLabel13 = new JLabel();
                getContentPane().add(jLabel13, new GridBagConstraints(3, 12, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jLabel13.setText("Collected measure:  ");
                jLabel13.setFont(new java.awt.Font("SansSerif",1,11));
            }
            {
                ComboBoxModel<String> jComboBox1Model =
                    new DefaultComboBoxModel<String>(
                            new String[] { "Item One", "Item Two" });
                jComboBoxFitnessCollecting = new JComboBox<String>();
                getContentPane().add(jComboBoxFitnessCollecting, new GridBagConstraints(4, 12, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jComboBoxFitnessCollecting.setModel(jComboBox1Model);
            }
            {
                jLabel14 = new JLabel();
                getContentPane().add(jLabel14, new GridBagConstraints(3, 14, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jLabel14.setText("Combine replicates:  ");
                jLabel14.setFont(new java.awt.Font("SansSerif",1,11));
            }
            {
                jLabel15 = new JLabel();
                getContentPane().add(jLabel15, new GridBagConstraints(3, 8, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jLabel15.setText("Step Limit:  ");
                jLabel15.setFont(new java.awt.Font("SansSerif",1,11));
            }
            {
                jLabel16 = new JLabel();
                getContentPane().add(jLabel16, new GridBagConstraints(3, 11, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jLabel16.setText("Goal:  ");
                jLabel16.setFont(new java.awt.Font("SansSerif",1,11));
            }
            {
                ComboBoxModel<String> jComboBox1Model =
                    new DefaultComboBoxModel<String>(
                            new String[] { "Item One", "Item Two" });
                jComboBoxFitnessCombineReplications = new JComboBox<String>();
                getContentPane().add(jComboBoxFitnessCombineReplications, new GridBagConstraints(4, 14, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jComboBoxFitnessCombineReplications.setModel(jComboBox1Model);
            }
            {
                jLabel17 = new JLabel();
                getContentPane().add(jLabel17, new GridBagConstraints(3, 6, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jLabel17.setText("Measure If:  ");
                jLabel17.setFont(new java.awt.Font("SansSerif",1,11));
            }
            {
                jTextFieldMeasureIf = new JTextField();
                getContentPane().add(jTextFieldMeasureIf, new GridBagConstraints(4, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
                jTextFieldMeasureIf.setFont(new java.awt.Font("Monospaced",0,11));
                jTextFieldMeasureIf.setText("true");
                jTextFieldMeasureIf.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                jTextFieldMeasureIf.setToolTipText("e.g. \"(ticks mod 100) = 0\", or \"member? ticks [50 100 200]\"");
            }
            {
                jLabel9 = new JLabel();
                getContentPane().add(jLabel9, new GridBagConstraints(3, 16, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jLabel9.setText("Evaluation limit:  ");
                jLabel9.setFont(new java.awt.Font("SansSerif",1,11));
            }
            {
                jPanel1 = new JPanel();
                FlowLayout jPanel1Layout = new FlowLayout();
                jPanel1Layout.setAlignment(FlowLayout.LEFT);
                jPanel1.setLayout(jPanel1Layout);
                getContentPane().add(jPanel1, new GridBagConstraints(4, 16, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                jPanel1.setOpaque(false);
                {
                    jTextFieldEvaluationLimit = new JTextField();
                    jPanel1.add(jTextFieldEvaluationLimit);
                    jTextFieldEvaluationLimit.setFont(new java.awt.Font("Monospaced",0,11));
                    jTextFieldEvaluationLimit.setText("300");
                    jTextFieldEvaluationLimit.setPreferredSize(new java.awt.Dimension(50, 20));
                    jTextFieldEvaluationLimit.setToolTipText("Stop the search after this many model runs have occurred.");
                }
                {
                    jLabel18 = new JLabel();
                    jPanel1.add(jLabel18);
                    jLabel18.setText("model runs");
                }
            }
            {
                jLabel1 = new JLabel();
                getContentPane().add(jLabel1, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jLabel1.setText("Parameter Specification");
                jLabel1.setBounds(197, 32, 138, 19);
                jLabel1.setFont(new java.awt.Font("SansSerif",1,14));
                jLabel1.setForeground(new java.awt.Color(0,0,0));
                jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
            }
            {
                jButtonHelpSearchSpace = new JButton();
                getContentPane().add(jButtonHelpSearchSpace, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jButtonHelpSearchSpace.setText("?");
                jButtonHelpSearchSpace.setToolTipText("Help about Search Space Specification");
                jButtonHelpSearchSpace.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        actionHelpSearchSpace();
                    }
                });
            }
            {
                jButtonHelpSearchMethod = new JButton();
                getContentPane().add(jButtonHelpSearchMethod, new GridBagConstraints(1, 11, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jButtonHelpSearchMethod.setText("?");
                jButtonHelpSearchMethod.setToolTipText("Help about this Search Method");
                jButtonHelpSearchMethod.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        actionHelpSearchMethod();
                    }
                });

            }
            {
                ComboBoxModel<String> jComboBoxSearchMethodModel =
                    new DefaultComboBoxModel<String>(
                            new String[] { "xxxx", "yyyy" });
                jComboBoxSearchMethodType = new JComboBox<String>();
                getContentPane().add(jComboBoxSearchMethodType, new GridBagConstraints(1, 11, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jComboBoxSearchMethodType.setModel(jComboBoxSearchMethodModel);
                jComboBoxSearchMethodType.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent arg0) {
                        actionUpdateSearchMethodParams();
                    }
                });
            }
            {
                jButtonHelpEvaluation = new JButton();
                getContentPane().add(jButtonHelpEvaluation, new GridBagConstraints(4, 10, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jButtonHelpEvaluation.setText("?");
                jButtonHelpEvaluation.setToolTipText("Help about Evaluation");
                jButtonHelpEvaluation.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        actionHelpEvaluation();
                    }
                });
            }
            {
                jButtonSuggestParamRanges = new JButton();
                getContentPane().add(jButtonSuggestParamRanges, new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jButtonSuggestParamRanges.setText("Load param ranges from model interface");
                jButtonSuggestParamRanges.setToolTipText("Sets the search space specification based on sliders, choosers, etc., from model interface tab.");
                jButtonSuggestParamRanges.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        actionSuggestParamRanges();
                    }
                });
            }
            {
                jCheckBoxCaching = new JCheckBox();
                getContentPane().add(jCheckBoxCaching, new GridBagConstraints(1, 16, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jCheckBoxCaching.setText("Use fitness caching");
                jCheckBoxCaching.setToolTipText("If fitness caching is turned on then the result of running the model with certain parameters gets saved so the model won't be re-run if a run with those same parameters are requested again.");
                jCheckBoxCaching.setSelected(true);
            }
            {
                jLabel19 = new JLabel();
                getContentPane().add(jLabel19, new GridBagConstraints(3, 17, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jLabel19.setText("BestChecking replicates:  ");
                jLabel19.setFont(new java.awt.Font("SansSerif",1,11));
            }
            {
                jTextFieldBestChecking = new JTextField();
                getContentPane().add(jTextFieldBestChecking, new GridBagConstraints(4, 17, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jTextFieldBestChecking.setFont(new java.awt.Font("Monospaced",0,11));
                jTextFieldBestChecking.setText("10");
                jTextFieldBestChecking.setPreferredSize(new java.awt.Dimension(50,20));
                jTextFieldBestChecking.setOpaque(true);
                jTextFieldBestChecking.setMinimumSize(new java.awt.Dimension(50, 27));
                jTextFieldBestChecking.setToolTipText("BestChecking: running another N independent model runs to get an unbiased estimate of the objective function for each \"best\" individual that's found.");
            }
            {
                jButtonHelpSearchSpaceRepresentation = new JButton();
                getContentPane().add(jButtonHelpSearchSpaceRepresentation, new GridBagConstraints(1, 18, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jButtonHelpSearchSpaceRepresentation.setText("?");
                jButtonHelpSearchSpaceRepresentation.setToolTipText("Help about this Search Space Representation");
                jButtonHelpSearchSpaceRepresentation.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        actionHelpSearchSpaceRepresentation();
                    }
                });
            }
            {
                jPanelDeriv = new JPanel();
                jPanelDeriv.setBackground(new Color(214,217,223));
                jPanelDeriv.setLayout(new BorderLayout());
                getContentPane().add(jPanelDeriv, new GridBagConstraints(3, 15, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
                {
                    jCheckBoxTakeDerivative = new JCheckBox();
                    JPanel temp = new JPanel();
                    temp.setLayout( new FlowLayout(FlowLayout.CENTER, 40, 10) );
                    temp.add(jCheckBoxTakeDerivative);
                    jPanelDeriv.add(temp, BorderLayout.NORTH);
                    jCheckBoxTakeDerivative.setText("Take derivative?");
                    jCheckBoxTakeDerivative.setToolTipText("Instead of using the measure you've specified, use the *change* in that measure (with respect to a certain parameter) for your objective function.");
                    jCheckBoxTakeDerivative.setPreferredSize(new java.awt.Dimension(134, 22));
                    jCheckBoxTakeDerivative.setFont(new java.awt.Font("SansSerif",1,11));
                    jCheckBoxTakeDerivative.addItemListener(new ItemListener() {
                        public void itemStateChanged(ItemEvent arg0) {
                            updateFitnessDerivativePanel();
                        }
                    });

                    jCheckBoxFitnessDerivativeUseAbs = new JCheckBox();
                    jCheckBoxFitnessDerivativeUseAbs.setText("Use ABS value?");
                    jCheckBoxFitnessDerivativeUseAbs.setToolTipText("You might want to take the absolute value if you don't care about the direction of the measured change... e.g., for trying to find phase transitions");
                    jCheckBoxTakeDerivative.setFont(new java.awt.Font("SansSerif",1,11));
                    temp.add(jCheckBoxFitnessDerivativeUseAbs);
                }
                {
                    jPanelDeriv2 = new JPanel();
                    jPanelDeriv2.setOpaque(false);
                    jPanelDeriv.add(jPanelDeriv2,BorderLayout.CENTER);
                    {
                        jLabelDerivWRT = new JLabel();
                        jPanelDeriv2.add(jLabelDerivWRT);
                        jLabelDerivWRT.setText("w.r.t.");
                    }
                    {
                        ComboBoxModel<String> jComboBoxFitnessDerivativeParameterModel =
                            new DefaultComboBoxModel<String>(
                                    new String[] { "----" });
                        jComboBoxFitnessDerivativeParameter = new JComboBox<String>();
                        jComboBoxFitnessDerivativeParameter.setToolTipText("Which parameter should be varied by a small amount to see how much change results?");
                        Dimension d = jComboBoxFitnessDerivativeParameter.getPreferredSize();
                        jComboBoxFitnessDerivativeParameter.setPreferredSize(new Dimension(200,d.height));
                        jPanelDeriv2.add(jComboBoxFitnessDerivativeParameter);
                        jComboBoxFitnessDerivativeParameter.setModel(jComboBoxFitnessDerivativeParameterModel);
                        jComboBoxFitnessDerivativeParameter.addFocusListener(new FocusListener() {
                            public void focusGained(FocusEvent arg0) {
                                updateFitnessDerivativeParameterChoices();
                            }
                            public void focusLost(FocusEvent arg0) {
                            }
                        });
                    }
                    {
                        jLabelDerivDELTA = new JLabel();
                        jPanelDeriv2.add(jLabelDerivDELTA);
                        jLabelDerivDELTA.setText("\u0394=");
                    }
                    {
                        jTextFieldFitnessDerivativeDelta = new JTextField();
                        jPanelDeriv2.add(jTextFieldFitnessDerivativeDelta);
                        jTextFieldFitnessDerivativeDelta.setText("0.100");
                        jTextFieldFitnessDerivativeDelta.setToolTipText("How much should be subtracted from the parameter's value, to get the measured change?");
                        int prefHeight = jTextFieldFitnessDerivativeDelta.getPreferredSize().height;
                        jTextFieldFitnessDerivativeDelta.setPreferredSize(new Dimension(50, prefHeight));
                    }
                }
            }
            pack();
            this.setSize(760, 630);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //the special comment on the next line marks the remaining code so the Jigloo gui builder won't try to parse it.
    //$hide>>$

    private String defaultProtocolXMLForNewSearch;

    private File currentFile;
    private String lastSavedText;
    private HashMap<String,SearchMethod> searchMethodChoices = new HashMap<String, SearchMethod>();
    BehaviorSearch.RunOptions runOptions = null;    // keep track of what options they ran the search with last time,
                                                    // and use those as the offered options when they run again.
    private File defaultUserDocumentsFolder = new JFileChooser().getCurrentDirectory();

    public static void handleError(String msg, java.awt.Container parentContainer)
    {
        JOptionPane wrappingTextOptionPane = new JOptionPane(msg, JOptionPane.ERROR_MESSAGE) {
            private static final long serialVersionUID = 1L;
            @Override
            public int getMaxCharactersPerLineCount() { return 58; } };
        javax.swing.JDialog dialog = wrappingTextOptionPane.createDialog(parentContainer, "Error!");
        dialog.setVisible(true);
//      javax.swing.JOptionPane.showMessageDialog(this, msg, "ERROR!", JOptionPane.ERROR_MESSAGE);

    }
    private void handleError(String msg)
    {
        handleError(msg,this);
    }

    private void finishInitWork()
    {
        setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(GeneralUtils.getResource("icon_behaviorsearch.png").getAbsolutePath()));

        // Set a few keyboard shortcuts for menu item
        jMenuItemNew.setAccelerator( javax.swing.KeyStroke.getKeyStroke( 'N' ,
                java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() ) ) ;
        jMenuItemOpen.setAccelerator( javax.swing.KeyStroke.getKeyStroke( 'O' ,
                java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() ) ) ;
        jMenuItemSave.setAccelerator( javax.swing.KeyStroke.getKeyStroke( 'S' ,
                java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() ) ) ;

        // load our default 'new' search configuration from a file
        try {
            defaultProtocolXMLForNewSearch = GeneralUtils.stringContentsOfFile(GeneralUtils.getResource("defaultNewSearch.xml"));
        } catch (java.io.FileNotFoundException ex)
        {
            handleError(ex.getMessage());
            System.exit(1);
        }

        initComboBoxes();

        actionNew();
    }

    /*
     * Some of the choices for combo boxes need to be filled in dynamically, based on reading files and such.
     */
    private void initComboBoxes()
    {
        ////////////// SearchMethods
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) jComboBoxSearchMethodType.getModel();
        model.removeAllElements();
        List<String> names = null;
        try {
            names = SearchMethodLoader.getAllSearchMethodNames();
        } catch (BehaviorSearchException ex)
        {
            handleError(ex.getMessage());
            System.exit(1);
        }
        for (String name: names)
        {
            try {
                searchMethodChoices.put(name, SearchMethodLoader.createFromName(name));
                model.addElement(name);
            } catch (BehaviorSearchException ex) {
                handleError(ex.getMessage());
            }
        }
        ////////////// ChromosomeType
        model = (DefaultComboBoxModel<String>) jComboBoxChromosomeType.getModel();
        model.removeAllElements();
        names = null;
        try {
            names = bsearch.representations.ChromosomeTypeLoader.getAllChromosomeTypes();
        } catch (BehaviorSearchException ex)
        {
            handleError(ex.getMessage());
            System.exit(1);
        }
        for (String name: names)
        {
            model.addElement(name);
        }

        ////////////// FitnessCollecting
        model = (DefaultComboBoxModel<String>) jComboBoxFitnessCollecting.getModel();
        model.removeAllElements();
        for (SearchProtocol.FITNESS_COLLECTING f: SearchProtocol.FITNESS_COLLECTING.values())
        {
            model.addElement(f.toString());
        }

        ////////////// FitnessCollecting
        model = (DefaultComboBoxModel<String>) jComboBoxFitnessCombineReplications.getModel();
        model.removeAllElements();
        for (SearchProtocol.FITNESS_COMBINE_REPLICATIONS f: SearchProtocol.FITNESS_COMBINE_REPLICATIONS.values())
        {
            model.addElement(f.toString());
        }
    }

    private void updateFitnessDerivativePanel()
    {
        boolean enabled = jCheckBoxTakeDerivative.isSelected();
        jLabelDerivWRT.setEnabled(enabled);
        jLabelDerivDELTA.setEnabled(enabled);
        jComboBoxFitnessDerivativeParameter.setEnabled(enabled);
        jTextFieldFitnessDerivativeDelta.setEnabled(enabled);
        jCheckBoxFitnessDerivativeUseAbs.setEnabled(enabled);

        if (enabled)
        {
            updateFitnessDerivativeParameterChoices();
        }
    }
    private void updateFitnessDerivativeParameterChoices()
    {
        try {
            Object oldChoice = jComboBoxFitnessDerivativeParameter.getSelectedItem();
            String name = currentFile.toString();
            SearchSpace ss = new SearchSpace(java.util.Arrays.asList(jTextAreaParamSpecs.getText().split("\n")), name.endsWith(".nlogo3d") || name.endsWith(".nlogox3d"));
            DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) jComboBoxFitnessDerivativeParameter.getModel();
            model.removeAllElements();
            for (ParameterSpec spec : ss.getParamSpecs())
            {
                model.addElement(spec.getParameterName());
            }
            model.addElement("@MUTATE@");
            //TODO: Question: what if old choice not in new list of param
            jComboBoxFitnessDerivativeParameter.setSelectedItem(oldChoice);
        }
        catch (Exception ex) {  }
    }

    private void actionNew()
    {
        if (!checkDiscardOkay())
        {
            return;
        }
        currentFile = null;
/*      jTextAreaParamSpecs.setText("[\"integerParameter\" [0 1 10]] \n" +
                "[\"continuousParameter\" [0.0 \"C\" 1.0]] \n " +
                "[\"choiceParameter\" \"near\" \"far\"] \n");
                */
        SearchProtocol protocol;
        try {
            protocol = SearchProtocol.load(defaultProtocolXMLForNewSearch);
            loadProtocol(protocol);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Error loading default XML protocol to initialize UI!");
        } catch (SAXException e) {
            e.printStackTrace();
            throw new IllegalStateException("Error loading default XML protocol to initialize UI!");
        }
        this.setTitle("Untitled" + getWindowTitleSuffix());
    }
    private void actionOpen()
    {
        if (!checkDiscardOkay())
        {
            return;
        }

        JFileChooser chooser = new JFileChooser();
        if (currentFile != null)
        {
            chooser.setSelectedFile(currentFile);
        }
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Completed search configurations (*.xml)", "xml"));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Search protocols (*.bsearch)", "bsearch"));
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            openFile(chooser.getSelectedFile());
        }
    }
    private void actionOpenExample()
    {
        if (!checkDiscardOkay())
        {
            return;
        }
        JFileChooser chooser = new JFileChooser(GeneralUtils.attemptResolvePathFromBSearchRoot("examples"));
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Completed search configurations (*.xml)", "xml"));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Search protocols (*.bsearch)", "bsearch"));
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            openFile(chooser.getSelectedFile());
        }
    }

  // the following three "handle" methods are called reflectively by the
  // NetLogo mac application wrapper.
  // handleOpenPaht enables opening associated bsearch files on Mac OS X.
  public void handleOpenPath(String path) {
        File f = new File(path);
        if (f.exists())
        {
            openFile(f);
        }
  }

  // handleShowAbout enables the nice Mac OS X "About BehaviorSearch..." Item in
  // the mac "Behaviorsearch" menu.
    public void handleShowAbout() {
    actionHelpAbout();
  }

  // handleQuit picks up on quit called from the mac "Behaviorsearch" menu.
  public void handleQuit() throws org.nlogo.awt.UserCancelException {
        if (checkDiscardOkay())
    {
      System.exit(0);
    }
    else
    {
      throw new org.nlogo.awt.UserCancelException();
    }
  }

    private void openFile(File fProtocol)
    {
        try {
            SearchProtocol protocol = SearchProtocol.loadFile(fProtocol.getPath());

            currentFile = fProtocol;
            loadProtocol(protocol);
            this.setTitle(currentFile.getName() + getWindowTitleSuffix());
        } catch (IOException e) {
            handleError("IO Error occurred attempting to load file: " + fProtocol.getPath());
            e.printStackTrace();
        } catch (SAXException e) {
            handleError("XML Parsing error occurred attempting to load file: " + fProtocol.getPath());
            e.printStackTrace();
        }
    }
    private void loadProtocol(SearchProtocol protocol)
    {
        jTextFieldModelFile.setText(protocol.modelFile);
        StringBuilder sb = new StringBuilder();
        for (String s : protocol.paramSpecStrings)
        {
            sb.append(s); sb.append("\n");
        }
        jTextAreaParamSpecs.setText(sb.toString());
        jTextFieldModelStepCommands.setText(protocol.modelStepCommands);
        jTextFieldModelSetupCommands.setText(protocol.modelSetupCommands);
        jTextFieldModelStopCondition.setText(protocol.modelStopCondition);
        jTextFieldModelStepLimit.setText(Integer.toString(protocol.modelStepLimit));
        jTextFieldFitnessMetric.setText(protocol.modelMetricReporter);
        jTextFieldMeasureIf.setText(protocol.modelMeasureIf);
        jComboBoxFitnessMinMax.setSelectedItem(protocol.fitnessMinimized ? "Minimize Fitness" : "Maximize Fitness");
        jComboBoxFitnessCollecting.setSelectedItem(protocol.fitnessCollecting.toString());
        jTextFieldFitnessSamplingRepetitions.setText(Integer.toString(protocol.fitnessSamplingReplications));
        jComboBoxFitnessSamplingMethod.setSelectedItem((protocol.fitnessSamplingReplications != 0) ? "Fixed Sampling" : "Adaptive Sampling");
        jComboBoxFitnessCombineReplications.setSelectedItem(protocol.fitnessCombineReplications.toString());
        jCheckBoxTakeDerivative.setSelected(protocol.fitnessDerivativeParameter.length() > 0);
        jCheckBoxFitnessDerivativeUseAbs.setSelected(protocol.fitnessDerivativeUseAbs);
        updateFitnessDerivativePanel();
        jComboBoxFitnessDerivativeParameter.setSelectedItem(protocol.fitnessDerivativeParameter);
        jTextFieldFitnessDerivativeDelta.setText(Double.toString(protocol.fitnessDerivativeDelta));
        jComboBoxSearchMethodType.setSelectedItem(protocol.searchMethodType);
        jComboBoxChromosomeType.setSelectedItem(protocol.chromosomeType);
        updateSearchMethodParamTable(searchMethodChoices.get(protocol.searchMethodType),protocol.searchMethodParams);
        jCheckBoxCaching.setSelected(protocol.caching);
        jTextFieldBestChecking.setText(Integer.toString(protocol.bestCheckingNumReplications));
        jTextFieldEvaluationLimit.setText(Integer.toString(protocol.evaluationLimit));

        lastSavedText = protocol.toXMLString();
        runOptions = null; // reset the runOptions to defaults, when a different Protocol is loaded

    }
    private void actionSave()
    {
        if (currentFile == null)
        {
            actionSaveAs();
        }
        else
        {
            doSave();
        }
    }
    private void actionSaveAs()
    {
        JFileChooser chooser = new JFileChooser("./experiments/");
        if (currentFile != null)
        {
            chooser.setSelectedFile(currentFile);
        }
        else
        {
            chooser.setSelectedFile(new File("Untitled.bsearch"));
        }
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Search protocols (*.bsearch)", "bsearch"));
        int returnVal = chooser.showSaveDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            currentFile = chooser.getSelectedFile();
            doSave();
            this.setTitle(currentFile.getName() + getWindowTitleSuffix());
        }
    }
    private SearchProtocol createProtocolFromFormData() throws UIConstraintException
    {
        HashMap<String, String> searchMethodParams = new java.util.LinkedHashMap<String, String>();
        TableModel model = jTableSearchMethodParams.getModel();
        for (int i = 0; i < model.getRowCount(); i++)
        {
            searchMethodParams.put(model.getValueAt(i, 0).toString().trim(), model.getValueAt(i, 1).toString());
        }

        int modelStepLimit = 0;
        try {
            modelStepLimit = Integer.valueOf(jTextFieldModelStepLimit.getText());
            if (modelStepLimit < 0)
            {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex)
        {
            throw new UIConstraintException("STEP LIMIT should be a non-negative integer.", "Error: can't create search protocol");
        }

        int fitnessSamplingRepetitions = 0;
        if (jComboBoxFitnessSamplingMethod.getSelectedItem().toString().equals("Fixed Sampling"))
        {
            try {
                fitnessSamplingRepetitions = Integer.valueOf(jTextFieldFitnessSamplingRepetitions.getText());
                if (fitnessSamplingRepetitions < 0)
                {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex)
            {
                throw new UIConstraintException("SAMPLING REPETITIONS should be a positive integer, or 0 if using adaptive sampling.", "Error: can't create protocol");
            }
        }
        boolean caching = jCheckBoxCaching.isSelected();

        int evaluationLimit = 0;
        try {
            evaluationLimit = Integer.valueOf(jTextFieldEvaluationLimit.getText());
            if (evaluationLimit <= 0)
            {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex)
        {
            throw new UIConstraintException("EVALUATION LIMIT should be a positive integer.", "Error: can't create search protocol");
        }

        int bestCheckingNumReplications = 0;
        try {
            bestCheckingNumReplications = Integer.valueOf(jTextFieldBestChecking.getText());
            if (bestCheckingNumReplications < 0)
            {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex)
        {
            throw new UIConstraintException("The number of 'BEST CHECKING' replicates should be a non-negative integer.", "Error: can't create search protocol");
        }
        double fitnessDerivDelta = 0.0;
        if (jCheckBoxTakeDerivative.isSelected())
        {
            try {
                fitnessDerivDelta = Double.valueOf(jTextFieldFitnessDerivativeDelta.getText());
            } catch (NumberFormatException ex)
            {
                throw new UIConstraintException("The DELTA value (for taking the derivative of the objective fucntion with respect to a parameter) needs to be a number", "Error: can't create search protocol");
            }
        }

        SearchProtocol protocol = new SearchProtocol(jTextFieldModelFile.getText(),
                java.util.Arrays.asList(jTextAreaParamSpecs.getText().split("\n")),
                jTextFieldModelStepCommands.getText(), jTextFieldModelSetupCommands.getText(), jTextFieldModelStopCondition.getText(),
                modelStepLimit,
                jTextFieldFitnessMetric.getText(),
                jTextFieldMeasureIf.getText(),
                jComboBoxFitnessMinMax.getSelectedItem().toString().equals("Minimize Fitness"),
                fitnessSamplingRepetitions,
                SearchProtocol.FITNESS_COLLECTING.valueOf(jComboBoxFitnessCollecting.getSelectedItem().toString()),
                SearchProtocol.FITNESS_COMBINE_REPLICATIONS.valueOf(jComboBoxFitnessCombineReplications.getSelectedItem().toString()),
                jCheckBoxTakeDerivative.isSelected()?jComboBoxFitnessDerivativeParameter.getSelectedItem().toString():"",
                fitnessDerivDelta,
                jCheckBoxFitnessDerivativeUseAbs.isSelected(),
                jComboBoxSearchMethodType.getSelectedItem().toString(),
                searchMethodParams,
                jComboBoxChromosomeType.getSelectedItem().toString(),
                caching,
                evaluationLimit,
                bestCheckingNumReplications
                );

        return protocol;
    }
    private void doSave()
    {
            java.io.FileWriter fout;
            try {
                fout = new java.io.FileWriter(currentFile);
                SearchProtocol protocol = createProtocolFromFormData();
                protocol.save(fout);
                fout.close();
                lastSavedText = protocol.toXMLString();
                javax.swing.JOptionPane.showMessageDialog(this, "Saved successfully.", "Saved.", JOptionPane.PLAIN_MESSAGE);
            } catch (IOException ex) {
                ex.printStackTrace();
                handleError("IO Error occurred attempting to save file: " + currentFile.getPath());
            } catch (UIConstraintException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), ex.getTitle(), JOptionPane.WARNING_MESSAGE);
            }
    }
    private void actionExit()
    {
        if (!checkDiscardOkay())
        {
            return;
        }
        System.exit(0);
    }
    private boolean protocolChangedSinceLastSave()
    {
        String xmlStr = "";
        try {
            xmlStr = createProtocolFromFormData().toXMLString();
        } catch (UIConstraintException ex)
        {
            // if we can't create a valid protocol object from the form data, assume the user has changed something...
            return true;
        }
        //System.out.println(xmlStr);
        //System.out.println("--");
        //System.out.println(lastSavedText);

        // Note: lastSavedText == null ONLY when the GUI is being loaded for the first time.
        return (lastSavedText != null && !lastSavedText.equals(xmlStr));
    }
    private boolean checkDiscardOkay()
    {
        if (protocolChangedSinceLastSave())
        {
            if (JOptionPane.showConfirmDialog(this, "Discard changes you've made to this search experiment?", "Discard changes?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                    == JOptionPane.NO_OPTION)
            {
                return false;
            }
        }
        return true;
    }
    private void updateSearchMethodParamTable(SearchMethod searchMethod, HashMap<String,String> searchMethodParams)
    {
        //if the search method in the protocol is missing some parameters, fill them in with defaults
        HashMap<String, String> defaultParams = searchMethod.getSearchParams();
        for (String key : defaultParams.keySet())
        {
            if (!searchMethodParams.containsKey(key))
            {
                searchMethodParams.put(key, defaultParams.get(key));
            }
        }

        DefaultTableModel model = (DefaultTableModel) jTableSearchMethodParams.getModel();
        model.setRowCount(0);
        for (String s: searchMethodParams.keySet())
        {
            model.addRow(new Object[] { s , searchMethodParams.get(s) } );
        }
    }
    protected void actionUpdateSearchMethodParams()
    {
        if (!searchMethodChoices.isEmpty()) // make sure everything has been initialized...
        {
            SearchMethod searchMethod = searchMethodChoices.get(jComboBoxSearchMethodType.getSelectedItem());
            updateSearchMethodParamTable(searchMethod,searchMethod.getSearchParams());
        }
    }
    protected void actionSamplingMethodChanged()
    {
        if (jComboBoxFitnessSamplingMethod.getSelectedItem().equals("Adaptive Sampling"))
        {
            SearchMethod searchMethod = searchMethodChoices.get(jComboBoxSearchMethodType.getSelectedItem());
            if (!searchMethod.supportsAdaptiveSampling())
            {
                JOptionPane.showMessageDialog(this, "The currently selected search method doesn't support 'Adaptive Sampling'.", "WARNING", JOptionPane.WARNING_MESSAGE);
                jComboBoxFitnessSamplingMethod.setSelectedItem("Fixed Sampling");
            }
            else
            {
                jTextFieldFitnessSamplingRepetitions.setText("0");
                jTextFieldFitnessSamplingRepetitions.setEnabled(false);
            }
        }
        else
        {
            jTextFieldFitnessSamplingRepetitions.setEnabled(true);
            if (jTextFieldFitnessSamplingRepetitions.getText().trim().equals("0"))
            {
                jTextFieldFitnessSamplingRepetitions.setText("10");
            }
        }
    }
    protected void actionBrowseModel()
    {
        JFileChooser chooser = new JFileChooser(System.getProperty("bsearch.startupfolder", "."));
        chooser.setSelectedFile(new File(jTextFieldModelFile.getText()));
        chooser.setFileFilter(new FileNameExtensionFilter("NetLogo Models", "nlogo"));
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            jTextFieldModelFile.setText(chooser.getSelectedFile().getPath());
        }
    }
    protected void actionSuggestParamRanges()
    {
        try {
            jTextAreaParamSpecs.setText(bsearch.nlogolink.Utils.getDefaultConstraintsText(jTextFieldModelFile.getText()));
        } catch (NetLogoLinkException e)
        {
            handleError(e.getMessage());
        }
    }

    protected void actionRunNow() {
        SearchProtocol protocol;
        try {
            protocol = createProtocolFromFormData();
        } catch (UIConstraintException e) {
            handleError("Error creating SearchProtocol: " + e.getMessage());
            return;
        }

/*      while (currentFile == null || protocolChangedSinceLastSave())
        {
            int choice = javax.swing.JOptionPane.showConfirmDialog(this, "Protocol must be saved before running the search.  Save now?" ,  "Save protocol?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.OK_OPTION)
            {
                actionSave();
            }
            else
            {
                return;
            }
        } */

        if (runOptions == null)
        {
            runOptions = new BehaviorSearch.RunOptions();
            //suggest a filename stem for output files, which users can change.
            if (currentFile != null)
            {
                String fnameStem = currentFile.getPath();
                fnameStem = fnameStem.substring(0, fnameStem.lastIndexOf('.'));
                fnameStem = GeneralUtils.attemptResolvePathFromStartupFolder(fnameStem);
                runOptions.outputStem = fnameStem;
            }
            else
            {
                //TODO: Use folder where the NetLogo model is located instead?
                runOptions.outputStem =  new File(defaultUserDocumentsFolder, "mySearchOutput").getPath();
            }
        }
        if (currentFile != null)
        {
            runOptions.protocolFilename = this.currentFile.getAbsolutePath();
        }

        if (RunOptionsDialog.showDialog(this, runOptions))
        {
            GUIProgressDialog dialog = new GUIProgressDialog(this);
            dialog.setLocationRelativeTo(null);
            dialog.startSearchTask(protocol, runOptions);
            dialog.setVisible(true);
        }
    }

    private void actionHelpSearchMethod()
    {
        SearchMethod sm = searchMethodChoices.get(jComboBoxSearchMethodType.getSelectedItem());
        HelpInfoDialog.showHelp(this, "Help about " + sm.getName(), sm.getHTMLHelpText());
    }
    private void actionHelpSearchSpaceRepresentation()
    {
        String chromosomeType = jComboBoxChromosomeType.getSelectedItem().toString();
        try {
            ChromosomeFactory factory = ChromosomeTypeLoader.createFromName(chromosomeType);

            HelpInfoDialog.showHelp(this, "Help about " + chromosomeType, factory.getHTMLHelpText() + "<BR><BR>");
        } catch (BehaviorSearchException ex)
        {
            handleError(ex.toString());
        }
    }
    private void actionHelpSearchSpace() {
        HelpInfoDialog.showHelp(this, "Help about search space specification", "<HTML><BODY>" +
                "Specifying the range of parameters to be searched works much the same as the BehaviorSpace tool in NetLogo:" +
                "<PRE> [ \"PARAM_NAME\" VALUE1 VALUE2 VALUE3 ... ] </PRE>" +
                "or <PRE> [ \"PARAM_NAME\" [RANGE_START INCREMENT RANGE_END] ] </PRE>" +
                "<P>One slight difference is that INCREMENT may be \"C\", which means to search the range continously " +
                "(or at least with fine resolution, if the chromosomal representation doesn't allow for continuous parameters)</P>" +
                "</BODY></HTML>");
    }
    private void actionHelpEvaluation() {
        //TODO: Better help docs
        HelpInfoDialog.showHelp(this, "Help about fitness evaluation", "<HTML><BODY>" +
                "An objective function must condense the data collected from multiple model runs into a single number, "
                + "which is what the search process will either attempt to minimize or maximize." +
                "</BODY></HTML>");
    }

    private void actionHelpTutorial() {
        org.nlogo.swing.BrowserLauncher.openPath(this,
      Path.of(GeneralUtils.attemptResolvePathFromBSearchRoot("documentation/tutorial.html")));
    }

    private void actionHelpAbout() {
        HelpAboutDialog.showAboutDialog(this);
    }


    public class UIConstraintException extends Exception
    {
        private String title;

        public UIConstraintException(String msg, String title) {
            super(msg);
            this.title = title;
        }
        public String getTitle()
        {
            return title;
        }
        private static final long serialVersionUID = 1L;
    }


    //$hide<<$
}
