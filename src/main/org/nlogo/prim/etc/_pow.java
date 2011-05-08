package org.nlogo.prim.etc ;

import org.nlogo.api.LogoException;
import org.nlogo.api.Pow;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Pure;
import org.nlogo.nvm.Syntax;

public final strictfp class _pow extends Reporter implements Pure
{
	@Override public Syntax syntax()
	{
		int left = Syntax.TYPE_NUMBER ;
		int[] right = { Syntax.TYPE_NUMBER } ;
		return Syntax.reporterSyntax( left , right , Syntax.TYPE_NUMBER ,
									  Syntax.NORMAL_PRECEDENCE - 1 ) ;
	}
	@Override public Object report( Context context ) throws LogoException
	{
		return report_1( context ,
						 argEvalDoubleValue( context , 0 ) ,
						 argEvalDoubleValue( context , 1 ) ) ;
	}
	public double report_1( Context context , double d0 , double d1 )
		throws LogoException
	{
		return validDouble( Pow.pow( d0 , d1 ) ) ;
	}
}
