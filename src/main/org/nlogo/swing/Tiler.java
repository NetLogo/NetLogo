package org.nlogo.swing ;

import java.awt.Window;

public final strictfp class Tiler
{

	// this class is not instantiable
	private Tiler() { throw new IllegalStateException() ; }

	public static java.awt.Point findEmptyLocation( java.util.List<Window> otherWindows , Window window )
	{
		java.awt.Frame parentFrame = (java.awt.Frame) window.getParent() ;
		java.awt.Rectangle availBounds = java.awt.GraphicsEnvironment
			.getLocalGraphicsEnvironment().getMaximumWindowBounds() ;
			
		// first see if there's room to the right of the parent frame
		if( parentFrame.getLocation().x + parentFrame.getWidth() + window.getWidth()
			<= availBounds.x + availBounds.width )
		{
			java.awt.Point loc = slideDown
				( otherWindows , window , availBounds ,
				  parentFrame.getLocation().x + parentFrame.getWidth() ,
				  parentFrame.getLocation().y + parentFrame.getInsets().top ) ;
			if( loc != null )
			{
				return loc ;
			}
		}
		// next see if there's room below
		if( parentFrame.getLocation().y + parentFrame.getHeight() + window.getHeight()
			<= availBounds.y + availBounds.height )
		{
			java.awt.Point loc = slideRight
				( otherWindows , window , availBounds ,
				  parentFrame.getLocation().x ,
				  parentFrame.getLocation().y + parentFrame.getHeight() ) ;
			if( loc != null )
			{
				return loc ;
			}
		}
		// next try the left side
		if( parentFrame.getLocation().x - window.getWidth() >= 0 )
		{
			java.awt.Point loc = slideDown
				( otherWindows , window , availBounds ,
				  parentFrame.getLocation().x - window.getWidth() ,
				  parentFrame.getLocation().y + parentFrame.getInsets().top ) ;
			if( loc != null )
			{
				return loc ;
			}
		}
		// try against the right edge of the screen
		java.awt.Point loc = slideDown
			( otherWindows , window , availBounds ,
			  availBounds.x + availBounds.width - window.getWidth() ,
			  parentFrame.getLocation().y + parentFrame.getInsets().top ) ;
		if( loc != null )
		{
			return loc ;
		}
		// try against the bottom edge of the screen
		loc = slideRight( otherWindows , window , availBounds , 0 ,
						  availBounds.y + availBounds.height - window.getHeight() ) ;
		if( loc != null )
		{
			return loc ;
		}
		// try against the left edge of the screen
		loc = slideDown( otherWindows , window , availBounds , 0 ,
						 parentFrame.getLocation().y + parentFrame.getInsets().top ) ;
		if( loc != null )
		{
			return loc ;
		}
		// put it in the lower right corner of the screen
		return new java.awt.Point
			( availBounds.x + availBounds.width  - window.getWidth() ,
			  availBounds.y + availBounds.height - window.getHeight() ) ;
	}

	private static java.awt.Point slideDown( java.util.List<Window> otherWindows ,
											 Window window ,
											 java.awt.Rectangle availBounds ,
											 int x , int y )
	{
		for( ; y + window.getHeight() <= availBounds.y + availBounds.height ;
			 y++ )
		{
			if( emptyLocation( otherWindows , window , x , y ) )
			{
				return new java.awt.Point( x , y ) ;
			}
		}
		return null ;
	}

	private static java.awt.Point slideRight( java.util.List<Window> otherWindows ,
											  Window window ,
											  java.awt.Rectangle availBounds ,
											  int x , int y )
	{
		for( ; x + window.getWidth() <= availBounds.x + availBounds.width ;
			 x++ )
		{
			if( emptyLocation( otherWindows , window , x , y ) )
			{
				return new java.awt.Point( x , y ) ;
			}
		}
		return null ;
	}

	private static boolean emptyLocation( java.util.List<Window> otherWindows , Window window , int x , int y )
	{
		for( Window otherWindow : otherWindows )
		{
			if( window != otherWindow && overlap( otherWindow , window , x , y ) )
			{
				return false ;
			}
		}
		return true ;
	}

	private static boolean overlap( Window otherWindow , Window window ,
									int x , int y )
	{
		return otherWindow.getBounds().intersects
			( new java.awt.Rectangle( new java.awt.Point( x , y ) ,
									  window.getSize() ) ) ;
	}

}
