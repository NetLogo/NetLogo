package org.nlogo.hotlink.graph.annotation;

import org.nlogo.hotlink.graph.LinePlot;
import java.awt.Color;
import org.jfree.chart.annotations.XYBoxAnnotation;

public class Highlighter extends XYBoxAnnotation {
    int start = -1;
    int end = -1;
    LinePlot myPlot;

    Highlighter( LinePlot myPlot , Double start ) {
        //XYBoxAnnotation(double x0, double y0, double x1, double y1, java.awt.Stroke stroke, java.awt.Paint outlinePaint, java.awt.Paint fillPaint)
        super( start.intValue() , myPlot.getChart().getXYPlot().getRangeAxis().getLowerBound() ,
               start.intValue() , myPlot.getChart().getXYPlot().getRangeAxis().getUpperBound() ,
               new java.awt.BasicStroke(),
               new Color(139, 86, 101, 100),
               new Color(139, 86, 101, 50)
             );
        this.myPlot = myPlot;
        this.start = start.intValue();
        //myPlot.getChart().getXYPlot().addAnnotation(this);
    }

    Highlighter( LinePlot myPlot , Double start , Double end ) {
        //XYBoxAnnotation(double x0, double y0, double x1, double y1, java.awt.Stroke stroke, java.awt.Paint outlinePaint, java.awt.Paint fillPaint)
        super( start.intValue() , myPlot.getChart().getXYPlot().getRangeAxis().getLowerBound() ,
               end.intValue() , myPlot.getChart().getXYPlot().getRangeAxis().getUpperBound() ,
               new java.awt.BasicStroke(),
               new Color(139, 86, 101, 100),
               new Color(139, 86, 101, 50)
             );
        this.myPlot = myPlot;
        this.start = start.intValue();
        this.end = end.intValue();
        //myPlot.getChart().getXYPlot().addAnnotation(this);
    }

    Highlighter( LinePlot myPlot , Double start , Color color ) {
        //XYBoxAnnotation(double x0, double y0, double x1, double y1, java.awt.Stroke stroke, java.awt.Paint outlinePaint, java.awt.Paint fillPaint)
        super( start.intValue() , myPlot.getChart().getXYPlot().getRangeAxis().getLowerBound() ,
               start.intValue() , myPlot.getChart().getXYPlot().getRangeAxis().getUpperBound() ,
               new java.awt.BasicStroke(),
               new Color(color.getRed(), color.getGreen(), color.getBlue(), 100),
               new Color(color.getRed(), color.getGreen(), color.getBlue(), 50)
             );
        this.myPlot = myPlot;
        this.start = start.intValue();
        //myPlot.getChart().getXYPlot().addAnnotation(this);
    }

    Highlighter( LinePlot myPlot , Double start , Double end , Color color ) {
        //XYBoxAnnotation(double x0, double y0, double x1, double y1, java.awt.Stroke stroke, java.awt.Paint outlinePaint, java.awt.Paint fillPaint)
        super( start.intValue() , myPlot.getChart().getXYPlot().getRangeAxis().getLowerBound() ,
               end.intValue() , myPlot.getChart().getXYPlot().getRangeAxis().getUpperBound() ,
               new java.awt.BasicStroke(),
               new Color(color.getRed(), color.getGreen(), color.getBlue(), 100),
               new Color(color.getRed(), color.getGreen(), color.getBlue(), 50)
             );
        this.myPlot = myPlot;
        this.start = start.intValue();
        this.end = end.intValue();
        //myPlot.getChart().getXYPlot().addAnnotation(this);
    }

    public double getStart() { return start; }
    public double getEnd() { return end; }
}