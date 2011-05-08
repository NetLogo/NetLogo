package org.nlogo.prim.hubnet ;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;

public strictfp class _hubnetresetperspective
	extends org.nlogo.nvm.Command
{
	@Override
	public Syntax syntax()
	{
		return Syntax.commandSyntax
			( new int[] 
				{ Syntax.TYPE_STRING } ,
			      "OTPL" , false ) ;
	}
	@Override
	public void perform( final Context context ) throws LogoException
	{
		final String client = argEvalString( context , 0 ) ;
		org.nlogo.api.Agent agent = world.observer().targetAgent() ;
		final Class<? extends org.nlogo.api.Agent> agentClass 
			= (agent != null ? agent.getClass() : org.nlogo.agent.Observer.class) ;
		final long id = (agent != null ? agent.id() : 0) ;

		workspace.waitFor
			( new org.nlogo.api.CommandRunnable() {
					public void run() {
						workspace.getHubNetManager().sendAgentPerspective
							( client , world.observer().perspective().export() ,
							  agentClass , id , ((world.worldWidth() - 1) / 2) , true ) ;
					} } ) ;
		context.ip = next ;
	}
}
