package org.nlogo.prim.threed;

import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _worlddepth extends Reporter
{
	@Override public Syntax syntax()
	{
		return Syntax.reporterSyntax( Syntax.TYPE_NUMBER ) ;
	}
	@Override public Object report( Context context ) 
	{
		return report_1( context ) ;
	}
	public double report_1( Context context ) 
	{
		return ( (org.nlogo.agent.World3D) world ).worldDepth() ;
	}
}
