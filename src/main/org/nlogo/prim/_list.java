package org.nlogo.prim ;

import org.nlogo.api.LogoException;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _list
	extends Reporter
	implements org.nlogo.nvm.Pure, org.nlogo.nvm.CustomGenerated
{
	@Override
	public Syntax syntax()
	{
		int[] right = { Syntax.TYPE_REPEATABLE | Syntax.TYPE_WILDCARD } ;
		int ret = Syntax.TYPE_LIST ;
		return Syntax.reporterSyntax( right , ret , 2 , 0 ) ;
	}
	@Override
	public Object report ( final Context context ) throws LogoException
	{
		LogoListBuilder list = new LogoListBuilder() ;
		for( int i = 0 ; i < args.length ; i ++ ) 
		{
			list.add( args[ i ].report( context ) ) ;
		}
		return list.toLogoList() ;
	}
}
