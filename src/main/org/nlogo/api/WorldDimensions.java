package org.nlogo.api ;

// use this class to wrap up dimensions to resize the world 
// using WorldResizer

public strictfp class WorldDimensions
{
	public int minPxcor ;
	public int maxPxcor ;
	public int minPycor ;
	public int maxPycor ;

	public WorldDimensions( int minx , int maxx , int miny , int maxy )
	{
		minPxcor = minx ;
		maxPxcor = maxx ;
		minPycor = miny ;
		maxPycor = maxy ;
	}

	public int width()
	{
		return maxPxcor - minPxcor + 1 ;
	}

	public int height()
	{
		return maxPycor - minPycor + 1 ;
	}
}
