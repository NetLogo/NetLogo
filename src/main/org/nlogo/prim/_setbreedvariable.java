package org.nlogo.prim ;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;

public final strictfp class _setbreedvariable
	extends Command
{
	private final String name ;
	public _setbreedvariable( _breedvariable original )
	{
		name = original.name ;
	}
	@Override
	public Syntax syntax()
	{
		return Syntax.commandSyntax
			( new int[] { Syntax.TYPE_WILDCARD } ,
			  "-T--" , true ) ;
	}
	@Override
	public String toString()
	{
		return super.toString() + ":" + name ;
	}
	@Override
	public void perform( final Context context )
		throws LogoException
	{
		Object value = args[ 0 ].report( context ) ;
		try
		{
			context.agent.setBreedVariable( name , value ) ;
		}
		catch( org.nlogo.api.AgentException ex )
		{
			throw new EngineException
				( context , this , ex.getMessage() ) ;
		}
		context.ip = next ;
	}
}
