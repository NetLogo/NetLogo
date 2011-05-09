package org.nlogo.prim.etc;

import org.nlogo.agent.Turtle;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;
import org.nlogo.api.Perspective;

public final strictfp class _follow
	extends Command
{
	@Override
	public Syntax syntax()
	{
		return Syntax.commandSyntax
			( new int[] { Syntax.TYPE_TURTLE } ,
			  "O---" , true ) ;
	}
	@Override
	public void perform( final Context context )
		throws LogoException
	{
		Turtle turtle = argEvalTurtle( context , 0 ) ;
		if( turtle.id == -1 )
		{
			throw new EngineException
				( context , this , I18N.errors().get("org.nlogo.$common.thatTurtleIsDead")) ;
		}
		world.observer().setPerspective( Perspective.FOLLOW , turtle ) ;
		// the following code is duplicated in _follow and _followme - ST 6/28/05
		int distance = (int) turtle.size() * 5 ;
		world.observer()
			.followDistance
			( StrictMath.max( 1 , StrictMath.min( distance , 100 ) ) ) ;
		context.ip = next ;
	}
}
