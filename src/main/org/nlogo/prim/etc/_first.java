package org.nlogo.prim.etc ;

import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _first
	extends Reporter
	implements org.nlogo.nvm.Pure
{
	@Override
	public Syntax syntax()
	{
		int[] right = { Syntax.TYPE_LIST | Syntax.TYPE_STRING} ;
		int ret = Syntax.TYPE_WILDCARD ;
		return Syntax.reporterSyntax( right , ret ) ;
	}
	@Override
	public Object report( final org.nlogo.nvm.Context context ) throws LogoException
	{
		return report_1( context , args[ 0 ].report( context ) ) ;
	}
	public Object report_1( final org.nlogo.nvm.Context context , Object obj ) 
		throws LogoException
	{
		if( obj instanceof LogoList )
		{
			LogoList list = (LogoList) obj ;
			if( list.isEmpty() )
			{
				throw new EngineException( context , this , "list is empty" ) ;
			}
			return list.first() ;
		}
		else if ( obj instanceof String )
		{
			String string = (String) obj ;
			if( string.length() == 0 )
			{
				throw new EngineException( context , this , "string is empty" ) ;
			}
			return string.substring( 0 , 1 ) ;
		}
		else
		{
			throw new ArgumentTypeException
				( context , this , 0 , Syntax.TYPE_LIST | Syntax.TYPE_STRING , obj ) ;
		}
	}
	public Object report_2( final org.nlogo.nvm.Context context , LogoList list ) 
		throws LogoException
	{
		if( list.isEmpty() )
		{
			throw new EngineException( context , this , "list is empty" ) ;
		}
		return list.first() ;
	}
	public Object report_3( final org.nlogo.nvm.Context context , String string ) 
		throws LogoException
	{
		if( string.length() == 0 )
		{
			throw new EngineException( context , this , "string is empty" ) ;
		}
		return string.substring( 0 , 1 ) ;
	}
}




