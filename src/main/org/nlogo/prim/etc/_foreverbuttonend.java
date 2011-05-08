package org.nlogo.prim.etc ;

import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;

public final strictfp class _foreverbuttonend
	extends Command
{
	@Override
	public Syntax syntax()
	{
		return Syntax.commandSyntax( true ) ;
	}
	@Override
	public void perform( final Context context )
	{
		context.job.buttonTurnIsOver = true ;
		// remember, the stopping flag on jobs is for the user
		// stopping a forever button by clicking it; the stopping
		// flag on contexts is for the forever button stopped
		// because the procedure invoked by the button used the
		// "stop" command to exit
		if( context.job.stopping || context.stopping )
		{
			context.finished = true ;
		}
		else
		{
			context.ip = next ;
		}
	}
}
