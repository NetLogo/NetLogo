package bsearch.app;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import bsearch.algorithms.SearchParameterException;
import bsearch.evaluation.ResultListener;
import bsearch.evaluation.SearchManager;
import bsearch.nlogolink.ModelRunResult;
import bsearch.nlogolink.ModelRunner.ModelRunnerException;
import bsearch.representations.Chromosome;
import bsearch.util.GeneralUtils;

public class GUIProgressDialog extends JDialog implements PropertyChangeListener, ActionListener, WindowListener {

    private static final long serialVersionUID = 1L;
    private JProgressBar progressBar;
    private JLabel labelMessage;
    private JLabel labelTimeRemaining;
    private JTextPane infoTextPane;
    private JButton buttonCancel;
    private SearchTask task;
    private long taskStartTime;

    JFreeChart fitnessPlot;
    private XYSeriesCollection fitnessPlotDataset;

    private boolean done = false;

    public GUIProgressDialog(Frame parent) {
        //super("BehaviorSearch - *running* "); //TODO: show name of search in the title bar
        super(parent,true);

        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(this);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        labelMessage = new JLabel("Performing search 0000 out of 0000");
        labelMessage.setFont(new Font("Arial", Font.BOLD, 12));

        labelTimeRemaining = new JLabel("Elapsed: ? Remaining: ?");
        labelTimeRemaining.setFont(new Font("Arial", Font.PLAIN, 12));

        infoTextPane = new JTextPane();
        infoTextPane.setEditable(false);
        infoTextPane.setBorder(BorderFactory.createEmptyBorder(30,5,10,12));
        infoTextPane.setContentType("text/html");
        infoTextPane.setText("<B><I>Search result status</I></B><BR>");

        buttonCancel = new JButton("Cancel");
        buttonCancel.addActionListener(this);

        fitnessPlotDataset = new XYSeriesCollection();

        fitnessPlot = ChartFactory.createXYLineChart("Search Progress",
                            "# of model runs", "Fitness", fitnessPlotDataset,
                            PlotOrientation.VERTICAL, true, false, false);
        final XYPlot plot = fitnessPlot.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);

        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setBaseShapesVisible(false);
        //renderer.setAutoPopulateSeriesShape(false);  // don't use different shapes for each series...
        //renderer.setBaseShape(new java.awt.geom.Ellipse2D.Double(-1,-1,3,3));  // smaller datapoint size...
        renderer.setBaseLegendTextFont(new Font("Arial",Font.PLAIN, 10));
        plot.setRenderer(renderer);

        ChartPanel cPanel = new ChartPanel(fitnessPlot);
        cPanel.setPreferredSize(new Dimension(500,400));

        JPanel panelSouth = new JPanel();
        panelSouth.setLayout(new BorderLayout());

        panelSouth.add(labelMessage, BorderLayout.WEST);
        panelSouth.add(progressBar, BorderLayout.CENTER);
        panelSouth.add(labelTimeRemaining, BorderLayout.EAST);
        panelSouth.add(buttonCancel, BorderLayout.SOUTH);

        JPanel panelEast = new JPanel();
        panelEast.setLayout(new BorderLayout());
        panelEast.add(new JScrollPane(infoTextPane),  BorderLayout.CENTER);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        panel.add(panelEast, BorderLayout.EAST);
        panel.add(cPanel, BorderLayout.CENTER);
        panel.add(panelSouth,BorderLayout.SOUTH);

        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(panel, BorderLayout.CENTER);
        this.pack();
    }

    public void startSearchTask(SearchProtocol protocol, BehaviorSearch.RunOptions runOptions)
    {


        if (protocol.useBestChecking())
        {
            ValueAxis rangeAxis = fitnessPlot.getXYPlot().getRangeAxis();
            rangeAxis.setLabel(rangeAxis.getLabel() + " (rechecked)");
        }

        labelMessage.setText("Search 0 of " + runOptions.numSearches);
        progressBar.setIndeterminate(true);
        progressBar.setMaximum(protocol.evaluationLimit);
        taskStartTime = System.currentTimeMillis();
        task = new SearchTask(protocol, runOptions);
        task.addPropertyChangeListener(this);
        task.execute();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if ("state".equals(evt.getPropertyName()))
        {
            if (evt.getNewValue().equals(SwingWorker.StateValue.DONE))
            {
                try {
                    if (task.isCancelled() )
                    {
                        javax.swing.JOptionPane.showMessageDialog(this, "You canceled the search.  \nPartial results may have been saved to output files.",  "Canceled", JOptionPane.WARNING_MESSAGE);
                    }
                    else if (task.fatalException != null)
                    {
                        throw task.fatalException;
                    }
                }
                catch (ModelRunnerException e) {
                    e.printStackTrace();
                    BehaviorSearchGUI.handleError("Error running the model:\n" + e.getMessage(), this.getParent());
                } catch (SearchParameterException e) {
                    e.printStackTrace();
                    BehaviorSearchGUI.handleError("Error setting search method parameters:\n" + e.getMessage(), this.getParent());
                }
                catch (IOException e) {
                    BehaviorSearchGUI.handleError("Error reading or writing files:\n" + e, this.getParent());
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                    BehaviorSearchGUI.handleError("Error: " + e, this.getParent());
                } catch (Throwable e) {
                    e.printStackTrace();
                    BehaviorSearchGUI.handleError("Serious Error: " + e, this.getParent());
                }
                done = true;
                buttonCancel.setText("Done");
            }
        }
    }
    public void actionPerformed(ActionEvent arg0) {
        if (arg0.getSource().equals(buttonCancel))
        {
            doCancel();
        }
    }
    private void doCancel()
    {
        if (done)
        {
            setVisible(false);
        }
        else
        {
            progressBar.setIndeterminate(false);
            task.cancel(true);
        }
    }


    class SearchTask extends SwingWorker<Void, Void>  implements ResultListener
    {
        private SearchProtocol protocol;
        private BehaviorSearch.RunOptions runOptions;
        private Throwable fatalException = null;

        public SearchTask(SearchProtocol protocol, BehaviorSearch.RunOptions runOptions)
        {
            this.protocol = protocol;
            this.runOptions = runOptions;
        }
        public void updateGUIForNextSearch(final int searchNum)
        {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    labelMessage.setText("Performing search " + (searchNum - runOptions.firstSearchNumber + 1) + " of " + runOptions.numSearches + ":  ");
                    progressBar.setValue(0);

                    XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) fitnessPlot.getXYPlot().getRenderer();
                    if (searchNum > runOptions.firstSearchNumber)
                    {
                        renderer.setSeriesStroke(searchNum - runOptions.firstSearchNumber - 1, new BasicStroke(1.0f));
                    }
                    renderer.setSeriesStroke(searchNum - runOptions.firstSearchNumber, new BasicStroke(3.0f));
                }
            });
        }
        public void updateGUIWhenFinished()
        {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    labelMessage.setText("Finished search " + runOptions.numSearches + " of " + runOptions.numSearches + ":  ");
                    progressBar.setValue(progressBar.getMaximum());

                    XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) fitnessPlot.getXYPlot().getRenderer();
                    renderer.setSeriesStroke(runOptions.numSearches - 1, new BasicStroke(1.0f));
                }
            });
        }
        @Override
        public Void doInBackground() {
            try {
                List<ResultListener> listeners = new ArrayList<ResultListener>();
                listeners.add(this);
                BehaviorSearch.runWithOptions(runOptions, protocol, listeners);
                updateGUIWhenFinished();

            } catch (Throwable e) {
                fatalException = e;  // handle it later, back in the regular Swing event thread
            }
            return null;
        }

        public void modelRunOccurred(SearchManager manager, Chromosome point, ModelRunResult result ) {
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime-taskStartTime;
            String elapsedStr = GeneralUtils.formatTimeNicely(elapsed);
            int searchesCompleted = (manager.getSearchIDNumber() - runOptions.firstSearchNumber);
            int totalSearches = runOptions.numSearches;
            double runsCompleted = manager.getEvaluationCount() + manager.getAuxilliaryEvaluationCount();
            double totalRuns = protocol.evaluationLimit + manager.getAuxilliaryEvaluationCount();
            double searchProgress = (searchesCompleted + runsCompleted / totalRuns) / totalSearches;

            long remaining = (long) (elapsed / searchProgress - elapsed); // in milliseconds
            String remainingStr = GeneralUtils.formatTimeNicely(remaining);
            labelTimeRemaining.setText("  (" + elapsedStr + " elapsed - " + remainingStr + " remaining)");
        }

        public void fitnessComputed(SearchManager manager, Chromosome point, double fitness) {
            final int searchNumber = manager.getSearchIDNumber();
            final double bestFitnessSoFar = protocol.useBestChecking() ? manager.getCurrentBestFitnessCheckedEstimate() :  manager.getCurrentBestFitness();

            final int evaluationCount = manager.getEvaluationCount();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(evaluationCount);

                    // enlarge the x-axis range if necessary, to show progress
                    ValueAxis axis = fitnessPlot.getXYPlot().getDomainAxis();
                    if (axis.getRange().getUpperBound() < evaluationCount + 1)
                    {
                        axis.setRange(0, evaluationCount + 1);
                    }

                    if (searchNumber - runOptions.firstSearchNumber + 1 > fitnessPlotDataset.getSeriesCount())
                    {
                        XYSeries series = new XYSeries("Search " + searchNumber);
                        fitnessPlotDataset.addSeries(series);
                    }
                    XYSeries series = fitnessPlotDataset.getSeries(searchNumber - runOptions.firstSearchNumber);
                    int itemCount = series.getItemCount();
                    if (itemCount == 0 || evaluationCount > series.getX(itemCount - 1).intValue()
                            || (bestFitnessSoFar != series.getY(itemCount - 1).doubleValue()))
                    {
                        // This is a preventive measure to not bog down real-time graphing in JFreeChart:
                        // if the fitness is on a plateau, we don't need a million XY data points to show a flat line
                        // we can use just 2 points (one at the beginning of the plateau, and one at the end)
                        if (itemCount > 1
                                && series.getY(itemCount - 1).doubleValue() == bestFitnessSoFar
                                && series.getY(itemCount - 2).doubleValue() == bestFitnessSoFar)
                        {
                            series.remove(itemCount - 1);
                        }
                        series.addOrUpdate(evaluationCount, bestFitnessSoFar);
                    }
                }
            });
        }
        private void updateInfoText(String title, Chromosome c, double fitness, double checkedFitness)
        {
            String bestText = GeneralUtils.getParamSettingsTextHTML(c.getParamSettings());
            StringBuilder sb = new StringBuilder();
            sb.append("<B><I>"+title+"</I></B>");
            sb.append("<BR><BR><B>Best found so far:</B><BR>");
            sb.append(bestText);
            sb.append("<BR><B>Fitness</B>=");
            sb.append(String.format("%10.6g",fitness));
            sb.append("<BR>");
            if (protocol.useBestChecking())
            {
                sb.append("<B>(re-checked)</B>=");
                sb.append(String.format("%10.6g",checkedFitness));
                sb.append("<BR>");
            }
            infoTextPane.setText(sb.toString());

        }
        public void newBestFound(SearchManager manager) {
            updateInfoText("In Search #" + manager.getSearchIDNumber() + ":", manager.getCurrentBest(), manager.getCurrentBestFitness(), manager.getCurrentBestFitnessCheckedEstimate());
        }
        public void initListener(bsearch.space.SearchSpace space) {
            // do nothing
        }
        public void searchStarting(SearchManager archive) {
            updateGUIForNextSearch(archive.getSearchIDNumber());
        }

        Chromosome overallBest = null;
        double overallBestFitness;
        double overallBestFitnessChecked;

        public void searchFinished(SearchManager manager) {
            double bestFitness = manager.getCurrentBestFitness();
            if (overallBest == null || manager.fitnessStrictlyBetter(bestFitness, overallBestFitness))
            {
                overallBest = manager.getCurrentBest();
                overallBestFitness = bestFitness;
                if (protocol.useBestChecking())
                {
                    overallBestFitnessChecked = manager.getCurrentBestFitnessCheckedEstimate();
                }
            }

        }
        public void allSearchesFinished() {
            updateInfoText("From all searches:", overallBest, overallBestFitness, overallBestFitnessChecked);
        }
        public void searchesAborted() {
            // do nothing
        }
    }

    public void windowClosing(WindowEvent arg0) {
        doCancel();
    }

    public void windowActivated(WindowEvent arg0) {
    }

    public void windowClosed(WindowEvent arg0) {
    }

    public void windowDeactivated(WindowEvent arg0) {
    }

    public void windowDeiconified(WindowEvent arg0) {
    }

    public void windowIconified(WindowEvent arg0) {
    }

    public void windowOpened(WindowEvent arg0) {
    }


}
