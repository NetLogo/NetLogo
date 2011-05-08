package org.nlogo.prim.threed ;

import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _oheading
	extends Reporter
{
	@Override
	public Syntax syntax()
	{
		int[] right = {} ;
		int ret = Syntax.TYPE_NUMBER;
		return Syntax.reporterSyntax( right , ret , "O---" ) ;
	}
	@Override
	public Object report ( final Context context )
	{
		return Double.valueOf
			( world.observer().heading() ); 
	}
}
