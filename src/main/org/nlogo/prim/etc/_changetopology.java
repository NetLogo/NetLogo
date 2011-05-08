package org.nlogo.prim.etc ;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;

public final strictfp class _changetopology
	extends Command
{
	@Override
	public Syntax syntax()
	{
		return Syntax.commandSyntax
			( new int[] { Syntax.TYPE_BOOLEAN ,
						  Syntax.TYPE_BOOLEAN } ) ;
	}
	@Override
	public void perform( final Context context )
		throws LogoException
	{
		workspace.changeTopology
			( argEvalBooleanValue( context , 0 ) ,
			  argEvalBooleanValue( context , 1 ) ) ;		
		context.ip = next ;
	}
}
