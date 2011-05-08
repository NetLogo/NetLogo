package org.nlogo.api;

public interface RendererInterface
{
	int SHAPE_WIDTH = 300 ;
	java.awt.Color VIEW_BACKGROUND = new java.awt.Color( 180 , 180 , 180 ) ;
	TrailDrawerInterface trailDrawer() ;
	void changeTopology( boolean wrapX , boolean wrapY ) ;
	void paint( GraphicsInterface g , ViewSettings settings ) ;
	void paint( java.awt.Graphics2D g , ViewSettings settings ) ;
	void resetCache( double patchSize ) ;
    double graphicsX( double xcor , double patchSize , double viewOffsetX ) ;
	double graphicsY( double ycor , double patchSize , double viewOffsetY ) ;
    void outlineAgent( Agent agent ) ;
	void exportView( java.awt.Graphics2D g , ViewSettings settings ) ;
	java.awt.image.BufferedImage exportView( ViewSettings settings ) ;
	void prepareToPaint( ViewSettings settings , int width , int height ) ;
}
