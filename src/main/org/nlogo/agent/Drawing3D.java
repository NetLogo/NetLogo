package org.nlogo.agent ;

import java.util.ArrayList;
import java.util.List;

public strictfp class Drawing3D
	implements org.nlogo.api.Drawing3D
{

	private final World3D world ;
	private final List<org.nlogo.api.DrawingLine3D> lines ;
	private final List<org.nlogo.api.TurtleStamp3D> turtleStamps ;
	private final List<org.nlogo.api.LinkStamp3D> linkStamps ;

	Drawing3D( World3D world )
	{
		this.world = world ;
		lines = new ArrayList<org.nlogo.api.DrawingLine3D>() ;
		turtleStamps = new ArrayList<org.nlogo.api.TurtleStamp3D>() ;
		linkStamps = new ArrayList<org.nlogo.api.LinkStamp3D>() ;
	}

	void clear()
	{
		lines.clear() ;
		turtleStamps.clear() ;
		linkStamps.clear() ;
	}

	public List<org.nlogo.api.DrawingLine3D> lines()
	{
		return lines ;
	}

	public List<org.nlogo.api.TurtleStamp3D> turtleStamps()
	{
		return turtleStamps ;
	}

	public Iterable<org.nlogo.api.LinkStamp3D> linkStamps()
	{
		return linkStamps ;
	}

	void stamp( Agent agent )
	{
		if( agent instanceof Turtle )
		{
			turtleStamps.add( new TurtleStamp3D( (Turtle3D)agent ) ) ;
		}
		else
		{
			linkStamps.add( new LinkStamp3D( (Link3D) agent ) ) ;
		}
	}

	void drawLine( double x0 , double y0 , double z0 , 
				   double x1 , double y1 , double z1 , 
				   double width , Object color )
	{
		DrawingLine3D l = new DrawingLine3D( x0 , y0 , z0 , x1 , y1 , z1 , 
											 width , color , world ) ;

		wrap( l ) ;
	}

	void addLine( double x0 , double y0 , double z0 , 
				  double x1 , double y1 , double z1 , 
				  double width , Object color )
	{
 		lines.add( new DrawingLine3D( x0 , y0 , z0 , x1 , y1 , z1 , 
									  width , color , world ) ) ;
	}

	void addStamp( String shape , double xcor , double ycor , double zcor , double size ,
				   double heading , double pitch , double roll , double color , double lineThickness )
	{
		turtleStamps.add( new TurtleStamp3D( shape , xcor , ycor , zcor , size ,
											 heading , pitch , roll , color , 
											 lineThickness ) ) ;
	}

	void addStamp( String shape , double x1 , double y1 , double z1 , double x2 , double y2 , double z2 ,
				   Object color , double lineThickness , boolean directedLink , double destSize ,
				   double heading , double pitch )
	{
		linkStamps.add( new LinkStamp3D( shape , x1 , y1 , z1 , x2 , y2 , z2 , color , 
										 lineThickness , directedLink , destSize , heading , pitch ) ) ;
	}


	private void wrap( DrawingLine3D l ) 
	{
		double startX = l.x0 ;
		double startY = l.y0 ;
		double endX = l.x0 ;
		double endY = l.y0 ;
		double startZ = l.z0 ;
		double endZ = l.z0 ;
		double temp ;

		if ( endX < startX )
	    {
			temp = endX ;
			endX = startX ;
			startX = temp ;
		}
		if ( endY < startY )
	    {
			temp = endY ;
			endY = startY ;
			startY = temp ;
		}
		if ( endZ < startZ )
	    {
			temp = endZ ;
			endZ = startZ ;
			startZ = temp ;
		}
				
		double xdiff = l.x1 - l.x0 ;
		double ydiff = l.y1 - l.y0 ;
		double zdiff = l.z1 - l.z0 ;
		double distX = l.x1 - l.x0 ;
		double distY = l.y1 - l.y0 ;
		double distZ = l.z1 - l.z0 ;
		double newStartX = 0 ;
		double newStartY = 0 ;
		double newStartZ = 0 ;
		double maxy = world.maxPycor() + 0.4999999 ;
		double maxx = world.maxPxcor() + 0.4999999 ;
		double maxz = world.maxPzcor() + 0.4999999 ;
		double miny = world.minPycor() - 0.5 ;
		double minx = world.minPxcor() - 0.5 ;
		double minz = world.minPzcor() - 0.5 ;
		double pixelSize = 1 / world.patchSize() ;

		do{
			endX = startX + distX ;
			endY = startY + distY ;
			endZ = startZ + distZ ;

			if( endY < miny )
			{
				endX = ( miny - startY ) * xdiff / ydiff + startX ;
				endY = miny ;
				endZ = ( miny - startY ) * zdiff / ydiff + startZ ;
				newStartY = maxy ;
				newStartX =  endX ;
				newStartZ = endZ ;

				if( newStartX == minx )
				{
					newStartX = maxx ;
				}
				else if( newStartX == maxx )
				{
					newStartX = minx ;
				}
				if( newStartZ == maxz )
				{
					newStartZ = minz ;
				}
				else if( newStartZ == minz )
				{
					newStartZ = maxz ;
				}
			}
			if( endY > maxy )
			{
				endX =  startX + ( ( maxy - startY ) * xdiff / ydiff ) ;
				endY = maxy ;
				endZ =  startZ + ( ( maxy - startY ) * zdiff / ydiff ) ;
				newStartX = endX ;
				newStartY = miny ;
				newStartZ = endZ ;
				if( newStartX == minx )
				{
					newStartX = maxx ;
				}
				else if( newStartX == maxx )
				{
					newStartX = minx ;
				}
				if( newStartZ == maxz )
				{
					newStartZ = minz ;
				}
				else if( newStartZ == minz )
				{
					newStartZ = maxz ;
				}
			}
			if( endX < minx )
			{
				endX = minx ;
				endY = ( ydiff * ( endX - startX ) ) / xdiff + startY ;
				endZ = ( zdiff * ( endX - startX ) ) / xdiff + startZ ;
				newStartX = maxx ;
				newStartY = endY ;
				newStartZ = endZ ;
				if( newStartY == miny )
				{
					newStartY = maxy ;
				}
				else if( newStartY == maxy )
				{
					newStartY = miny ;
				}
				if( newStartZ == maxz )
				{
					newStartZ = minz ;
				}
				else if( newStartZ == minz )
				{
					newStartZ = maxz ;
				}
			}
			if( endX > maxx )
			{
				endX = maxx ;
				endY = ( ydiff * ( endX - startX ) ) / xdiff + startY ;
				endZ = ( zdiff * ( endX - startX ) ) / xdiff + startZ ;
				newStartX = minx ;
				newStartY = endY ;
				newStartZ = endZ ;
				if( newStartY == miny )
				{
					newStartY = maxy ;
				}
				else if( newStartY == maxy )
				{
					newStartY = miny ;
				}
				if( newStartZ == maxz )
				{
					newStartZ = minz ;
				}
				else if( newStartZ == minz )
				{
					newStartZ = maxz ;
				}
			}
			if( endZ < minz )
			{
				endZ = minz ;
				endY = ( ydiff * ( endZ - startZ ) ) / zdiff + startY ;
				endX = ( xdiff * ( endZ - startZ ) ) / zdiff + startX ;
				newStartZ = maxz ;
				newStartY = endY ;
				newStartX = endX ;
				if( newStartY == miny )
				{
					newStartY = maxy ;
				}
				else if( newStartY == maxy )
				{
					newStartY = miny ;
				}
				if( newStartX == minx )
				{
					newStartX = maxx ;
				}
				else if( newStartX == maxx )
				{
					newStartX = minx ;
				}
			}
			if( endZ > maxz )
			{
				endZ = maxz ;
				endY = ( ydiff * ( endZ - startZ ) ) / zdiff + startY ;
				endX = ( xdiff * ( endZ - startZ ) ) / zdiff + startX ;
				newStartZ = minz ;
				newStartY = endY ;
				newStartX = endX ;
				if( newStartY == miny )
				{
					newStartY = maxy ;
				}
				else if( newStartY == maxy )
				{
					newStartY = miny ;
				}
				if( newStartX == minx )
				{
					newStartX = maxx ;
				}
				else if( newStartX == maxx )
				{
					newStartX = minx ;
				}
			}

			lines.add( new DrawingLine3D( startX , startY , startZ , 
										  endX , endY , endZ , 
										  l.width , l.color , world ) ) ;

			distX -= ( endX - startX ) ; 
			distY -= ( endY - startY ) ;
			distZ -= ( endZ - startZ ) ;

			startX = newStartX ;
			startY = newStartY ;
			startZ = newStartZ ;
		} while ( StrictMath.abs( distY ) >= pixelSize 
				  || StrictMath.abs( distX ) >= pixelSize 
				  || StrictMath.abs( distZ ) >= pixelSize ) ;
	}
}
