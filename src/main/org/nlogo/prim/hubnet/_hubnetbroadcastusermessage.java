package org.nlogo.prim.hubnet ;

import org.nlogo.api.Dump;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Syntax;

public final strictfp class _hubnetbroadcastusermessage
	extends org.nlogo.nvm.Command
{
	@Override
	public void perform( final org.nlogo.nvm.Context context ) throws LogoException
	{
		Object data = args[ 0 ].report( context ) ;
		workspace.getHubNetManager().broadcastUserMessage( Dump.logoObject( data ) ) ; 
		context.ip = next ;
	}
	@Override
	public Syntax syntax()
	{
		int[] right = { Syntax.TYPE_WILDCARD } ;
		return Syntax.commandSyntax( right ) ;
	}
}
