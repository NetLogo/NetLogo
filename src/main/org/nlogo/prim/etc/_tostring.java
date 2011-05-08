package org.nlogo.prim.etc ;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Pure;
import org.nlogo.nvm.Syntax;

public final strictfp class _tostring extends Reporter implements Pure
{
	@Override public Syntax syntax()
	{
		return Syntax.reporterSyntax
			( new int[] { Syntax.TYPE_WILDCARD } ,
			  Syntax.TYPE_STRING ) ;
	}
	@Override public Object report( Context context ) throws LogoException
	{
		return report_1( context , args[ 0 ].report( context ) ) ;
	}
	public String report_1( Context context , Object arg0 )
	{
		return arg0.toString() ;
	}
}
