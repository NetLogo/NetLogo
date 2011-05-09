package org.nlogo.prim.etc ;

import org.nlogo.agent.Agent;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;

public final strictfp class _distance extends Reporter
{
	@Override public Syntax syntax()
	{
		return Syntax.reporterSyntax
			( new int[] { Syntax.TYPE_TURTLE | Syntax.TYPE_PATCH } ,
			  Syntax.TYPE_NUMBER , "-TP-" ) ;
	}
	@Override public Object report( Context context ) throws LogoException
	{
		return report_1( context , argEvalAgent( context , 0 ) ) ;
	}
	public double report_1( Context context , Agent otherAgent ) throws LogoException
	{
		if( otherAgent instanceof org.nlogo.agent.Link )
		{
			throw new EngineException
				( context , this , I18N.errors().get("org.nlogo.prim.etc.$common.expectedTurtleOrPatchButGotLink")) ;
		}		
		if( otherAgent.id == -1 )
		{
			throw new EngineException( context , this , I18N.errors().get("org.nlogo.$common.thatTurtleIsDead") ) ;
		}
		return world.protractor().distance( context.agent , otherAgent , true ) ; // true = wrap
	}
}
