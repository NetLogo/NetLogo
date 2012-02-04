package org.nlogo.deltatick;

import org.nlogo.api.Shape;
import org.nlogo.app.App;
import org.nlogo.deltatick.dialogs.ShapeSelector;
import org.nlogo.deltatick.dialogs.VariationSelector;
import org.nlogo.deltatick.xml.Breed;
import org.nlogo.deltatick.xml.OwnVar;
import org.nlogo.deltatick.dnd.PrettyInput;
import org.nlogo.hotlink.dialogs.ShapeIcon;
import org.nlogo.hotlink.dialogs.StackedShapeIcon;
import org.nlogo.nvm.Workspace;
import org.nlogo.shape.DrawableShape;
import org.nlogo.shape.ShapesManagerInterface;
import org.nlogo.shape.TurtleShapesManagerInterface;
import org.nlogo.shape.VectorShape;
import org.nlogo.shape.editor.ImportDialog;
import org.nlogo.window.Widget;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.*;

import org.nlogo.deltatick.dialogs.TraitSelector;

// BreedBlock contains code for how whatever happens in BreedBlock is converted into NetLogo code -A. (aug 25)

public strictfp class BreedBlock
	extends CodeBlock
    implements java.awt.event.ActionListener,
               ImportDialog.ShapeParser,
               MouseMotionListener,
        MouseListener {

    // "transient" means the variable's value need not persist when the object is stored  -a.
    String breedShape = "default";
    transient Breed breed;
    transient VectorShape shape = new VectorShape();
    transient Frame parentFrame;
    transient ShapeSelector selector;
    transient JButton breedShapeButton;
    transient PrettyInput number;
    transient PrettyInput plural;
    //TraitSelector traitSelector;
    //VariationSelector variationSelector;


    //String pluralGiven;
    //String singular;
    //ShapeSelector myShapeSelector;
    int id;
    transient String trait;
    JTextField traitLabel;
    transient String variation;

    // constructor for breedBlock without trait & variation
    public BreedBlock( Breed breed , String plural, Frame frame )
	{
		super( plural , ColorSchemer.getColor(3) ) ;
        this.parentFrame = frame;
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.setLocation(0,0);
        this.setForeground(color);
        //this.id = id;
        this.breed = breed;
        number.setText( breed.getStartQuant() );

        //this.singular() = singular;
        //this.pluralGiven = plural;
        //myShapeSelector = new ShapeSelector( parentFrame , allShapes() , this );
		setBorder( org.nlogo.swing.Utils.createWidgetBorder() ) ;

        flavors = new DataFlavor[] {
          DataFlavor.stringFlavor,
          codeBlockFlavor,
          breedBlockFlavor,
          //patchBlockFlavor
        };

        //setPreferredSize( 250 , 99 );
        //setSize( 250 , 99 );
    }

    // second constructor for breedBlock with trait & variation
    public BreedBlock( Breed breed , String plural, String traitName, String variationName, Frame frame )
	{
		super( plural , ColorSchemer.getColor(3) ) ;
        this.parentFrame = frame;
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.setLocation(0,0);
        this.setForeground(color);
        //this.id = id;
        this.breed = breed;
        number.setText( breed.getStartQuant() );
        this.trait = traitName;
        this.variation = variationName;
        //traitLabel.setText(variation + " " + trait);

        TraitSelector traitSelector = new TraitSelector(frame);
		setBorder( org.nlogo.swing.Utils.createWidgetBorder() ) ;

        flavors = new DataFlavor[] {
          DataFlavor.stringFlavor,
          codeBlockFlavor,
          breedBlockFlavor,
          //patchBlockFlavor
        };
        

        //setPreferredSize( 250 , 99 );
        //setSize( 250 , 99 );
    }

    // this is where breed is declared in NetLogo code -A. (aug 25)
    public String declareBreed() {
        return "breed [ " + plural() + " " + singular() + " ]\n";
    }

    //this is where breeds-own variables show up in NetLogo code -A. (aug 25)
    public String breedVars() {
        String code = "";

        if( breed.getOwnVars().size() > 0 ){
            code += plural() + "-own [\n";
            for( OwnVar var : breed.getOwnVars() ) {
                code += "  " + var.name + "\n";
            }
            code += "]\n";
        }

         // send Trait as turtles-own variable -A. (feb 3, 2012)
        else if ( trait != null ) {
            code += plural() + "-own [\n" + trait + "\n]";
            }
        return code;
    }

    // code to setup in NetLogo code window. This method is called in MBgInfo -A.
    public String setup() {
        String code = "";
        if( breed.needsSetupBlock() ) {
            code += "create-" + plural() + " " + number.getText() + " [\n";
            if( breed.getSetupCommands() != null ) { code += breed.getSetupCommands(); }
            for( OwnVar var : breed.getOwnVars() ) {
                if( var.setupReporter != null ) {
                    code += "set " + var.name + " " + var.setupReporter + "\n";
                }
            }
            code += "]\n";
        }

        return code;
    }

    public String update() {
        String code = "";
        if( breed.needsUpdateBlock() ) {
            code += "ask " + plural() + " [\n";
            if( breed.getUpdateCommands() != null ) { code += breed.getUpdateCommands(); }
            for( OwnVar var : breed.getOwnVars() ) {
                if( var.updateReporter != null ) {
                    code += "set " + var.name + " " + var.updateReporter + "\n";
                }
            }
            code += "]\n";
        }

        return code;
    }

    // very smart! singular is just prefixed plural -A.
    public String singular() {
        return "one-of-" + plural.getText();
    }


    // returns "plural" of breed
    public String plural() {
        return plural.getText();
        //temp_plural = plural.getText();
        //plural = variation + temp_plural;
        //return plural;
    }

    //public java.awt.Dimension getMinimumSize() {
    //    return new java.awt.Dimension( 250 , 99 );
    //}

    //public Dimension getPreferredSize() {
    //    return new java.awt.Dimension( 250 , 99 );
    //}


    //I don't understand dataflavors
    public Object getTransferData( DataFlavor dataFlavor )
            throws UnsupportedFlavorException {
        if( isDataFlavorSupported( dataFlavor ) ) {
            if( dataFlavor.equals( breedBlockFlavor ) ) {
                return this;
            }
            if( dataFlavor.equals( patchBlockFlavor ) ) {
                return this;
            }
            if( dataFlavor.equals( DataFlavor.stringFlavor ) ) {
                return unPackAsCode();
            }
        } else {
            return "Flavor Not Supported";
        }
        return null;
    }

    // reads through each block in the linked list, myBlocks to read code? -a.
    public String unPackAsCode() {
        String passBack = "";

        passBack += "ask " + plural() + " [\n";
        for( CodeBlock block : myBlocks ) {
            passBack += block.unPackAsCode();
        }
        passBack += "]\n";

        return passBack;
    }

    public void makeLabel() {
        JPanel label = new JPanel();
        // TODO: This is a hard coded hack for now. Fix it.
        label.add(removeButton);
        label.add( new JLabel( "Ask" ) );

        // number of turtles of a breed starting with -a.
        number = new PrettyInput( this );
        number.setText( "100" );
        label.add( number );

        //commented out on Feb 4 when breed block was separated from variation Block
        //traitLabel = new JTextField();
        //label.add(traitLabel);
        //traitLabel.setText(trait + variation);

        
        /* name of the breed take from plural -a. Getting plural from textfield (Nov 24)
        name comes from getName in CodeBlock which takes the parameter passed to BreedBlock as name,
        or from textfield Pretty Input.
        I want the label to come from CodeBlock name or breedTypeSelector

*/
        plural = new PrettyInput( this );
        plural.setText( getName() );
        label.add( plural );
        //System.out.println(plural);



        // add button to change shape -a.
        label.add( new JLabel( " (" ) );
        label.add( makeBreedShapeButton() );
        label.add( new JLabel( ") to..." ) );



        label.setBackground(getBackground());
        add(label);
        //traitLabel.setText("");
    }

    //not sure what this does - think has to do with picking shape -a.
    // NM. It's never used -A. (aug 25)
    private final javax.swing.Action pickBreedShape =
		new javax.swing.AbstractAction() {
            public void actionPerformed( java.awt.event.ActionEvent e ) {

            }
        };

    public JButton makeBreedShapeButton() {
        breedShapeButton = new JButton( new ShapeIcon( org.nlogo.shape.VectorShape.getDefaultShape() ) );
        breedShapeButton.setActionCommand( this.getName() );
        breedShapeButton.addActionListener(this);
        breedShapeButton.setSize( 40 , 40 );
        return breedShapeButton;
    }

    // when clicks on shape selection -a.
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        ShapeSelector myShapeSelector = new ShapeSelector( parentFrame , allShapes() , this );
        myShapeSelector.setVisible(true);
        breedShapeButton.setIcon( new ShapeIcon( myShapeSelector.getShape() ) );
        breedShape = myShapeSelector.getChosenShape();
    }

    // getting shapes from NL -a.
    String[] allShapes() {
        String[] defaultShapes =
			org.nlogo.util.Utils.getResourceAsStringArray
			( "/system/defaultShapes.txt" ) ;
		String[] libraryShapes =
			org.nlogo.util.Utils.getResourceAsStringArray
			( "/system/libraryShapes.txt" ) ;
		String[] mergedShapes =
			new String[ defaultShapes.length + 1 + libraryShapes.length ] ;
		System.arraycopy( defaultShapes , 0 ,
						  mergedShapes , 0 ,
						  defaultShapes.length ) ;
		mergedShapes[ defaultShapes.length ] = "" ;
		System.arraycopy( libraryShapes , 0 ,
						  mergedShapes , defaultShapes.length + 1 ,
						  libraryShapes.length ) ;
        return defaultShapes; // NOTE right now just doing default
    }
    
    public java.util.List<Shape> parseShapes( String [] shapes , String version )
	{
		return org.nlogo.shape.VectorShape.parseShapes( shapes , version ) ;
	}

    /*
    Michelle's code for shapes for breeds thta have shape. I removed them because I've added variation to name of breed
    -A. (Nov 23)
    public String setBreedShape() {
        if( breedShape != null ) {
            return "set-default-shape " + plural() + " \"" + breedShape + "\"\n";
        }
        return "";
    }
    */

    public Breed myBreed() {
        return breed;
    }

    // events and mouse action but not sure what it's doing -a.
    public void mouseEnter( MouseEvent evt ) {}
    public void mouseExit( MouseEvent evt ) {}
    public void mouseEntered( MouseEvent evt ) {}
    public void mouseExited( MouseEvent evt ) {}
    public void mouseClicked( MouseEvent evt ) {}
    public void mouseMoved(MouseEvent evt) {}
    public void mouseReleased(MouseEvent evt) {}

    int beforeDragX;
    int beforeDragY;

    int beforeDragXLoc;
    int beforeDragYLoc;

    // how the breed block being moved works
    public void mousePressed( java.awt.event.MouseEvent evt ) {
        Point point = evt.getPoint();
        javax.swing.SwingUtilities.convertPointToScreen( point , this );
        beforeDragX = point.x;
        beforeDragY = point.y;
        beforeDragXLoc = getLocation().x;
        beforeDragYLoc = getLocation().y;
    }

    // not sure what the difference is between beforeDragX and beforeDragXLoc -a.
    public void mouseDragged( java.awt.event.MouseEvent evt ) {
        Point point = evt.getPoint();
        javax.swing.SwingUtilities.convertPointToScreen( point , this );
        this.setLocation(
                point.x - beforeDragX + beforeDragXLoc ,
                point.y - beforeDragY + beforeDragYLoc );
    }

    public void repaint() {
        if( parentFrame != null ) {
            parentFrame.repaint();
        }
        super.repaint();
    }
}

