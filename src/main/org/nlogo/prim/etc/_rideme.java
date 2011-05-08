package org.nlogo.prim.etc ;

import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;
import org.nlogo.api.Perspective;

public final strictfp class _rideme
	extends Command
{
	@Override
	public Syntax syntax()
	{
		return Syntax.commandSyntax( "-T--" , true ) ;
	}
	@Override
	public void perform( final Context context )
	{
		world.observer().setPerspective( Perspective.RIDE , context.agent ) ;
		world.observer().followDistance( 0 ) ;
		context.ip = next ;
	}
}
