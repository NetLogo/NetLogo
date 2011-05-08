package org.nlogo.prim.etc ;

import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _fput
	extends Reporter
	implements org.nlogo.nvm.Pure
{
	@Override
	public Syntax syntax()
	{
		int[] right = { Syntax.TYPE_WILDCARD ,
						Syntax.TYPE_LIST } ;
		int ret = Syntax.TYPE_LIST ;
		return Syntax.reporterSyntax( right , ret ) ;
	}
	@Override
	public Object report( final org.nlogo.nvm.Context context ) throws LogoException
	{
		return report_1( context , args[ 0 ].report( context ) , argEvalList( context , 1 ) ) ;
	}
	public LogoList report_1( final org.nlogo.nvm.Context context , Object obj , LogoList list )
	{
		return list.fput( obj ) ;
	}
}
