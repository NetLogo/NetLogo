package org.nlogo.prim.gui ;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;
import org.nlogo.window.GUIWorkspace;

public final strictfp class _mousexcor extends Reporter
{
	@Override public Syntax syntax()
	{
		return Syntax.reporterSyntax( Syntax.TYPE_NUMBER ) ;
	}
	@Override public Object report( Context context ) throws LogoException
	{
		return report_1( context ) ;
	}
	public double report_1( Context context ) throws LogoException
	{
		if( workspace instanceof GUIWorkspace )
		{
			return ((GUIWorkspace) workspace).mouseXCor() ;
		}
		return 0 ;
	}
}
