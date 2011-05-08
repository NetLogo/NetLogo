package org.nlogo.prim.etc ;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Link;
import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;

public final strictfp class _layoutspring
	extends Command
{
	@Override
	public Syntax syntax()
	{
		return Syntax.commandSyntax
			( new int[] { Syntax.TYPE_TURTLESET , Syntax.TYPE_LINKSET ,
						  Syntax.TYPE_NUMBER , Syntax.TYPE_NUMBER , Syntax.TYPE_NUMBER } ,
				true ) ;
	}
	@Override
	public void perform( final Context context )
		throws LogoException
	{
		AgentSet nodeset = argEvalAgentSet( context , 0 , Turtle.class ) ;
		AgentSet linkset = argEvalAgentSet( context , 1 , Link.class ) ;
		double springConstant = argEvalDoubleValue( context , 2 ) ;
		double springLength = argEvalDoubleValue( context , 3 ) ;
		double repulsionConstant = argEvalDoubleValue( context , 4 ) ;
		org.nlogo.agent.Layouts.spring
			( nodeset , linkset , springConstant , springLength , repulsionConstant , 
			  context.job.random ) ;
		context.ip = next ;
	}
}
