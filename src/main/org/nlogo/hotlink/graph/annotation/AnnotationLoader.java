package org.nlogo.hotlink.graph.annotation;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;

public class AnnotationLoader {
    File file;
    String modelName;
    String timeStamp;
    String userName;
    String userID;
    ArrayList<LoadedAnnotation> annotations = new ArrayList<LoadedAnnotation>();
    HashSet<String> plotNames = new HashSet<String>();
    
    public AnnotationLoader( File file ) throws FileNotFoundException, IOException {
        this.file = file;

        // check to see if the name of the model this file was saved for
        // matches the name of the model we're loading it into
        if( file.canRead() ) {
            FileInputStream fstream = new FileInputStream(file);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            // first line is timestamp
            timeStamp = reader.readLine();
            // second line is model name
            modelName = reader.readLine();
            // third line is user name
            userName = reader.readLine();
            // fourth is user ID
            userID = reader.readLine();

            // the rest is annotations
            String nextLine = reader.readLine();
            LoadedAnnotation loadedAnnotation = null;

            while( nextLine != null ) { // while there's still stuff to read
                if( nextLine.compareTo( "###" ) == 0 ) {
                    if( loadedAnnotation != null ) {
                        addAnnotation(loadedAnnotation);
                    }

                    loadedAnnotation = new LoadedAnnotation( reader.readLine() ,
                                Double.parseDouble( reader.readLine() ) ,
                                Double.parseDouble( reader.readLine() ));

                } else {
                    loadedAnnotation.append(nextLine);
                }

                nextLine = reader.readLine();
            }

            if( loadedAnnotation != null ) {
                addAnnotation(loadedAnnotation);
            }

        }
    }
    
    public void addAnnotation( LoadedAnnotation loadedAnnotation ) {
        annotations.add(loadedAnnotation);
        plotNames.add(loadedAnnotation.plotName);
        System.out.println("loaded the annotation for plot " + loadedAnnotation.plotName );
    }

    public HashSet<String> getPlotNames() {
        return plotNames;
    }

    public ArrayList<LoadedAnnotation> getLoadedAnnotations( String plotName ) {
        ArrayList<LoadedAnnotation> loadedAnnotations = new ArrayList<LoadedAnnotation>();

        for( LoadedAnnotation loadedAnnotation : annotations ) {
            if( loadedAnnotation.plotName.compareTo( plotName ) == 0 ) {
                loadedAnnotations.add(loadedAnnotation);
            }
        }

        return loadedAnnotations;
    }
    
    public class LoadedAnnotation {
        String plotName;
        Double start;
        Double end;
        String annotationText;

        public LoadedAnnotation( String plotName , Double start , Double end ) {
            this.plotName = plotName;
            this.start = start;
            this.end = end;
        }

        public void append( String text ) {
            annotationText += text;
        }

        public Double getStart() {
            return start;
        }

        public Double getEnd() {
            return end;
        }

        public String getText() {
            return annotationText;
        }
    }
}