package org.nlogo.prim.etc ;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Syntax;

public final strictfp class _setcurdir
	extends Command
{
	@Override
	public void perform( final org.nlogo.nvm.Context context ) throws LogoException
	{
		workspace.fileManager().setPrefix( argEvalString( context , 0 ) ) ;
		context.ip = next ;
	}
	@Override
	public Syntax syntax()
	{
		int[] right = { Syntax.TYPE_STRING } ;
		return Syntax.commandSyntax( right ) ;
	}
}
