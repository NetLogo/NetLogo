package org.nlogo.hotlink.controller;

import org.nlogo.hotlink.dialogs.InterfaceObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.nlogo.agent.Agent;
import org.nlogo.agent.BooleanConstraint;
import org.nlogo.agent.ChooserConstraint;
import org.nlogo.agent.SliderConstraint;
import org.nlogo.agent.Turtle;
import org.nlogo.api.CompilerException;
import org.nlogo.api.LogoException;
import org.nlogo.api.Shape;
import org.nlogo.api.ValueConstraint;
import org.nlogo.headless.HeadlessWorkspace;
import org.nlogo.shape.DrawableShape;

public class ModelReader extends Thread {
    String fileName;
    List<InterfaceObject> interfaceObjects = new ArrayList<InterfaceObject>();
    ArrayList<DrawableShape> modelBreedShapes = new ArrayList<DrawableShape>();
    HeadlessWorkspace workspace;

    public ModelReader( String fileName ) {
        this.workspace = HeadlessWorkspace.newInstance();
        this.fileName = fileName;

        try {
            workspace.open(fileName);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
/*
    public void setModel( String fileName ) {
        this.fileName = fileName;

        try {
            workspace.open(fileName);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }*/

    public List<InterfaceObject> getInterfaceObjects() {
        while( this.isAlive() ) {} // wait till i'm done!
        return interfaceObjects;
    }
/*
    public void run() {
        if( workspace.modelOpened ) {
            StringBuilder sb = new StringBuilder();

            int numVars = workspace.world().observer().getVariableCount();

            String name ="";
            double min =0;
            double max =0;
            double incr =0;
            int type = 0;
            double value = 0;

            for (int i = 0; i < numVars; i++)
            {
                ValueConstraint con = workspace.world().observer().variableConstraint(i);

                if ( con != null) {
                    name = workspace.world.observerOwnsNameAt(i);

                    //try {
                        if (con instanceof SliderConstraint)
                        {
                            SliderConstraint scon = (SliderConstraint) con;
                            min = scon.minimum();
                            incr = scon.increment();
                            max = scon.maximum();
                            // TODO: Here
                            value = Double.parseDouble( workspace.world.getObserverVariableByName(name).toString() );

                            InterfaceObject newIO = new InterfaceObject( (int) max , (int) min , incr , name , value );
                            interfaceObjects.add( newIO );
                        } else if (con instanceof ChooserConstraint) {
                            ChooserConstraint ccon = (ChooserConstraint) con;

                            InterfaceObject newIO = new InterfaceObject( name , ccon.acceptedValues() , ccon.defaultIndex() );
                            interfaceObjects.add( newIO );
                            //for (Object obj: ccon.acceptedValues())
                            //{
                            //   System.out.println(org.nlogo.api.Dump.logoObject(obj, true, false));
                            //}
                        } else if (con instanceof BooleanConstraint) {
                            BooleanConstraint bcon = (BooleanConstraint) con;

                            InterfaceObject newIO = new InterfaceObject( name , bcon.defaultValue().toString() );
                            interfaceObjects.add( newIO );
                        }
                    //} catch (LogoException ex)	{
                            //sb2.setLength(0);
                            //sb2.append(org.nlogo.api.Dump.logoObject(con.defaultValue(), true, false));
                    //}
                }
            }

            // Set up the model so that we get the breed shapes that we need
            try {
                workspace.command("setup");
            } catch (CompilerException ex) {
                // TODO: Throw some kind of exception here.
                Logger.getLogger(ModelReader.class.getName()).log(Level.SEVERE, null, ex);
            } catch (LogoException ex) {
                Logger.getLogger(ModelReader.class.getName()).log(Level.SEVERE, null, ex);
            }

            if( workspace.world().getBreeds().size() > 0 ) {
                System.out.println("Breeds are declared.");
                for( Object breed : workspace.world().getBreeds().keySet() ) {

                    String breedShapeName =
                            workspace.world().turtleBreedShapes.breedShape(
                                workspace.world().getBreed( breed.toString() ) );
                    modelBreedShapes.add( (DrawableShape)
                            workspace.world().turtleShapeList().shape(
                                breedShapeName ) );
                }
            } else {
                System.out.println("No breeds declared.");

                String breedShapeName =
                        workspace.world().turtleBreedShapes.breedShape(
                            workspace.world().turtles() );
                modelBreedShapes.add( (DrawableShape)
                        workspace.world().turtleShapeList().shape(
                            breedShapeName ) );
            }
        } else {
            // TODO: some kind of exception
            System.out.println("ModelReader does not have a file open yet.");
        }
    }

    public ArrayList<DrawableShape> getModelBreedShapes() {
        return modelBreedShapes;
    }*/
}