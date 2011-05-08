package org.nlogo.prim.etc ;

import org.nlogo.nvm.Command;
import org.nlogo.nvm.Syntax;

public final strictfp class _nodisplay
	extends Command
{
	@Override
	public void perform( final org.nlogo.nvm.Context context )
	{
		world.displayOn( false ) ;
		context.ip = next ;
	}
	@Override
	public Syntax syntax()
	{
		int[] right = {} ;
		return Syntax.commandSyntax( right ) ;
	}
}
