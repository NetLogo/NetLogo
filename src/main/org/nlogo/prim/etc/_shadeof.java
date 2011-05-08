package org.nlogo.prim.etc ;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _shadeof
	extends Reporter
	implements org.nlogo.nvm.Pure
{
	@Override
	public Object report( final org.nlogo.nvm.Context context ) throws LogoException
	{
		double color1 = argEvalDoubleValue( context , 0 ) ;
		double color2 = argEvalDoubleValue( context , 1 ) ;
		color1 = org.nlogo.api.Color.findCentralColorNumber( color1 ) ;
		color2 = org.nlogo.api.Color.findCentralColorNumber( color2 ) ;
		return color1 == color2 ? Boolean.TRUE : Boolean.FALSE ;
	}
	@Override
	public Syntax syntax()
	{
		int[] right = { Syntax.TYPE_NUMBER ,
						Syntax.TYPE_NUMBER } ;
		int ret = Syntax.TYPE_BOOLEAN ;
		return Syntax.reporterSyntax( right , ret ) ;
	}
}
