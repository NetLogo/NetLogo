package org.nlogo.deltatick;

import org.nlogo.deltatick.xml.Breed;
import org.nlogo.deltatick.xml.Envt;
import org.nlogo.deltatick.xml.ModelBackgroundInfo;
import org.nlogo.window.GUIWorkspace;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mwilkerson
 * Date: Mar 8, 2010
 * Time: 5:13:59 PM
 * To change this template use File | Settings | File Templates.
 */

// Transferable defines the interface for classes that can be used to provide data for a transfer operation -A. (sept 8)
public class BuildPanel
        extends JPanel
        implements Transferable {

    GUIWorkspace workspace;

    // Linked list is a class. In addition to implementing the List interface, the LinkedList class provides
    // uniformly named methods to get, remove and insert an element at the beginning and end of the list. -a.

    List<BreedBlock> myBreeds = new LinkedList<BreedBlock>();
    List<PlotBlock> myPlots = new LinkedList<PlotBlock>();
    List<EnvtBlock> myEnvts = new LinkedList<EnvtBlock>();
    ModelBackgroundInfo bgInfo = new ModelBackgroundInfo();


    //DataFlavor"[]" is an array - A. (sept 8)
    DataFlavor[] flavors = new DataFlavor[] {
        DataFlavor.stringFlavor
    };

    public BuildPanel( GUIWorkspace workspace ) {
        super();
        this.workspace = workspace;
        setBackground( java.awt.Color.white );
        setLayout(null);
        validate();
    }

    public Object getTransferData( DataFlavor dataFlavor )
        throws UnsupportedFlavorException {
        if( isDataFlavorSupported( dataFlavor ) ) {
            return unPackAsCode();
        }
        return null;
    }

    public String unPackAsCode() {
        String passBack = "";

        // declare breeds method called from BreedBlock
        for( BreedBlock breedBlock : myBreeds ) {
            passBack += breedBlock.declareBreed();
        }

        passBack += "\n";
        for( BreedBlock breedBlock : myBreeds ) {
            passBack += breedBlock.breedVars();
        }

        passBack += "\n";
        for ( EnvtBlock envtBlock : myEnvts ) {
            passBack += envtBlock.OwnVars();
        }

        passBack += "\n";

        passBack += bgInfo.declareGlobals();

        passBack += "\n";

        passBack += bgInfo.setupBlock(myBreeds, myEnvts, myPlots );
        passBack += "\n";

        passBack += "to go\n";
        passBack += bgInfo.updateBlock( myBreeds );

        for( BreedBlock breedBlock : myBreeds ) {
            passBack += breedBlock.unPackAsCode();
        }

        for( EnvtBlock envtBlock : myEnvts ) {
            passBack += envtBlock.unPackAsCode();
        }
        passBack += "tick\n";
        if( myPlots.size() > 0 ) {
            passBack += "do-plotting\n";
        }
        passBack += "end\n";
        passBack += "\n";

        passBack += unPackProcedures();
        passBack += "\n";

        if( myPlots.size() > 0 ) {
            passBack += "\n\n";
            passBack += "to do-plotting\n";
            for( PlotBlock plot : myPlots ) {
                passBack += plot.unPackAsCode();
            }
            passBack += "end\n";
        }

        return passBack;
    }

    public String saveAsXML() {
        String passBack = "<";

        // declare breeds
        for( BreedBlock breedBlock : myBreeds ) {
            passBack += breedBlock.declareBreed();
        }

        passBack += "\n";
        for( BreedBlock breedBlock : myBreeds ) {
            passBack += breedBlock.breedVars();
        }

        passBack += "\n";

        passBack += bgInfo.declareGlobals();
        passBack += "\n";

        passBack += bgInfo.setupBlock( myBreeds , myEnvts, myPlots );
        passBack += "\n";

        passBack += "to go\n";
        passBack += bgInfo.updateBlock( myBreeds );

        for( BreedBlock breedBlock : myBreeds ) {
            passBack += breedBlock.unPackAsCode();
        }
        passBack += "tick\n";
        if( myPlots.size() > 0 ) {
            passBack += "do-plotting\n";
        }
        passBack += "end\n";
        passBack += "\n";

        passBack += unPackProcedures();
        passBack += "\n";

        if( myPlots.size() > 0 ) {
            passBack += "\n\n";
            passBack += "to do-plotting\n";
            for( PlotBlock plot : myPlots ) {
                passBack += plot.unPackAsCode();
            }
            passBack += "end\n";
        }

        return passBack;
    }



//*
//isDataFlavorSupported
//public boolean isDataFlavorSupported(DataFlavor flavor)
  //  Returns whether the requested flavor is supported by this Transferable.
    //Specified by:
      //  isDataFlavorSupported in interface Transferable
    //Parameters:
      //  flavor - the requested flavor for the data
    //Returns:
     //   true if flavor is equal to DataFlavor.stringFlavor or DataFlavor.plainTextFlavor; false if flavor is not one of the above flavors
    //Throws:
      //  NullPointerException - if flavor is null
// -a.

    public boolean isDataFlavorSupported( DataFlavor dataFlavor ) {
        for( int i = 0; i < flavors.length; i++ ) {
            if( dataFlavor.equals( flavors[i] ) ) {
                return true;
            }
        }
        return false;
    }

    // all array being returned -A. (sept 8)
    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    public ModelBackgroundInfo getBgInfo() {
        return bgInfo;
    }

    // find method in DeltaTicktab to add actors -a.
    public void addBreed( BreedBlock block ) {
        myBreeds.add( block );
        block.setBounds( 0 ,
                         0 ,
                         block.getPreferredSize().width,
                         block.getPreferredSize().height );

        add( block );
        block.doLayout();
        block.validate();
        block.repaint();
        //block.repaint();
    }
/*
    public void addBreed( String name , int number , String shape , int x , int y , String breedName ) throws Exception {
        BreedBlock newBlock = new BreedBlock( bgInfo.getBreed( breedName ) , name , workspace.getFrame() );
        newBlock.setBounds( 0 ,
                            0 ,
                            block.getPreferredSize().width,
                            block.getPreferredSize().height );
    }
*/
    public void addPlot( PlotBlock block ) {
        myPlots.add( block );
        block.setPlotName( "New Plot " + myPlots.size() );
        block.setBounds( 200 ,
                         0 ,
                         block.getPreferredSize().width,
                         block.getPreferredSize().height );
        add( block );
        block.doLayout();
        block.validate();
        block.repaint();
    }

    //make linked list for envt? -A. (sept 8)
    public void addEnvt ( EnvtBlock block ) {
        //EnvtBlock block = new EnvtBlock();
        //workspace.add( block );
        myEnvts.add( block );
        //usedEnvts.add( block );
        block.setBounds( 400,
                         0,
                         block.getPreferredSize().width,
                         block.getPreferredSize().height );
        add ( block );
        block.doLayout();
        block.validate();
        block.repaint();


    }
    public void addPlot( String name , int x , int y ) {
        PlotBlock newPlot = new PlotBlock();
        myPlots.add(newPlot);
        newPlot.setPlotName(name);
        newPlot.setBounds( 400 ,
                           0 ,
                           newPlot.getPreferredSize().width,
                           newPlot.getPreferredSize().height );
        newPlot.setLocation( x , y );
        add( newPlot );
        newPlot.doLayout();
        newPlot.validate();
        newPlot.repaint();
    }


    // Collection<typeObject> object that groups multiple elements into a single unit -A. (sept 8)
    public Collection<BreedBlock> getMyBreeds() {
        return myBreeds;
    }

    public List<PlotBlock> getMyPlots() {
        return myPlots;
    }

    public Collection<EnvtBlock> getMyEnvts() {
        return myEnvts;
    }

    //HashMap is a pair of values. eg. name1: SSN1 -A. (sept 8)
    public String unPackProcedures() {
        HashMap<String,CodeBlock> procedureCollection = new HashMap<String,CodeBlock>();
        String passBack = "";

        for( BreedBlock breedBlock : myBreeds ) {
            if( breedBlock.children() != null ) {
                procedureCollection.putAll( breedBlock.children() );
            }
        }

        for( PlotBlock plotBlock : myPlots ) {
            if( plotBlock.children() != null ) {
                procedureCollection.putAll( plotBlock.children() );
            }
        }

        for ( EnvtBlock envtBlock : myEnvts ) {
            if ( envtBlock.children() != null ) {
                procedureCollection.putAll( envtBlock.children() );
            }
        }

        for( String name : procedureCollection.keySet() ) {
            passBack += procedureCollection.get(name).unPackAsProcedure();
        }

        return passBack;
    }

    //I think this is where you clear the window to remove everything-a.
    public void clear() {
        myBreeds.clear();
        myPlots.clear();
        myEnvts.clear();
        removeAll();
        doLayout();
        //validate();
    }

    @Override
    public void paintComponent( java.awt.Graphics g )
	{
		g.setColor( getBackground() ) ;
		g.fillRect(0,0,getWidth(),getHeight() ) ;

        for( Component c : getComponents() ) {
            c.setBounds(c.getBounds());
            //System.out.println("xcor: " + c.getX() + " ycor: " + c.getY() );
        }
	}

    public int breedCount() {
        return myBreeds.size();
    }

    public int plotCount() {
        if( myPlots != null ) {
            return myPlots.size();
        }
        return 0;
    }

    //number of breeds in buildPanel -A. (sept 8)
    //No, I think it's breeds available in XML -A. (oct 5)
    public ArrayList<Breed> availBreeds() {
        return bgInfo.getBreeds();
    }

    public ArrayList<Envt> availEnvts() {
        return bgInfo.getEnvts();
    }

    public void removePlot( PlotBlock plotBlock ) {
        myPlots.remove(plotBlock);
        remove(plotBlock);
    }

    public void removeBreed( BreedBlock breedBlock ) {
        myBreeds.remove(breedBlock);
        remove(breedBlock);
    }

    public void removeEnvt( EnvtBlock envtBlock ) {
        myEnvts.remove(envtBlock);
        remove(envtBlock);
    }


}
