package org.nlogo.hotlink.graph.annotation;

import org.nlogo.hotlink.graph.LinePlot;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import org.jfree.chart.ChartMouseEvent;

public class AnnotationManager {
    LinePlot myPlot;
    // Annotations as keys, AnnotationWindow as values.
    HashMap<Annotation,AnnotationWindow> aws =
            new HashMap<Annotation,AnnotationWindow>(); // only one at a time

    Highlighter tempHighlighter;

    public AnnotationManager( LinePlot myPlot ) {
        this.myPlot = myPlot;
    }

    // to make new annotations from the interface
    public void makeAnnotation( Point point , Double start , Double end ) {
        Annotation annotation = new Annotation( start.intValue() ,
                end.intValue() , this );
        AnnotationWindow aWindow = new AnnotationWindow(
                                    myPlot.getPlotPanel().getPlotTab() ,
                                    annotation,
                                    point );
        aws.put(annotation, aWindow);
    }

    // to make new annotations loaded from a file
    // this is called by AnnotationLoader
    public void makeAnnotation( Double start , Double end , 
            String annotationText ) {
        Annotation annotation = new Annotation( start.intValue() ,
                end.intValue() , annotationText , this );
        AnnotationWindow aWindow = new AnnotationWindow(
                                    myPlot.getPlotPanel().getPlotTab() ,
                                    annotation, annotationText , 
                                    myPlot.getLocationOnScreen() );
        aws.put(annotation, aWindow);
    }

    public void makeNewBox( Double value ) {

        if( tempHighlighter != null ) {
            if( tempHighlighter.getStart() == -1 || tempHighlighter.getEnd() != -1 ) {
                tempHighlighter = new Highlighter( myPlot , value );
            } else {
                double start = tempHighlighter.getStart();
                tempHighlighter = new Highlighter( myPlot , start , value );
                makeAnnotation( myPlot.getLocationOnScreen() , start , value );
            }
        } else {
            tempHighlighter = new Highlighter( myPlot , value );
        }

        myPlot.getChart().getXYPlot().addAnnotation(tempHighlighter);
    }

    public void showAnnotationBoxes( double value , Point mouseLocation ) {
        //clearAnnotationWindows( value );
        // todo: change so it just deletes them when needed rather than every time
        for( Annotation annotation : aws.keySet() ) {
            if( value > annotation.startTime 
                    && value < annotation.endTime ) {
                myPlot.getChart().getXYPlot().addAnnotation(
                            new Highlighter( myPlot , annotation.startTime ,
                                             annotation.endTime , annotation.color )
                        );
                if( ! aws.get(annotation).isVisible() ) {
                    Double newX = this.myPlot.getLocationOnScreen().getX()
                            + mouseLocation.getX();
                    Double newY = this.myPlot.getLocationOnScreen().getY()
                            + mouseLocation.getY();
                    Point skewedPoint = new Point(
                            newX.intValue() ,
                            newY.intValue() );
                    aws.get(annotation).show( skewedPoint );
                }
            } else {
                aws.get(annotation).setVisible(false);
            }
        }
    }

    public void showAllAnnotationRegions() {
        for( Annotation annotation : aws.keySet() ) {
            myPlot.getChart().getXYPlot().addAnnotation(
                new Highlighter( myPlot , annotation.startTime ,
                                 annotation.endTime , annotation.color
                )
            );
        }
    }

    public void clearAnnotationWindows( double value ) {
        for( Annotation annotation : aws.keySet() ) {
            if( (value < annotation.startTime || value > annotation.endTime) && aws.containsKey(annotation)) {
                if( aws.get(annotation) != null ) {
                    aws.get(annotation).dispose();
                    aws.remove(annotation);
                } else {
                    aws.remove(annotation);
                }
            }
        }
    }

    public String export() {
        String sBuilder = "";
        for( Annotation annotation : aws.keySet() ) {
            sBuilder += annotation.export();
        }
        return sBuilder;
    }

    public boolean anyAnnotations() {
        return (! aws.isEmpty());
    }

    void removeAnnotation(Annotation annotation) {
        aws.get(annotation).dispose();
        aws.remove(annotation);
    }
    
}