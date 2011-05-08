package org.nlogo.agent ;

import org.nlogo.api.AgentException;

public strictfp class DrawingLine3D
	implements org.nlogo.api.DrawingLine3D
{
	public double x0 ;
	public double x0() { return x0 ; }
	public double y0 ;
	public double y0() { return y0 ; }
	public double z0 ;
	public double z0() { return z0 ; }
	public double x1 ;
	public double x1() { return x1 ; }
	public double y1 ;
	public double y1() { return y1 ; }
	public double z1 ;
	public double z1() { return z1 ; }
	public double width ;
	public double width() { return width ; }
	public Object color ;
	public Object color() { return color ; }
	public double heading ;
	public double heading() { return heading ; }
	public double pitch ;
	public double pitch() { return pitch ; }

	DrawingLine3D( double x0 , double y0 , double z0 , 
				   double x1 , double y1 , double z1 , 
				   double width , Object color , World3D world )
	{
		this.x0 = x0 ;
		this.y0 = y0 ;
		this.z0 = z0 ;
		this.x1 = x1 ;
		this.y1 = y1 ;
		this.z1 = z1 ;
		this.width = width ;
		this.color = color ;
		heading = heading( world ) ;
		pitch = pitch( world ) ;
	}

	public double length()
	{
		double xdiff = x1 - x0 ;
		double ydiff = y1 - y0 ;
		double zdiff = z1 - z0 ;
		return StrictMath.sqrt( ( xdiff * xdiff ) + ( ydiff * ydiff ) + ( zdiff * zdiff ) ) ;
	}

	private double heading( World3D world )
	{
		try
		{
			return world.protractor().towards( x0 , y0 , x1 , y1 , true ) ;
		}
		catch( AgentException e )
		{
			return 0.0 ;
		}
	}
	
	private double pitch( World3D world )
	{
		try
		{
			return world.protractor().towardsPitch( x0 , y0 , z0 , x1 , y1 , z1 , true ) ;
		}
		catch( AgentException e )
		{
			throw new IllegalStateException( e ) ;
		}
	}
}
