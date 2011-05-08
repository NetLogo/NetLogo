package org.nlogo.prim.etc ;

import org.nlogo.agent.Agent;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;

public final strictfp class _distancenowrap extends Reporter
{
	@Override public Syntax syntax()
	{
		int[] right = { Syntax.TYPE_TURTLE | Syntax.TYPE_PATCH } ;
		return Syntax.reporterSyntax( right , Syntax.TYPE_NUMBER , "-TP-" ) ;
	}
	@Override public Object report( Context context ) throws LogoException
	{
		return report_1( context , argEvalAgent( context , 0 ) ) ;
	}
	public double report_1( Context context , Agent arg0 ) throws LogoException
	{
		if( arg0 instanceof org.nlogo.agent.Link )
		{
			throw new EngineException
				( context , this , "expected this to be a turtle or a patch but got a link instead" ) ;
		}		
		if( arg0.id == -1 )
		{
			throw new EngineException( context , this , "that turtle is dead" ) ;
		}
		return world.protractor().distance( context.agent , arg0 , false ) ; // false = don't wrap
	}
}
