package org.nlogo.prim.etc ;

import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;

public final strictfp class _towardsnowrap extends Reporter
{
	@Override public Syntax syntax()
	{
		int[] right = { Syntax.TYPE_TURTLE | Syntax.TYPE_PATCH } ;
		return Syntax.reporterSyntax( right , Syntax.TYPE_NUMBER , "-TP-" ) ;
	}
	@Override public Object report( Context context ) throws LogoException
	{
		org.nlogo.agent.Agent agent = argEvalAgent( context , 0 ) ;
		if( agent instanceof org.nlogo.agent.Link )
		{
			throw new EngineException
				( context , this , 
				  I18N.errors().get("org.nlogo.prim.etc.$common.expectedTurtleOrPatchButGotLink") ) ;
		}
		if( agent.id == -1 )
		{
			throw new EngineException( context , this , I18N.errors().get("org.nlogo.$common.thatTurtleIsDead")) ;
		}
		try
		{
			return validDouble
				( world.protractor().towards
				  ( context.agent , agent , false ) ) ; // false = don't wrap
		}
		catch( org.nlogo.api.AgentException ex )
		{
			throw new EngineException
				( context , this  , ex.getMessage() ) ;
		}
	}
}
