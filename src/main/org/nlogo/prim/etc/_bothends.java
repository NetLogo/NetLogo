package org.nlogo.prim.etc ;

import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Link;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _bothends
	extends Reporter
{
	@Override
	public Syntax syntax()
	{
		return Syntax.reporterSyntax
			( Syntax.TYPE_AGENTSET , "---L" ) ;
	}
	@Override
	public Object report( final Context context )
	{
		return report_1( context ) ;
	}
	public AgentSet report_1( final Context context )
	{
		return ( (Link) context.agent ).bothEnds() ;
	}
}
