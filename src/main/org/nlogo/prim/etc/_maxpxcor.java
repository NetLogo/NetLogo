package org.nlogo.prim.etc;

import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _maxpxcor extends Reporter
{
	@Override public Syntax syntax()
	{
		return Syntax.reporterSyntax( Syntax.TYPE_NUMBER ) ;
	}
	@Override public Object report( Context context ) 
	{
		return report_1( context ) ;
	}
	public Double report_1( Context context )
	{
		return world.maxPxcorBoxed() ;
	}
	public double report_2( Context context )
	{
		return world.maxPxcor() ;
	}
}
