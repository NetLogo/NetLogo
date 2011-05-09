package org.nlogo.prim.etc ;

import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Pure;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;

public final strictfp class _mod extends Reporter implements Pure
{
	@Override public Syntax syntax()
	{
		int left = Syntax.TYPE_NUMBER ;
		int[] right = { Syntax.TYPE_NUMBER } ;
		return Syntax.reporterSyntax( left , right , Syntax.TYPE_NUMBER ,
									  Syntax.NORMAL_PRECEDENCE - 2 ) ;
	}
	@Override public Object report( Context context ) throws LogoException
	{
		return report_1( context ,
						 argEvalDoubleValue( context , 0 ) ,
						 argEvalDoubleValue( context , 1 ) ) ;
	}
	public double report_1( Context context , double d0 , double d1 ) throws LogoException
	{
		if( d1 == 0 )
		{
			throw new EngineException(context , this , I18N.errors().get("org.nlogo.prim.etc.$common.divByZero"));
		}
		return validDouble( d0 - ( StrictMath.floor( d0 / d1 ) * d1 ) ) ;
	}
}
