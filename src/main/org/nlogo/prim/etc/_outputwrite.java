package org.nlogo.prim.etc ;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Syntax;
import org.nlogo.nvm.Workspace;

public final strictfp class _outputwrite
	extends org.nlogo.nvm.Command
{
	@Override
	public void perform( final org.nlogo.nvm.Context context ) throws LogoException
	{
		workspace.outputObject
			( args[ 0 ].report( context ) ,
			  null , false , true ,
			  Workspace.OutputDestination.OUTPUT_AREA ) ;
		context.ip = next ;
	}
	@Override
	public Syntax syntax()
	{
		int[] right = { Syntax.TYPE_READABLE } ;
		return Syntax.commandSyntax( right );
	}
}
