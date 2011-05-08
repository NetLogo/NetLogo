package org.nlogo.prim.threed;

import org.nlogo.agent.Turtle3D;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;

public final strictfp class _rollright
	extends Command
{
	@Override
	public Syntax syntax()
	{
		return Syntax.commandSyntax
			( new int[] { Syntax.TYPE_NUMBER } ,
			  "-T--" , true ) ;
	}
	@Override
	public void perform( final Context context )
		throws LogoException
	{
		double delta = argEvalDoubleValue( context , 0 ) ;
		Turtle3D turtle = (Turtle3D) context.agent ;
		turtle.roll( turtle.roll() + delta ) ;
		context.ip = next ;
	}
}
