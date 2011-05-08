package org.nlogo.api ;

public final strictfp class Approximate
{

	// not instantiable
	private Approximate() { throw new IllegalStateException() ; }

	public static double approximate( double n , int places )
	{
		// the 17 here was not arrived at through any deep understanding
		// of IEEE 754 or anything like that, but just by entering different
		// expressions into NetLogo and noting that I couldn't seem to come
		// up with an expression that would make more than 17 decimal places
		// print; an example that makes 17 places print is
		// "show 0.1 - 0.00000000000000001" -- I think there may still be
		// theoretical correctness issues here, perhaps involving very large
		// or very small numbers, but for now this'll have to do - ST 5/3/02
		if( places >= 17 )
		{
			return n ;
		}
		double multiplier = StrictMath.pow ( 10, places ) ;
		double result = StrictMath.floor( n * multiplier + 0.5 ) / multiplier ;
		return places > 0
			? result
			: StrictMath.round( result ) ;
	}
	public static double approximateCeiling( double n , int places )
	{
		// see comment in approximate() about the 17 - ST 9/10/04
		if( places >= 17 )
		{
			return n ;
		}
		double multiplier = StrictMath.pow ( 10, places ) ;
		double result = StrictMath.ceil( n * multiplier ) / multiplier ;
		return places > 0
			? result
			: StrictMath.round( result ) ;
	}
	public static double approximateFloor( double n , int places )
	{
		// see comment in approximate() about the 17 - ST 9/10/04
		if( places >= 17 )
		{
			return n ;
		}
		double multiplier = StrictMath.pow ( 10, places ) ;
		double result = StrictMath.floor( n * multiplier ) / multiplier ;
		return places > 0
			? result
			: StrictMath.round( result ) ;
	}
}
