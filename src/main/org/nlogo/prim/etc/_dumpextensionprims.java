package org.nlogo.prim.etc ;

import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

/**
 * Returns a string representation of the contents of the stack
 **/
public final strictfp class _dumpextensionprims
	extends Reporter
{
	@Override
	public Object report( final org.nlogo.nvm.Context context )
	{
		return workspace.getExtensionManager().dumpExtensionPrimitives();
	}
	@Override
	public Syntax syntax()
	{
		return Syntax.reporterSyntax( Syntax.TYPE_STRING ) ;
	}
}
