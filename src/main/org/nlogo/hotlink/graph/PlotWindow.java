package org.nlogo.hotlink.graph;

import org.nlogo.hotlink.graph.annotation.AnnotationManager;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.ArrayList;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.nlogo.hotlink.main.MainWindow;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.data.general.AbstractDataset;

import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.JFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

public abstract class PlotWindow extends javax.swing.JPanel
				   implements javax.swing.event.ChangeListener,
				   			  org.jfree.chart.ChartMouseListener,
                              javax.swing.event.MouseInputListener {
	Frame mainWindow;
	PenCollection penCollection;
    PlotPanel plotPanel;
    AnnotationManager annotationManager;
    JFreeChart chart;
    ChartPanel chartPanel;
    Plot plot;
    org.nlogo.plot.Plot netLogoPlot;
    ArrayList<AbstractDataset> dataPerRun;
	
	PlotWindow( Frame mainWindow , PenCollection penCollection , PlotPanel plotPanel , Plot plot , org.nlogo.plot.Plot netLogoPlot ) {
		this.mainWindow = mainWindow;
		this.penCollection = penCollection;
        this.plotPanel = plotPanel;
        this.plot = plot;
        this.netLogoPlot = netLogoPlot;
	}
	
	public void addNewTick() {}
	public void addDataPoint( int run, int series , double x , double y ) {}
	public void addDataPoint( int run, int series , double[][] data ) {}
    public void saveWithAnnotations( String saveDirectory ) throws IOException {
        File savePlot = new File( saveDirectory + "/" + plot.getName() + ".png" );
        savePlot.createNewFile();
        plot.getPlot().getAnnotationManager().showAllAnnotationRegions();
        Image image = chart.createBufferedImage(300, 150);
        ImageIO.write((RenderedImage) image, "png", savePlot);
    }

	// ================ Mouse Events
    // ======== jfree chartMouse stuff
	public void stateChanged( ChangeEvent e ) {}
	public void chartMouseClicked(ChartMouseEvent arg0) {}
	public void chartMouseMoved(ChartMouseEvent arg0) {}
    // ======== swing mouse stuff
    public void mouseClicked(MouseEvent e) { }
    public void mousePressed(MouseEvent e) { }
    public void mouseReleased(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    public void mouseDragged(MouseEvent e) { }
    public void mouseMoved(MouseEvent e) { }

	public void setTick( int tick ) {}
	public void rePaint() {}
    public PlotPanel getPlotPanel() {
        return plotPanel;
    }
    public JFreeChart getChart() {
        return chart;
    }

    public AnnotationManager getAnnotationManager() {
        return annotationManager;
    }

    public Insets getInsets() { 
        return new Insets( -7 , -2 , -10 , -15 );
    }

    public String getName() {
        return plot.getName();
    }
}