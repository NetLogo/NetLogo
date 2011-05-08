package org.nlogo.prim.etc ;

import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

// only display the mainRNG state, the auxillary shouldn't matter
// since it doesn't affect the outcome of the model.

public final strictfp class _randomstate
	extends Reporter
{
	@Override
	public Object report( final org.nlogo.nvm.Context context )
	{
		return world.mainRNG.save() ;
	}
	@Override
	public Syntax syntax()
	{
		return Syntax.reporterSyntax( Syntax.TYPE_STRING ) ;
	}
}
