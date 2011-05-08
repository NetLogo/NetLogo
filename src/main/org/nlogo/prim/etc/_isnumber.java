package org.nlogo.prim.etc ;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _isnumber
	extends Reporter
	implements org.nlogo.nvm.Pure
{
	@Override
	public Object report( final Context context ) throws LogoException
	{
		return ( args[ 0 ].report( context ) instanceof Double )
			? Boolean.TRUE
			: Boolean.FALSE ;
	}
	@Override
	public Syntax syntax()
	{
		return Syntax.reporterSyntax
			( new int[] { Syntax.TYPE_WILDCARD } ,
			  Syntax.TYPE_BOOLEAN ) ;
	}
}




