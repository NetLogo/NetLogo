package org.nlogo.prim ;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentSet;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _anyother extends Reporter
{
	@Override public Syntax syntax()
	{
		return Syntax.reporterSyntax
			( new int[] { Syntax.TYPE_AGENTSET } ,
			  Syntax.TYPE_BOOLEAN ,
			  "-TPL" ) ;
	}
	@Override public Object report( Context context )
		throws LogoException
	{
		return report_1( context , argEvalAgentSet( context , 0 ) ) ;
	}
	public boolean report_1( Context context , AgentSet sourceSet )
	{
		for( AgentSet.Iterator it = sourceSet.iterator() ; it.hasNext() ; )
		{
			Agent otherAgent = it.next() ;
			if( context.agent != otherAgent )
			{
				return true ;
			}
		}
		return false ;
	}
}
