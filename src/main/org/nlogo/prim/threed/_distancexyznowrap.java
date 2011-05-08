package org.nlogo.prim.threed;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _distancexyznowrap
	extends Reporter
{
	@Override
	public Syntax syntax()
	{
		int[] right = { Syntax.TYPE_NUMBER , 
						Syntax.TYPE_NUMBER ,
						Syntax.TYPE_NUMBER } ;
		int ret = Syntax.TYPE_NUMBER ;
		return Syntax.reporterSyntax( right , ret , "-TP-" ) ;
	}
	@Override
	public Object report( final Context context )
		throws LogoException
	{
		return newValidDouble
			( ((org.nlogo.agent.Protractor3D)world.protractor()).distance
			  ( context.agent , 
				argEvalDoubleValue( context , 0 ) ,
				argEvalDoubleValue( context , 1 ) ,
				argEvalDoubleValue( context , 2 ) ,
				false ) ) ; // false = don't wrap
	}
}
