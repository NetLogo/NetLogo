package org.nlogo.prim.etc;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;
import org.nlogo.api.Perspective;

public final strictfp class _watch
	extends Command
{
	@Override
	public Syntax syntax()
	{
		return Syntax.commandSyntax
			( new int[] { Syntax.TYPE_AGENT } ,
			  "O---", true ) ;
	}
	@Override
	public void perform( final org.nlogo.nvm.Context context ) throws LogoException
	{
		org.nlogo.agent.Agent agent = argEvalAgent( context , 0 ) ;
		if( agent.id == -1 )
		{
			throw new EngineException( context , this , "that turtle is dead" ) ;
		}
		world.observer().home() ;
		world.observer().setPerspective( Perspective.WATCH , agent ) ;
		context.ip = next ;
	}
}
