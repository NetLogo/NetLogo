package org.nlogo.prim.etc ;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;
import org.nlogo.nvm.Workspace;

public final strictfp class _print
	extends org.nlogo.nvm.Command
{
	@Override
	public void perform( final Context context ) throws LogoException
	{
		workspace.outputObject
			( args[ 0 ].report( context ) ,
			  null , true , false , Workspace.OutputDestination.NORMAL ) ;
		context.ip = next ;
	}
	@Override
	public Syntax syntax()
	{
		int[] right = { Syntax.TYPE_WILDCARD } ;
		return Syntax.commandSyntax( right );
	}
	public void perform_1( Context context , Object o0 ) throws LogoException
	{
		workspace.outputObject
			( o0 , null , true , false , Workspace.OutputDestination.NORMAL ) ;
		context.ip = next ;
	}
}
