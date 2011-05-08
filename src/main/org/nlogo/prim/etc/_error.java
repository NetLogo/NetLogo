package org.nlogo.prim.etc ;

import org.nlogo.api.Dump;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;

public final strictfp class _error
	extends Command
{
	@Override
	public Syntax syntax()
	{
		return Syntax.commandSyntax(
			new int[] { Syntax.TYPE_WILDCARD } ) ;
	}
	@Override
	public void perform( Context context ) throws LogoException
	{
		throw new EngineException
			( context , this ,
			  Dump.logoObject( args[ 0 ].report( context ) ) ) ;
	}
}
