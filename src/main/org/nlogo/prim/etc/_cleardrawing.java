package org.nlogo.prim.etc ;

import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;

public final strictfp class _cleardrawing
	extends Command
{
	@Override
	public Syntax syntax()
	{
		return Syntax.commandSyntax( "O---" , true ) ;
	}
	@Override
	public void perform( final Context context )
	{
		workspace.clearDrawing();
		context.ip = next ;
	}
}
