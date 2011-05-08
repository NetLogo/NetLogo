package org.nlogo.prim.etc ;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _lput
	extends Reporter
	implements org.nlogo.nvm.Pure
{
	@Override
	public Object report( final org.nlogo.nvm.Context context ) throws LogoException
	{
		Object obj = args[ 0 ].report( context ) ;
		return argEvalList( context , 1 ).lput( obj ) ;
	}
	@Override
	public Syntax syntax()
	{
		int[] right = { Syntax.TYPE_WILDCARD ,
						Syntax.TYPE_LIST } ;
		int ret = Syntax.TYPE_LIST ;
		return Syntax.reporterSyntax( right , ret ) ;
	}
}
