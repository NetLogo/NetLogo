package org.nlogo.prim ;

import org.nlogo.nvm.Context;
import org.nlogo.api.Let;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _letvariable
	extends Reporter
{
	public final Let let ;
	final String name ;
	public _letvariable( Let let , String name )
	{
		this.let = let ;
		this.name = name ;
	}
	@Override public Syntax syntax()
	{
		return Syntax.reporterSyntax( Syntax.TYPE_WILDCARD ) ;
	}
	@Override public String toString()
	{
		return super.toString() + "(" + name + ")";
	}
	@Override public Object report( Context context )
	{
		return report_1( context ) ;
	}
	public Object report_1( Context context )
	{
		return context.getLet( let ) ;
	}
}
