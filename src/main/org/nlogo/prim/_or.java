package org.nlogo.prim ;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _or
	extends Reporter
	implements org.nlogo.nvm.Pure, org.nlogo.nvm.CustomGenerated
{
	@Override
	public Syntax syntax()
	{
		return Syntax.reporterSyntax( Syntax.TYPE_BOOLEAN ,
									  new int[] { Syntax.TYPE_BOOLEAN } ,
									  Syntax.TYPE_BOOLEAN ,
									  Syntax.NORMAL_PRECEDENCE - 6 ) ;
	}
	@Override public Object report( final Context context ) throws LogoException
	{
		return argEvalBooleanValue( context , 0 )
			? Boolean.TRUE
			: argEvalBoolean( context , 1 ) ;
	}
}
