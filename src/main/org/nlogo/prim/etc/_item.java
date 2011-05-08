package org.nlogo.prim.etc ;

import org.nlogo.api.Dump;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _item
	extends Reporter
	implements org.nlogo.nvm.Pure
{
	@Override
	public Object report( final org.nlogo.nvm.Context context ) throws LogoException
	{
		int index = argEvalIntValue( context , 0 ) ;
		Object obj = args[ 1 ].report( context ) ;
		if( index < 0 )
		{
			throw new EngineException
				( context , this , index + " isn't greater than or equal to zero " ) ;
		}
		if ( obj instanceof LogoList )
		{
			LogoList list = (LogoList) obj ;
			if( index >= list.size() )
			{
				throw new EngineException
					( context , this , "can't find element " + index + " of the list " +
					  Dump.logoObject( list ) + ", which is only of length " + list.size() ) ;
			}
			return list.get( index ) ;
		}
		else if ( obj instanceof String )
		{
			String string = ( String ) obj ;
			if( index >= string.length() )
			{
				throw new EngineException
					( context , this , "can't find element " + index + " of the string " +
					  Dump.logoObject( string ) + ", which is only of length " + string.length() ) ;
			}
			return string.substring( index, index + 1 ) ;
		}
		
		else 
		{
			throw new ArgumentTypeException
				( context , this , 1 , Syntax.TYPE_LIST | Syntax.TYPE_STRING , obj ) ;
		}
	}
	@Override
	public Syntax syntax()
	{
		int[] right = { Syntax.TYPE_NUMBER ,
						Syntax.TYPE_LIST | Syntax.TYPE_STRING } ;
		int ret = Syntax.TYPE_WILDCARD ;
		return Syntax.reporterSyntax( right , ret ) ;
	}
}
