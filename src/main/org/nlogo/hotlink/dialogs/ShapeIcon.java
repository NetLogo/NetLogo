// TODO: This will eventually be called from deltatick

package org.nlogo.hotlink.dialogs;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.Serializable;

import javax.swing.Icon;
import javax.swing.JButton;

import org.nlogo.api.Color;
import org.nlogo.api.GraphicsInterface;
import org.nlogo.shape.DrawableShape;
import org.nlogo.shape.editor.DrawableList;

public class ShapeIcon implements javax.swing.Icon, Serializable {
	
	DrawableShape shape;
    int size = 20;
    java.awt.Color bgColor = java.awt.Color.white;
    java.awt.Color fgColor = org.nlogo.api.Color.getColor( (Double) 5.0 );

    public ShapeIcon( DrawableShape shape ) {
        super();
        this.shape = shape;
        this.size = 20;
    }
	
	public ShapeIcon( DrawableShape shape , int size )
	{	
		super();
		this.shape = shape;
        this.size = size;

	}

	public ShapeIcon( DrawableShape shape , int size , java.awt.Color color )
	{
		super();
		this.shape = shape;
        this.size = size;
        this.bgColor = color;
	}

    public ShapeIcon( DrawableShape shape , java.awt.Color color )
	{
		super();
		this.shape = shape;
        this.bgColor = color;
	}
	
	public int getIconHeight() {
		return size;
	}

	public int getIconWidth() {
		return size;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D g2 = (Graphics2D) g;

		shape.paint(
            new org.nlogo.api.Graphics2DWrapper( g2 ) ,
			fgColor ,
			x , 
            y ,
            size ,
            0
        );
	}

    public void setColor ( java.awt.Color color ) {
        fgColor = color;
    }
		
}		
