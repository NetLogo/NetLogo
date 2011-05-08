package org.nlogo.prim.threed;

import org.nlogo.agent.Link;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _linkpitch extends Reporter
{
	@Override public Syntax syntax()
	{
		return Syntax.reporterSyntax
			( Syntax.TYPE_NUMBER , "---L" ) ;
	}
	@Override public Object report( Context context ) throws LogoException
	{
		return report_1( context ) ;
	}
	public double report_1( Context context )
		throws LogoException
	{
		try
		{
			Link link = (Link) context.agent ;
			return world.protractor().towardsPitch( link.end1() , link.end2() , true ) ;
		}
		catch ( org.nlogo.api.AgentException e )
		{
			throw new org.nlogo.nvm.EngineException
				( context , this , 
				  "there is no pitch of a link whose endpoints are in the same position" ) ;
		}
	}
}
