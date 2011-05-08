package org.nlogo.prim.etc ;

import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;

/**
 * reloads all extensions
 **/
public final strictfp class _reloadextensions
	extends Command
{
	@Override
	public Syntax syntax()
	{
		return Syntax.commandSyntax( "OTPL" , true ) ;
	}
	@Override
	public void perform( final Context context )
	{
		 workspace.getExtensionManager().reset() ;
	}
}
