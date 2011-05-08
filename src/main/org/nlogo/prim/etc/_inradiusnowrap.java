package org.nlogo.prim.etc ;

import java.util.List;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentSet;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _inradiusnowrap
	extends Reporter
{
	@Override
	public Syntax syntax()
	{
		int left = Syntax.TYPE_AGENTSET ;
		int[] right = { Syntax.TYPE_NUMBER } ;
		int ret = Syntax.TYPE_AGENTSET ;
		return Syntax.reporterSyntax
			( left , right , ret , Syntax.NORMAL_PRECEDENCE + 2 ,
			  false , "-TP-" , null ) ;
	}
	@Override
	public Object report( final Context context ) throws LogoException
	{
		return report_1( context , argEvalAgentSet( context , 0 ) ,
						 argEvalDoubleValue( context , 1 ) ) ;
	}
	public AgentSet report_1( final Context context , AgentSet sourceSet , double radius )
		throws LogoException
	{
		if( sourceSet.type() == org.nlogo.agent.Link.class )
		{
			throw new EngineException
				( context , this , "expected a turtle or a patch but got a link" ) ;
		}
		if( radius < 0 )
		{
			throw new EngineException
				( context , this , displayName() + " cannot take a negative radius" ) ;	
		}
		List<Agent> result =
			world.inRadiusOrCone.inRadius( context.agent , sourceSet , radius , false ) ;
		return new org.nlogo.agent.ArrayAgentSet
			( sourceSet.type() ,
			  result.toArray( new Agent[ result.size() ] ) ,
			  world ) ;
	}
}
