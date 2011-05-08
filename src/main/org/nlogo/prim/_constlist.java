package org.nlogo.prim ;

import org.nlogo.api.Dump;
import org.nlogo.api.LogoList;
import org.nlogo.nvm.Pure;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _constlist
	extends Reporter implements Pure
{
	final LogoList value;
	public _constlist( LogoList value )
	{
		this.value = value ;
	}
	@Override
	public Syntax syntax()
	{
		return Syntax.reporterSyntax( Syntax.TYPE_LIST ) ;
	}
	@Override
	public Object report( final org.nlogo.nvm.Context context )
	{
		return value ;
	}
	@Override
	public String toString()
	{
		return super.toString() + ":" + Dump.logoObject( value ) ;
	}
	public LogoList report_1( final org.nlogo.nvm.Context context )
	{
		return value ;
	}
}
