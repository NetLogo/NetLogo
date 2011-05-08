package org.nlogo.api ;

public interface ViewInterface
{
	boolean viewIsVisible() ;
	void framesSkipped() ;

	boolean isDead() ;
	void paintImmediately( boolean force ) ;
	void incrementalUpdateFromEventThread() ;
	void repaint() ;

	double mouseXCor() ;
	double mouseYCor() ;
	boolean mouseDown() ;
	boolean mouseInside() ; 
	void resetMouseCors() ;

	void shapeChanged( org.nlogo.api.Shape shape ) ;
	void applyNewFontSize( int fontSize , int zoom ) ;
}
