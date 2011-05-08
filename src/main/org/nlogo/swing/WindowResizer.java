package org.nlogo.swing ;

public strictfp class WindowResizer
	extends javax.swing.JPanel
	implements
		java.awt.event.MouseListener ,
		java.awt.event.MouseMotionListener
{

	private final javax.swing.JWindow window ;
	private java.awt.Point mousePressAbsLoc ;
	private java.awt.Dimension sizeWhenPressed ;
	
	public WindowResizer( javax.swing.JWindow window )
	{
		this.window = window ;
		addMouseListener( this ) ;
		addMouseMotionListener( this ) ;
	}

	public void mouseMoved(java.awt.event.MouseEvent e ) { /* ignore */ }
	public void mouseClicked( java.awt.event.MouseEvent e ) { /* ignore */ }
	public void mouseEntered( java.awt.event.MouseEvent e ) { /* ignore */ }
	public void mouseExited( java.awt.event.MouseEvent e ) { /* ignore */ }
	public void mouseReleased( java.awt.event.MouseEvent e ) { /* ignore */ }

	public void mousePressed( java.awt.event.MouseEvent e ) 
	{
		java.awt.Point mousePressLoc = e.getPoint() ;
		mousePressAbsLoc = new java.awt.Point( mousePressLoc ) ;
		org.nlogo.awt.Utils.convertPointToScreen( mousePressAbsLoc , WindowResizer.this ) ;
		sizeWhenPressed = window.getSize() ;
	}

	public void mouseDragged( java.awt.event.MouseEvent e )
	{
		java.awt.Point dragAbsLoc = new java.awt.Point( e.getPoint() ) ;
		org.nlogo.awt.Utils.convertPointToScreen( dragAbsLoc , WindowResizer.this ) ;
		window.setSize
			( sizeWhenPressed.width + ( dragAbsLoc.x - mousePressAbsLoc.x ) ,
			  sizeWhenPressed.height + ( dragAbsLoc.y - mousePressAbsLoc.y ) ) ;
	}
}
