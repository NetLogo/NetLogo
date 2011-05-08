package org.nlogo.prim.etc ;

import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _rgb
	extends Reporter
{
	@Override
	public Syntax syntax()
	{
		int[] right = { Syntax.TYPE_NUMBER , Syntax.TYPE_NUMBER , Syntax.TYPE_NUMBER } ;
		int ret = Syntax.TYPE_LIST ;
		return Syntax.reporterSyntax( right , ret ) ;
	}
	@Override
	public Object report( final org.nlogo.nvm.Context context ) throws LogoException
	{		
		return report_1( context , 
						 argEvalDoubleValue( context , 0 ) ,
						 argEvalDoubleValue( context , 1 ) ,
						 argEvalDoubleValue( context , 2 ) ) ;
	}
	public LogoList report_1( final Context context , double r , double g , double b )
	{
		LogoListBuilder rgbList = new LogoListBuilder() ;
		rgbList.add( Double.valueOf ( StrictMath.max( 0 , StrictMath.min( 255 , r ) ) ) ) ;
		rgbList.add( Double.valueOf ( StrictMath.max( 0 , StrictMath.min( 255 , g ) ) ) ) ;
		rgbList.add( Double.valueOf ( StrictMath.max( 0 , StrictMath.min( 255 , b ) ) ) ) ;
		return rgbList.toLogoList() ;
	}
}
