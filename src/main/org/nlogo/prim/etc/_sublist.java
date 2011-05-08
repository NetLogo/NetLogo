package org.nlogo.prim.etc ;

import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _sublist
	extends Reporter
	implements org.nlogo.nvm.Pure
{
	@Override
	public Object report( final org.nlogo.nvm.Context context ) throws LogoException
	{
		LogoList list = argEvalList( context , 0 ) ; 
		int start = argEvalIntValue( context , 1 ) ;
		int stop = argEvalIntValue( context , 2 ) ;
		int size = list.size() ;
		if( start < 0 )
		{
			throw new EngineException
				( context , this  , start + " is less than zero" ) ;
		}
		else if( stop < start )
		{
			throw new EngineException
				( context , this  , stop + " is less than " + start ) ;
		}
		else if( stop > size )
		{
			throw new EngineException
				( context , this  , stop + " is greater than the length of the input list (" +
				  size + ")" ) ;
		}
		return list.logoSublist( start , stop ) ;
	}
	@Override
	public Syntax syntax()
	{
		return Syntax.reporterSyntax
			( new int[] { Syntax.TYPE_LIST , Syntax.TYPE_NUMBER , Syntax.TYPE_NUMBER } ,
			  Syntax.TYPE_LIST ) ;
	}
}
