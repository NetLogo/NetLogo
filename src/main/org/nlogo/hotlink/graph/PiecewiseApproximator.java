package org.nlogo.hotlink.graph;

import java.awt.BasicStroke;
import java.awt.Color;

import java.util.List;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class PiecewiseApproximator extends ValueMarker {
    // TODO: switch to intervalmarker

    // my pieces
	ValueMarker lMarker = new ValueMarker(0);;
	ValueMarker rMarker = new ValueMarker(0);;
    XYLineAnnotation secantLine = new XYLineAnnotation(0,0,0,0);

    // my parents
    JFreeChart chart;
    LinePlot linePlot;

    // my properties
    boolean visible = true;
	
	public PiecewiseApproximator( LinePlot linePlot ) {
        super(0);
        this.chart = linePlot.chart;
        this.linePlot = linePlot;

		chart.getXYPlot().addAnnotation(secantLine);

		chart.getXYPlot().addDomainMarker(lMarker);
		chart.getXYPlot().addDomainMarker(rMarker);
	}

    void clear() {
        chart.getXYPlot().clearAnnotations();
    }

    void setVisible(boolean b) {
        visible = b;
    }
	
	void updatePosition( int center ) {
        clear();
        if( linePlot.getPlotPanel().getShowPiecewiseApproximation() ) {
            double dtInterval = linePlot.getPlotPanel().getInterval();

            // TODO: abstract that out eventually
            lMarker.setValue(center - Math.ceil(dtInterval / 2) );
            rMarker.setValue(center + Math.floor(dtInterval / 2) );
            lMarker.setPaint(Color.gray);
            rMarker.setPaint(Color.gray);

            calculateDX();
        } else {
            chart.getXYPlot().clearDomainMarkers(1);
            chart.getXYPlot().clearDomainMarkers(2);
        }
	}

    // TODO: eventually pass in dataset
    void calculateDX(  ) {
        if( linePlot.getCurrentDataset() != null ) {
            XYSeriesCollection collection
                    = linePlot.getCurrentDataset();
            int index = 0;
            for( Object dataSeries : collection.getSeries() ) {
                XYSeries xyDataSeries = (XYSeries) dataSeries;

                int lowerBound = (int) maxMinOrValue( lMarker.getValue() );
                int upperBound = (int) maxMinOrValue( rMarker.getValue() );

                if( xyDataSeries.getItemCount() > upperBound ) {
                    double lowerBoundValue = xyDataSeries.getY( lowerBound ).doubleValue();
                    double upperBoundValue = xyDataSeries.getY( upperBound ).doubleValue();

                    secantLine = new XYLineAnnotation( lowerBound , lowerBoundValue,
                                        upperBound , upperBoundValue,
                                        new BasicStroke(1.0f) {} ,
                                        linePlot.getPen(index).getCheckBox().getForeground().darker() );
                    chart.getXYPlot().addAnnotation(secantLine);

                    linePlot.getPen( index ).setDeltaValue( upperBoundValue - lowerBoundValue );
                }
                index ++;
            }
        }
        
    }

    double maxMinOrValue( double value ) {
        if( value < ( linePlot.maxDomain ) && value >= 0 ) {
            return value;
        } else if ( value < 0 ) {
            return 0;
        } else {
            return ( linePlot.maxDomain );
        }
    }
}
