package org.nlogo.prim ;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentSet;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _of
	extends Reporter
{
	@Override
	public Syntax syntax()
	{
		return Syntax.reporterSyntax
			( Syntax.TYPE_REPORTER_BLOCK ,  // input on left
			  new int[] { Syntax.TYPE_AGENT | Syntax.TYPE_AGENTSET } , // inputs on right
			  Syntax.TYPE_WILDCARD , // return type
			  Syntax.NORMAL_PRECEDENCE + 1 ,
			  true , // right associative
			  "OTPL" ,
			  "?"    // takes reporter block of unknown agent type
				) ;
	}
	@Override
	public Object report( final Context context ) throws LogoException
	{
		Object agentOrSet = args[ 1 ].report( context ) ;
		if( agentOrSet instanceof Agent )
		{
			Agent agent = (Agent) agentOrSet ;
			if( agent.id == -1 )
			{
                throw new EngineException( context , this , I18N.errors().get("org.nlogo.$common.thatTurtleIsDead")) ;
			}
			args[ 0 ].checkAgentClass( agent , context ) ;
			return new Context( context , agent ).evaluateReporter( agent , args[ 0 ] ) ;
		}
		else if( agentOrSet instanceof AgentSet )
		{
			AgentSet sourceSet = (AgentSet) agentOrSet ;
			LogoListBuilder result = new LogoListBuilder() ;
			Context freshContext = new Context( context , sourceSet ) ;
			args[ 0 ].checkAgentSetClass( sourceSet , context ) ;
			for( AgentSet.Iterator iter = sourceSet.shufflerator( context.job.random ) ;
				 iter.hasNext() ; )
			{
				result.add( freshContext.evaluateReporter( iter.next() , args[ 0 ] ) ) ;
			}
			return result.toLogoList() ;
		}
		else
		{
			throw new org.nlogo.nvm.ArgumentTypeException
				( context , this , 1 ,
				  Syntax.TYPE_AGENTSET | Syntax.TYPE_AGENT ,
				  agentOrSet ) ;
		}
	}
}
