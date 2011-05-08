package org.nlogo.prim;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentSet;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _linkbreedvariableof
	extends Reporter
{
	public String name ;
	public _linkbreedvariableof( String name )
	{
		this.name = name ;
	}
	@Override
	public Object report( final org.nlogo.nvm.Context context ) throws LogoException
	{
		Object agentOrSet = args[ 0 ].report( context ) ;
		if( agentOrSet instanceof Agent )
		{
			Agent agent = (Agent) agentOrSet ;
			if( agent.id == -1 )
			{
				throw new EngineException
					( context , this , "that link is dead" ) ;
			}
			try
			{
				return agent.getLinkBreedVariable( name ) ;
			}
			catch( org.nlogo.api.AgentException ex )
			{
				throw new EngineException( context , this , ex.getMessage() ) ;
			}
		}
		else if( agentOrSet instanceof AgentSet )
		{
			AgentSet sourceSet = (AgentSet) agentOrSet ;
			LogoListBuilder result = new LogoListBuilder() ;
			try
			{
				for( AgentSet.Iterator iter = sourceSet.shufflerator( context.job.random ) ;
					 iter.hasNext() ; )
				{
					result.add( iter.next().getLinkBreedVariable( name ) ) ;
				}
			}
			catch( org.nlogo.api.AgentException ex )
			{
				throw new EngineException( context , this , ex.getMessage() ) ;
			}
			return result.toLogoList() ;
		}
		else
		{
			throw new org.nlogo.nvm.ArgumentTypeException
				( context , this , 0 ,
				  Syntax.TYPE_LINKSET | Syntax.TYPE_LINK ,
				  agentOrSet ) ;
		}
	}
	@Override
	public String toString()
	{
		return super.toString() + ":" + name ;
	}
	@Override
	public Syntax syntax()
	{
		int[] right = { Syntax.TYPE_LINK | Syntax.TYPE_LINKSET } ;
		int ret = Syntax.TYPE_WILDCARD ;
		return Syntax.reporterSyntax( right , ret ) ;
	}
}
