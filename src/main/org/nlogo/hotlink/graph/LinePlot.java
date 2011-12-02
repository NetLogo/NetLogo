package org.nlogo.hotlink.graph;

import java.awt.Color;
import java.awt.Frame;
import java.awt.BasicStroke;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import javax.swing.JFrame;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.general.AbstractDataset;
import org.nlogo.hotlink.main.MainWindow;
import org.nlogo.hotlink.graph.annotation.*;
import org.nlogo.hotlink.view.ViewPanel;
import org.nlogo.app.PlotTab;

import org.jfree.data.xy.*;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.renderer.xy.XYShapeRenderer;

import javax.swing.JCheckBox;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;

public class LinePlot extends PlotWindow {
    // activeDataSet is the set with the pens I want to see
    private XYSeriesCollection activeDataset = new XYSeriesCollection();
    private XYSeriesCollection dataset = new XYSeriesCollection();
    PenCollection penCollection;
    ArrayList<XYSeriesCollection> dataPerRun = new ArrayList<XYSeriesCollection>();

    ViewPanel viewPanel;

    private double maxRange = 5;
    public double maxDomain = 5;
    private double minRange = 0;

    // TODO: refactor these guys into their own thing
    private ValueMarker frameMarker = new ValueMarker(0);
    private ValueMarker floatMarker = new ValueMarker(0);

    //private PiecewiseApproximator piecewiseApproximator;

     // for refreshing
    private ChartMouseEvent lastChartMouseEvent;
	
	LinePlot( String name , PlotTab plotTab ,
            PenCollection penCollection , PlotPanel plotPanel , Plot plot , org.nlogo.plot.Plot netLogoPlot ) {
        super( plotTab.getAppFrame() , penCollection , plotPanel , plot , netLogoPlot );
	    setOpaque( true ) ;
	    setName( name );
        this.netLogoPlot = netLogoPlot;

        viewPanel = plotTab.getViewPanel();
        this.penCollection = penCollection;
	    
	    chart = org.jfree.chart.ChartFactory.createXYLineChart(
	    		null, null, null, 
	    		activeDataset, org.jfree.chart.plot.PlotOrientation.VERTICAL, 
	    		false, false, false );
	    chart.setBorderPaint(java.awt.Color.LIGHT_GRAY);
	    
	    chartPanel = new ChartPanel(chart);
	    chartPanel.setPreferredSize( new java.awt.Dimension(300,140) );
        chartPanel.setSize( getPreferredSize() );
	    
	    chartPanel.addChartMouseListener(this);
        chartPanel.addMouseListener(this);
	    chart.setBackgroundPaint(Color.getHSBColor(0, 0, (float) .9098));
	    chart.getXYPlot().setBackgroundPaint(Color.white);
	    chart.getXYPlot().setDomainGridlinePaint(Color.gray);
	    chart.getXYPlot().setRangeGridlinePaint(Color.gray);
	    
	    add( chartPanel );

	    chart.getXYPlot().addDomainMarker(frameMarker);
        chart.getXYPlot().addDomainMarker(floatMarker);
        //piecewiseApproximator = new PiecewiseApproximator(this); //?????
        annotationManager = new AnnotationManager( this );
        //this.setBorder( new LineBorder( java.awt.Color.red ) );
	}

    public void addHighlighter( Highlighter highlighter ) {
        this.getChart().getXYPlot().addAnnotation( highlighter );
    }

    public void removeHighlighter( Highlighter highlighter ) {
        this.getChart().getXYPlot().removeAnnotation(highlighter);
    }
	
	public void addDataPoint( int run, int pen , double x , double y ) {
        // if the run hasn't been added
        // add it to the dataPerRun array
        // if there aren't previous runs, add them too
        while( dataPerRun.size() <= run ) {
            dataPerRun.add( new XYSeriesCollection() );
        }
            // if the series (i.e. the pen) hasn't been added
            // add it to both dataset and activeDataset
        if( dataPerRun.get( run ).getSeriesCount() <= pen ) {
            dataPerRun.get( run ).addSeries( new XYSeries( pen , true , false ) );
            //activeDataset.addSeries( new XYSeries( series , true , false ) );
        }


        // add the point x,y to the right pen for both dataset
        // and activeDataset
        try {
            dataPerRun.get( run ).getSeries( pen ).add( x , y );
            //activeDataset.getSeries( series ).add( x , y );
        } catch( Exception e ) {  }

        if( x > maxDomain ) maxDomain = x;
        if( y > maxRange ) maxRange = y;
        if( y < minRange ) minRange = y;
	}

	// Does the hiding and showing of data when people select 
	// the pen's checkbox.
	
	// TODO: Find a better way to do this. I know one has to exist.
    @Override
	public void stateChanged( ChangeEvent e ) {
		javax.swing.JCheckBox pen = (JCheckBox) e.getSource();
		//int index = Integer.parseInt( pen.getName() );

        if( viewPanel.getPlotTab().showAllRuns() ) {
            showAllRuns();
        } else {
            showOneRun( plotPanel.getPlotTab().getFocusRunNumber() - 1 );
        }

		rePaint();
		
		// then at the end, make sure the scale stays the same.
		chart.getXYPlot().getRangeAxis().setRange( minRange , maxRange * 1.05 );
		chart.getXYPlot().getDomainAxis().setRange( 0 , maxDomain * 1.05 );
	}

    public void rePaint() {
        if( viewPanel.getPlotTab().showAllRuns() ) {
            showAllRuns();
        } else {
            showOneRun( plotPanel.getPlotTab().getFocusRunNumber() - 1 );
        }

		// then at the end, make sure the scale stays the same.
		chart.getXYPlot().getRangeAxis().setRange( minRange , maxRange * 1.05 );
		chart.getXYPlot().getDomainAxis().setRange( 0 , maxDomain * 1.05 );

    }

    void showAllRuns() {
        int count = 0;

        for( XYSeriesCollection runData : dataPerRun ) {
            // count is the run number
            chart.getXYPlot().setDataset( count , runData );
            XYShapeRenderer renderer = new XYShapeRenderer();
            renderer.setBaseShape( new Rectangle2D.Double( -2, -2, 4, 4 ) );
            //renderer.setBaseShape( );
            // i is the pen number
            for( int i = 0 ; i < runData.getSeriesCount() ; i++ ) {
                if( penCollection.getPen(i).getCheckBox().isSelected() ) {
                    // make the focus run number data 100% opaque
                    if( plotPanel.getPlotTab().getFocusRunNumber() - 1 == count ) {
                        renderer.setSeriesPaint( i , new java.awt.Color(
                                    penCollection.getPen(i).getColor().getRed() ,
                                    penCollection.getPen(i).getColor().getGreen() ,
                                    penCollection.getPen(i).getColor().getBlue() ).darker() );
                        //renderer.setSeriesPaint( i , java.awt.Color.black );
                        renderer.setBaseShape( new Rectangle2D.Double( -2, -2, 4, 4 ) );
                        renderer.setSeriesStroke( i , new BasicStroke( 2 ) );
                        chart.getXYPlot().setRenderer( count , renderer );
                    } else {
                        //renderer.setSeriesPaint( i , new java.awt.Color(
                        //            penCollection.getPen(i).getColor().getRed() ,
                        //            penCollection.getPen(i).getColor().getGreen() ,
                        //            penCollection.getPen(i).getColor().getBlue() , 90 ) );
                        renderer.setSeriesPaint( i , java.awt.Color.gray );
                        renderer.setBaseShape( new Rectangle2D.Double( -1, -1, 2, 2 ) );
                        renderer.setSeriesStroke( i , new BasicStroke( 1 ) );
                        chart.getXYPlot().setRenderer( count , renderer );
                    }
                } else {
                    renderer.setSeriesStroke( i , new BasicStroke( 0 ) );
                    //renderer.setSeriesLinesVisible( i , false );
                }
            }
            count ++;
        }
    }

    void showOneRun( int runNumber ) {

        chart.getXYPlot().setDataset( null );
        chart.getXYPlot().setDataset( 0 , dataPerRun.get( runNumber ) );

        for( int i = 0 ; i < dataPerRun.get( runNumber ).getSeriesCount() ; i++ ) {
            if( penCollection.getPen(i).getCheckBox().isSelected() ) {
                chart.getXYPlot().getRenderer().setSeriesPaint(0 , new java.awt.Color(
                                         penCollection.getPen(i).getColor().getRed() ,
                                         penCollection.getPen(i).getColor().getGreen() ,
                                         penCollection.getPen(i).getColor().getBlue() ) );
            }
        }
    }

    public XYSeriesCollection getCurrentDataset() {
        int count = 0;
        for( XYSeriesCollection runData : dataPerRun ) {
            if( plotPanel.getPlotTab().getFocusRunNumber() - 1 == count ) {
                return runData;
            }
            count ++;
        }
        return null;
    }


    // Accessor methods
    public XYSeriesCollection getDataset() {
        return dataset;
    }

    public LinePlotPenPanel getPen( int index ) {
        return (LinePlotPenPanel) this.penCollection.getPen(index);
    }

	public void setPenColor( int pen ) {
		chart.getXYPlot().getRendererForDataset( (XYDataset) dataset.getSeries( pen ));
	}

    @Override
	public void setTick( int tick ) {
		// draws the line
		rePaint();
		frameMarker.setValue(tick);
		frameMarker.setPaint(Color.black);
	}

    // =========================
    // Plot interaction
    // =========================

    @Override
	public void chartMouseMoved(ChartMouseEvent arg0) {

        // Normal floating vertical marker
		double myValue = chart.getXYPlot().getDomainAxis().java2DToValue( arg0.getTrigger().getX(), 
				 chartPanel.getScreenDataArea(), 
				 RectangleEdge.BOTTOM );
		int myInt = (int) Math.ceil( myValue );

        floatMarker.setValue(myInt);
        floatMarker.setPaint(Color.lightGray);

        // piecewise approximator
        //piecewiseApproximator.updatePosition(myInt);

        // look for annotations
        annotationManager.showAnnotationBoxes(myValue , arg0.getTrigger().getPoint() );

        lastChartMouseEvent = arg0;
	}


    @Override
	public void chartMouseClicked(ChartMouseEvent arg0) {

		double myValue = chart.getXYPlot().getDomainAxis().java2DToValue( arg0.getTrigger().getX(),
														 chartPanel.getScreenDataArea(),
														 RectangleEdge.BOTTOM );
		int myInt = (int) Math.ceil( myValue );
		viewPanel.goToFrame( myInt );


        // SHIFT DOWN FOR ANNOTATION
        if( arg0.getTrigger().isShiftDown() ) {
            annotationManager.makeNewBox(myValue);
        }

	}

    public void refresh() {
        chartMouseMoved( lastChartMouseEvent );
    }


    public void mouseExited(MouseEvent e) {
        //piecewiseApproximator.clear();
        //System.out.println("boop");
        //chart.getXYPlot().clearDomainMarkers(1);
        //chart.getXYPlot().clearDomainMarkers(2);
        //floatMarker.
        //chart.getXYPlot().clearAnnotations();
    }
}