package org.nlogo.prim.hubnet ;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;
import org.nlogo.api.Perspective;
import org.nlogo.agent.Agent ;

public strictfp class _hubnetsendfollow
	extends org.nlogo.nvm.Command
{
	@Override
	public Syntax syntax()
	{
		return Syntax.commandSyntax
			( new int[] 
				{ Syntax.TYPE_STRING , Syntax.TYPE_AGENT , Syntax.TYPE_NUMBER } ,
			      "OTPL" , false ) ;
	}
	@Override
	public void perform( final Context context ) throws LogoException
	{
		final String client = argEvalString( context , 0 ) ;
		final Agent agent = argEvalAgent( context , 1 ) ;
		final double radius = argEvalDoubleValue( context , 2 ) ;

		workspace.waitFor
			( new org.nlogo.api.CommandRunnable() {
					public void run() {
						workspace.getHubNetManager().sendAgentPerspective
							( client , 
							  Perspective.FOLLOW.export() , 
							  agent.getAgentClass() , agent.id , radius , false ) ;
					} } ) ;
		context.ip = next ;
	}
}
