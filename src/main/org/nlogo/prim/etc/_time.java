package org.nlogo.prim.etc;

import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _time
	extends Reporter
{
	@Override
	public Syntax syntax()
	{
		return Syntax.reporterSyntax( Syntax.TYPE_STRING ) ;
	}
	@Override
	public Object report( final org.nlogo.nvm.Context context )
	{
		return report_1( context ) ;
	}
	public String report_1( final org.nlogo.nvm.Context context )
	{
		return new java.text.SimpleDateFormat( "hh:mm:ss.SSS a dd-MMM-yyyy" )
			.format( new java.util.Date() ) ;
	}
}
