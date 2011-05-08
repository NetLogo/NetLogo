package org.nlogo.prim.hubnet ;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Syntax;

public final strictfp class _hubnetreset
	extends org.nlogo.nvm.Command
{
	@Override
	public Syntax syntax()
	{
		return Syntax.commandSyntax( "O---" , false ) ;
	}
	@Override
	public void perform( final org.nlogo.nvm.Context context ) throws LogoException
	{
		workspace.waitFor
			( new org.nlogo.api.CommandRunnable() {
					public void run() throws LogoException {
						workspace.getHubNetManager().reset() ;
					} } ) ;
		context.ip = next ;
	}
}
