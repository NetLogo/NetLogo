package org.nlogo.prim.etc ;

import org.nlogo.api.LogoList;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _basecolors
	extends Reporter
{
	private static final LogoList colors = cache() ;
	private static LogoList cache()
	{
		LogoListBuilder result = new LogoListBuilder() ;
		for( int i = 0 ; i < 14 ; i++ )
		{
			result.add( Double.valueOf( i * 10 + 5 ) ) ;
		}
		return result.toLogoList() ;
	}
	@Override
	public Syntax syntax()
	{
		return Syntax.reporterSyntax( Syntax.TYPE_LIST ) ;
	}
	@Override
	public Object report( Context context )
	{
		return colors ;
	}
	public LogoList report_1( Context context )
	{
		return colors ;
	}
}
