package bsearch.app;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.nlogo.api.MersenneTwisterFast;



public class RunOptionsDialog extends javax.swing.JDialog {

    private static final long serialVersionUID = 1L;
    private JPanel jPanelNorth;
    private JPanel jPanelSouth;
    private JButton jButtonCancel;
    private JLabel jLabelNumSearches;
    private JSpinner jSpinnerThreads;
    private JLabel jLabelThreads;
    private JSpinner jSpinnerNumSearches;
    private JPanel jPanelCenter;
    private JButton jButtonStartSearch;

    private boolean dialogCanceled = true;
    private JCheckBox jCheckBoxBriefOutput;
    private JButton jButtonNewSeed;
    private JPanel jPanel1;
    private JSpinner jSpinnerFirstSearchNumber;
    private JLabel jLabelFirstSearchNumber;
    private JSpinner jSpinnerRandomSeed;
    private JLabel jLabelRandomSeed;
    private JButton jButtonBrowseStem;
    private JTextField jTextFieldOutputStem;
    private JLabel jLabelOutputStem;

    public RunOptionsDialog(JFrame frame, BehaviorSearch.RunOptions runOptions) {
        super(frame, "Choose experiment running options");

        {
            jPanelNorth = new JPanel();
            BorderLayout jPanelNorthLayout = new BorderLayout();
            jPanelNorth.setLayout(jPanelNorthLayout);
            getContentPane().add(jPanelNorth, BorderLayout.NORTH);
            {
                jLabelOutputStem = new JLabel();
                jPanelNorth.add(jLabelOutputStem, BorderLayout.WEST);
                jLabelOutputStem.setText("Output file stem:  ");
                jLabelOutputStem.setFont(new java.awt.Font("Dialog",1,12));
            }
            {
                jTextFieldOutputStem = new JTextField();
                jPanelNorth.add(jTextFieldOutputStem, BorderLayout.CENTER);
                jTextFieldOutputStem.setText("/path/to/some/filename/stem");
                jTextFieldOutputStem.setToolTipText("All of the output files will start with the same filename \"stem\" (e.g.  MYSTEM.bestHistory.csv, MYSTEM.finalBests.csv)");
            }
            {
                jButtonBrowseStem = new JButton();
                jPanelNorth.add(jButtonBrowseStem, BorderLayout.EAST);
                jButtonBrowseStem.setText("Browse...");
                jButtonBrowseStem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser chooser = new JFileChooser("./experiments/");
                        chooser.setSelectedFile(new File(jTextFieldOutputStem.getText()));
                        int returnVal = chooser.showSaveDialog(RunOptionsDialog.this);
                        if(returnVal == JFileChooser.APPROVE_OPTION)
                        {
                            jTextFieldOutputStem.setText(chooser.getSelectedFile().getPath());
                        }

                    }
                });

            }
            jPanelNorth.setPreferredSize(new java.awt.Dimension(190, 35));
        }
        {
            jPanelSouth = new JPanel();
            FlowLayout jPanelSouthLayout = new FlowLayout();
            jPanelSouthLayout.setAlignment(FlowLayout.RIGHT);
            jPanelSouth.setLayout(jPanelSouthLayout);
            getContentPane().add(jPanelSouth, BorderLayout.SOUTH);
            jPanelSouth.setPreferredSize(new java.awt.Dimension(190, 35));
            {
                jButtonStartSearch = new JButton("Start Search");
                jPanelSouth.add(jButtonStartSearch);
                jButtonStartSearch.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        dialogCanceled = false;
                        setVisible(false);
                    }
                });
            }
            {
                jButtonCancel = new JButton();
                jPanelSouth.add(jButtonCancel);
                jButtonCancel.setText("Cancel");
                jButtonCancel.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        setVisible(false);
                    }
                });

            }
        }
        {
            jPanelCenter = new JPanel();
            getContentPane().add(jPanelCenter, BorderLayout.CENTER);
            GridBagLayout jPanel2Layout = new GridBagLayout();
            jPanel2Layout.columnWidths = new int[] {7, 7};
            jPanel2Layout.rowHeights = new int[] {7, 7, 7, 7, 7};
            jPanel2Layout.columnWeights = new double[] {0.1, 0.1};
            jPanel2Layout.rowWeights = new double[] {0.1, 0.1, 0.1, 0.1, 0.1};
            jPanelCenter.setLayout(jPanel2Layout);
            {
                jLabelNumSearches = new JLabel();
                jPanelCenter.add(jLabelNumSearches, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jLabelNumSearches.setText("Number of searches:");
                jLabelNumSearches.setFont(new java.awt.Font("Dialog",1,12));
            }
            {
                SpinnerNumberModel jSpinnerNumSearchesModel =
                    new SpinnerNumberModel(1,1,10000,1);
                jSpinnerNumSearches = new JSpinner();
                jPanelCenter.add(jSpinnerNumSearches, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
                jSpinnerNumSearches.setModel(jSpinnerNumSearchesModel);
                jSpinnerNumSearches.setToolTipText("How many times should this search be repeated?");
            }
            {
                jLabelThreads = new JLabel();
                jPanelCenter.add(jLabelThreads, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jLabelThreads.setText("Number of threads:");
                jLabelThreads.setToolTipText("(only affects search time performance, not search results)");
                jLabelThreads.setFont(new java.awt.Font("SansSerif",1,12));
            }
            {
                SpinnerNumberModel jSpinnerThreadsModel =
                    new SpinnerNumberModel(1,1,32,1);
                jSpinnerThreads = new JSpinner();
                jPanelCenter.add(jSpinnerThreads, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
                jSpinnerThreads.setModel(jSpinnerThreadsModel);
                jSpinnerThreads.setToolTipText("(only affects search time performance, not search results)");
            }
            {
                jLabelRandomSeed = new JLabel();
                jPanelCenter.add(jLabelRandomSeed, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jLabelRandomSeed.setText("Initial random seed:");
                jLabelRandomSeed.setFont(new java.awt.Font("SansSerif",1,12));
            }
            {
                jPanel1 = new JPanel();
                BorderLayout jPanel1Layout = new BorderLayout();
                jPanel1.setLayout(jPanel1Layout);
                jPanelCenter.add(jPanel1, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
                {
                    SpinnerNumberModel jSpinnerRandomSeedModel =
                        new SpinnerNumberModel(1,Integer.MIN_VALUE,Integer.MAX_VALUE,1);

                    jSpinnerRandomSeed = new JSpinner();
                    jPanel1.add(jSpinnerRandomSeed, BorderLayout.CENTER);
                    jSpinnerRandomSeed.setModel(jSpinnerRandomSeedModel);
                    jSpinnerRandomSeed.setToolTipText("random seed to be used for the first search (additional searches will be seeded with following consecutive numbers)");
                }
                {
                    jButtonNewSeed = new JButton();
                    jPanel1.add(jButtonNewSeed, BorderLayout.EAST);
                    jButtonNewSeed.setText("New");
                    jButtonNewSeed.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            jSpinnerRandomSeed.setValue(new MersenneTwisterFast().nextInt());
                        }
                    });
                }
            }
            {
                jLabelFirstSearchNumber = new JLabel();
                jPanelCenter.add(jLabelFirstSearchNumber, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jLabelFirstSearchNumber.setText("Starting at search ID:");
                jLabelFirstSearchNumber.setFont(new java.awt.Font("Dialog",1,12));
            }
            {
                SpinnerNumberModel jSpinnerFirstSearchNumberModel = new SpinnerNumberModel(1,0,10000,1);

                jSpinnerFirstSearchNumber = new JSpinner();
                jPanelCenter.add(jSpinnerFirstSearchNumber, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
                jSpinnerFirstSearchNumber.setModel(jSpinnerFirstSearchNumberModel);
                jSpinnerFirstSearchNumber.setToolTipText("What number should the \"search ID\" numbers start at, in the output files?");
            }
            {
                jCheckBoxBriefOutput = new JCheckBox();
                jPanelCenter.add(jCheckBoxBriefOutput, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                jCheckBoxBriefOutput.setText("Brief Output?");
                jCheckBoxBriefOutput.setToolTipText("Do not create the two largest files, with ALL of the model run and objective function data.");
                jCheckBoxBriefOutput.setFont(new java.awt.Font("Dialog",1,12));
            }
        }
        // make sure the OK button always gets focus, so it's easy to close the help dialog.
        this.addWindowFocusListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                jButtonStartSearch.requestFocusInWindow();
            }
        });

        init(runOptions);
        this.setPreferredSize(new java.awt.Dimension(600,280));
        this.pack();  // size the window based on the textpane's contents
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    //the special comment on the next line marks the remaining code so the Jigloo gui builder won't try to parse it.
    //$hide>>$

    public void init(BehaviorSearch.RunOptions runOptions)
    {
        jTextFieldOutputStem.setText(runOptions.outputStem);
        jSpinnerNumSearches.setValue(runOptions.numSearches);
        jSpinnerFirstSearchNumber.setValue(runOptions.firstSearchNumber);
        jSpinnerThreads.setValue(runOptions.numThreads);
        jSpinnerRandomSeed.setValue(runOptions.randomSeed.intValue());
        jCheckBoxBriefOutput.setSelected(runOptions.briefOutput);

    }
    public void updateOptions(BehaviorSearch.RunOptions runOptions)
    {
        //TODO: input validation for this dialog, and don't let them press OK to close unless it's valid.
        runOptions.outputStem = jTextFieldOutputStem.getText();
        runOptions.numSearches = (Integer) jSpinnerNumSearches.getValue();
        runOptions.firstSearchNumber = (Integer) jSpinnerFirstSearchNumber.getValue();
        runOptions.numThreads = (Integer) jSpinnerThreads.getValue();
        runOptions.randomSeed = (Integer) jSpinnerRandomSeed.getValue();
        runOptions.briefOutput = jCheckBoxBriefOutput.isSelected();

    }

    /**
     * @param parent - parent Frame
     * @param runOptions - search options object, which will be modified by this dialog
     * @return true if OK was pressed, false if dialog was canceled.
     */
    public static boolean showDialog(JFrame parent, BehaviorSearch.RunOptions runOptions)
    {
        //TODO: make GUI options for shortened output, and suppressing all model results...
        //TODO: make the GENERATE-random-seed button!

        RunOptionsDialog dialog = new RunOptionsDialog(parent, runOptions);
        dialog.setLocationRelativeTo(null);
        dialog.setModalityType(ModalityType.APPLICATION_MODAL);
        dialog.setVisible(true);
        if (dialog.dialogCanceled)
        {
            return false;
        }
        dialog.updateOptions(runOptions);
        return true;
    }

    //$hide<<$
}
