package org.nlogo.prim.etc ;

import org.nlogo.api.Dump;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Pure;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;

public final strictfp class _min extends Reporter implements Pure
{
	@Override public Syntax syntax()
	{
		return Syntax.reporterSyntax
			( new int[] { Syntax.TYPE_LIST } ,
			  Syntax.TYPE_NUMBER ) ;
	}
	@Override public Object report( Context context ) throws LogoException
	{
		return report_1( context , argEvalList( context , 0 ) ) ;
	}
	public double report_1( Context context , LogoList list ) throws LogoException
	{
		double winner = 0 ;
		Double boxedWinner = null ;
		for( Object elt : list )
		{
			if( elt instanceof Double )
			{
				Double boxedValue = (Double) elt ;
				double value = boxedValue.doubleValue() ;
				if( boxedWinner == null || value < winner )
				{
					winner = value ;
					boxedWinner = boxedValue ;
				}
			}
		}
		if( boxedWinner == null )
		{
            throw new EngineException
                ( context , this ,
                        I18N.errors().getNJava("org.nlogo.prim._min.cantFindMinOfListWithNoNumbers",
                                new String [] {Dump.logoObject( list )})) ;
		}
		return boxedWinner ;
	}
}
