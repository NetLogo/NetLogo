package org.nlogo.prim.etc ;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;

public final strictfp class _behaviorspacerunnumber
	extends org.nlogo.nvm.Reporter
{
	@Override
	public Syntax syntax()
	{
		return Syntax.reporterSyntax( Syntax.TYPE_NUMBER ) ;
	}
	@Override public Object report( Context context )
	{
		return report_1( context ) ;
	}
	public double report_1( Context context )
	{
		return workspace.behaviorSpaceRunNumber() ;
	}
}
