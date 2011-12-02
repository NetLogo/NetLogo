// TODO: Eventually this will be called from deltatick

package org.nlogo.hotlink.dialogs;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JButton;

import org.nlogo.api.Color;
import org.nlogo.api.GraphicsInterface;
import org.nlogo.shape.DrawableShape;
import org.nlogo.shape.editor.DrawableList;

public class StackedShapeIcon extends ShapeIcon implements javax.swing.Icon {

    public StackedShapeIcon( DrawableShape shape ) {
        super( shape );
    }
	
	public StackedShapeIcon( DrawableShape shape , int size )
	{	
		super( shape , size );
	}

	public StackedShapeIcon( DrawableShape shape , int size , java.awt.Color color )
	{
		super( shape , size , color );
	}

	public StackedShapeIcon( DrawableShape shape , java.awt.Color color )
	{
		super( shape , color );
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		//org.nlogo.api.GraphicsInterface
		//Graphics2D graphics = new org.nlogo.api.Graphics2DWrapper( g );
		Graphics2D g2 = (Graphics2D) g;

		shape.paint(
            new org.nlogo.api.Graphics2DWrapper( g2 ) ,
            org.nlogo.api.Color.getColor( (Double) 5.0 ) ,
            0 ,
            (int) (y - Math.round(size * .1)) ,
            size * .75 ,
            0
        );
		shape.paint(
            new org.nlogo.api.Graphics2DWrapper( g2 ) ,
            org.nlogo.api.Color.getColor( (Double) 3.0 ) ,
            (int) (x + Math.round(size * .2)) ,
            (int) (y + Math.round(size * .1)) ,
            size * .75 ,
            0
        );
		shape.paint(
            new org.nlogo.api.Graphics2DWrapper( g2 ) ,
            org.nlogo.api.Color.getColor( (Double) 7.0 ) ,
            (int) (x + Math.round(size * .1) ) ,
            (int) (y + Math.round(size * .3) ),
            size * .75 ,
            0
        );
		
	}
		
}		
