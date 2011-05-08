package org.nlogo.agent ;

// This is the old AnimationRenderer class
// is doesn't depend on opengl though and really
// all it does is upate the heading of the observer.
// makes more sense down here I think.

strictfp class HeadingSmoother
{
	private double thirdPersonOldHeading = 0.0d ;
	private double firstPersonOldHeading = 0.0d ;
	
	private double angleChange = 1.0d ;
	private double oldHeading = 0.0d ;
	
	public double follow( org.nlogo.api.Agent agent )
	{
		thirdPersonUpdate( agent ) ;
		return thirdPersonOldHeading ; 
	}
	
	public double watch( org.nlogo.api.Agent agent )
	{
		firstPersonUpdate( agent ) ;
		return firstPersonOldHeading ;
	}

	private void thirdPersonUpdate( org.nlogo.api.Agent agent )
    {
		double heading = 0 ;

		if( agent instanceof org.nlogo.agent.Turtle )
 		{
			heading = ((org.nlogo.agent.Turtle) agent ).heading() ;
		}
			
      	if ( thirdPersonOldHeading + angleChange <= heading )
		{
			if ( heading - thirdPersonOldHeading > 180.0d )
			{
				thirdPersonOldHeading -= angleChange ;
			}
			else
			{
				thirdPersonOldHeading += angleChange ;
			}
		}
		else if ( thirdPersonOldHeading - angleChange >= heading )
		{
			if ( thirdPersonOldHeading - heading > 180.0d )
			{
				thirdPersonOldHeading += angleChange ;
			}
			else
			{
				thirdPersonOldHeading -= angleChange ;
			}
		}
		else
		{
			thirdPersonOldHeading = heading ;
			angleChange = 1.0d ;
		}
		
		if ( thirdPersonOldHeading >= 360.0d )
		{
			thirdPersonOldHeading -= 360.0d ;
		}
		else if ( thirdPersonOldHeading < 0.0d )
		{
			thirdPersonOldHeading += 360.0d ;
		}
		
		if ( heading == oldHeading )
		{
			angleChange = angleChange * 1.5d ;
		}
		else
		{
			angleChange = angleChange / 1.5d ;
			if ( angleChange < 1.0d )
			{
				angleChange = 1.0d ;	
			}
		}
		
		oldHeading = heading ;
    }
    
    private void firstPersonUpdate( org.nlogo.api.Agent agent )
    {
      	double heading = 0 ;

		if( agent instanceof org.nlogo.agent.Turtle )
		{
			heading = ( (org.nlogo.agent.Turtle) agent).heading() ;
		}
	
      	if ( firstPersonOldHeading + angleChange <= heading )
		{
			if ( heading - firstPersonOldHeading > 180.0d )
			{
				firstPersonOldHeading -= angleChange ;
			}
			else
			{
				firstPersonOldHeading += angleChange ;
			}
		}
		else if ( firstPersonOldHeading - angleChange >= heading )
		{
			if ( firstPersonOldHeading - heading > 180.0d )
			{
				firstPersonOldHeading += angleChange ;
			}
			else
			{
				firstPersonOldHeading -= angleChange ;
			}
		}
		else
		{
			firstPersonOldHeading = heading ;
			angleChange = 6.0d ;
		}
		
		if ( firstPersonOldHeading >= 360.0d )
		{
			firstPersonOldHeading -= 360.0d ;
		}
		else if ( firstPersonOldHeading < 0.0d )
		{
			firstPersonOldHeading += 360.0d ;
		}
		
		angleChange = angleChange * 1.75d ;
    }    
}
