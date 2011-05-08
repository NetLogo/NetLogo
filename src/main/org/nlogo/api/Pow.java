package org.nlogo.api ;

public final strictfp class Pow
{

	// this class is not instantiable
	private Pow() { throw new IllegalStateException() ; }

	public static double pow( double d0 , double d1 )
	{
		if( d1 == 0 )
		{
			return 1.0 ;
		}
		// If there is some more efficient way to test
		// whether a double has no fractional part and
		// lies in IEEE 754's exactly representable range,
		// I would love to know about it. - ST 5/31/06, 3/4/11
		long n = (long) d1 ;
		if( n != d1 ||
			n < -9007199254740992L ||
			n > 9007199254740992L )
		{
			return StrictMath.pow( d0 , d1 ) ;
		}
		if( n < 0 )
		{
			d0 = 1 / d0 ;
			n = -n ;
		}
		// see http://en.wikipedia.org/wiki/Exponentiation_by_squaring
		double result = 1 ;
		while( n > 0 )
		{
			if( n % 2 == 1 )
			{
				result *= d0 ;
			}
			d0 *= d0 ;
			n /= 2 ;
		}
		return result ;
	}

}
