package org.nlogo.prim.hubnet ;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;
import org.nlogo.agent.Agent ;
import org.nlogo.api.Perspective;

public strictfp class _hubnetsendwatch
	extends org.nlogo.nvm.Command
{
	@Override
	public Syntax syntax()
	{
		return Syntax.commandSyntax
			( new int[] 
				{ Syntax.TYPE_STRING , Syntax.TYPE_AGENT } ,
			      "OTPL" , false ) ;
	}
	@Override
	public void perform( final Context context ) throws LogoException
	{
		final String client = argEvalString( context , 0 ) ;
		final Agent agent = argEvalAgent( context , 1 ) ;

		workspace.waitFor
			( new org.nlogo.api.CommandRunnable() {
					public void run() {
						workspace.getHubNetManager().sendAgentPerspective
							( client , 
							  Perspective.WATCH.export() , 
							  agent.getAgentClass() , agent.id , ( (world.worldWidth() - 1) / 2) , false ) ;
					} } ) ;
		context.ip = next ;
	}
}
