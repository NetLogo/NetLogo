package org.nlogo.hotlink.graph.annotation;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.Date;
import javax.swing.JTextPane;

class Annotation implements Serializable,
                            KeyListener {
    String text;
    AnnotationManager annotator;
    double startTime;
    double endTime;
    Color color = new Color(139, 86, 100);
    Date timestamp;

    // for loaded annotations
    String author;

    Annotation( int startTime , int endTime , AnnotationManager annotator ) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.annotator = annotator;
        //this.timestamp = Calendar.getInstance().
    }
   // for when an annotation is loaded from a file
    Annotation(int startTime, int endTime, String annotationText,
            AnnotationManager annotator ) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.annotator = annotator;
        this.text = annotationText;
    }

    // for when an annotation is loaded from a file
    Annotation(int startTime, int endTime, String annotationText,
            AnnotationManager annotator , Color color ) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.annotator = annotator;
        this.text = annotationText;
    }

    public void setText( String text ) { this.text = text; }
    public String getText() { return text; }
    public void setColor( Color color ) { this.color = color; }
    void setEndTime(int end) { endTime = end; }
    void setStartTime(int start) { startTime = start; }

    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}

    public void keyPressed(KeyEvent e) {
        // if enter is pressed, save the note.
        if( e.getKeyCode() == KeyEvent.VK_ENTER ) {
            JTextPane textPane = (JTextPane) e.getSource();
            if( textPane.getText() != "" ) {
                this.text = textPane.getText();
            } 
        }

        //TODO: This is just a test
        //if( e.getKeyCode() == KeyEvent.VK_9 ) {
        //    export();
        //}
    }

    /* Export Format:
     *
     * ###
     * LinePlot <name>
     * <startticks>-<endticks>
     * annotation text
     *
     * */
    public String export() {
        String sBuilder = "";

        sBuilder += "###";
        sBuilder += "\n";

        sBuilder += this.annotator.myPlot.getName();
        sBuilder += "\n";

        sBuilder += this.startTime;
        sBuilder += "\n";
        sBuilder += this.endTime;
        sBuilder += "\n";

        sBuilder += this.getText();
        sBuilder += "\n";

        return sBuilder;
    }
}