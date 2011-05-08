package org.nlogo.prim.etc ;

import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _nopatches
	extends Reporter
{
	@Override
	public Object report( final Context context )
	{
		return world.noPatches() ;
	}
	@Override
	public Syntax syntax()
	{
		return Syntax.reporterSyntax( Syntax.TYPE_PATCHSET ) ;
	}
}
