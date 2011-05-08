package org.nlogo.api;

public interface ViewSettings
{
	int fontSize() ;
	double patchSize() ;
	double viewWidth() ;
	double viewHeight() ;
	Perspective perspective() ;
    double viewOffsetX() ;
	double viewOffsetY() ;
    boolean drawSpotlight() ;
	boolean renderPerspective() ;
	boolean isHeadless() ;
}
