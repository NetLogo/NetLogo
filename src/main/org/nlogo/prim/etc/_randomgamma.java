package org.nlogo.prim.etc ;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;

public final strictfp class _randomgamma extends Reporter
{
	@Override public Syntax syntax()
	{
		return Syntax.reporterSyntax
			( new int[] { Syntax.TYPE_NUMBER , Syntax.TYPE_NUMBER } ,
			  Syntax.TYPE_NUMBER ) ;
	}
	@Override public Object report( Context context ) throws LogoException
	{
		return report_1( context ,
						 argEvalDoubleValue( context , 0 ) ,
						 argEvalDoubleValue( context , 1 ) ) ;
	}
	public double report_1( Context context , double alpha , double lambda ) throws LogoException
	{
		if( alpha <= 0 || lambda <= 0 )
		{
			throw new EngineException
				( context , this , "both inputs to " + displayName() +
				  " must be positive" ) ;
		}
		return validDouble
			( org.nlogo.agent.Gamma.nextDouble
			  ( context.job.random , alpha , lambda ) ) ;
	}
}
