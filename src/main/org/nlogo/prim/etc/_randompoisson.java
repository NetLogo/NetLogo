package org.nlogo.prim.etc ;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _randompoisson extends Reporter
{
	@Override public Syntax syntax()
	{
		return Syntax.reporterSyntax
			( new int[] { Syntax.TYPE_NUMBER } ,
			  Syntax.TYPE_NUMBER ) ;
	}
	@Override public Object report( Context context ) throws LogoException
	{
		return report_1( context , argEvalDoubleValue( context , 0 ) ) ;
	}
	public double report_1( Context context , double mean )
	{
		int q = 0 ;
		double sum = - StrictMath.log( 1 - context.job.random.nextDouble() ) ;
		while( sum <= mean )
		{
			q++ ;
			sum -= StrictMath.log( 1 - context.job.random.nextDouble() ) ;
		}
		return q ;
	}
}
