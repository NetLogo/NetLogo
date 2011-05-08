package org.nlogo.prim.etc ;

import org.nlogo.agent.Turtle;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;
import org.nlogo.api.Perspective;

public final strictfp class _followme
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
		Turtle turtle = (Turtle) context.agent ;
		world.observer().setPerspective( Perspective.FOLLOW , turtle ) ;
		// the following code is duplicated in _follow and _followme - ST 6/28/05
		int distance = (int) turtle.size() * 5 ;
		world.observer().followDistance
			( StrictMath.max( 1 , StrictMath.min( distance , 100 ) ) ) ;
		context.ip = next ;
	}
}
