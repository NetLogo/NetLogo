package org.nlogo.prim.etc ;

import java.util.Iterator;

import org.nlogo.agent.AgentSet;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _nof
	extends Reporter
{
	@Override
	public Object report( final Context context )
		throws LogoException
	{
		int n = argEvalIntValue( context , 0 ) ;
		if( n < 0 )
		{
			throw new EngineException
				( context , this , I18N.errors().getNJava("org.nlogo.prim.etc.$common.firstInputCantBeNegative",
                        new String [] {displayName()}));
		}
		Object obj = args[ 1 ].report( context ) ;
		if( obj instanceof LogoList )
		{
			LogoList list = (LogoList) obj ;
			if( n > list.size() )
			{
				throw new EngineException
					(context , this , I18N.errors().getNJava("org.nlogo.prim.etc.$common.requestMoreItemsThanInList",
                            new String [] {new Integer(n).toString(),new Integer(list.size()).toString()}));
			}
			if( n == list.size() )
			{
				return list ;
			}
			return randomSubset( list , n , context.job.random ) ;
		}
		else if( obj instanceof AgentSet )
		{
			AgentSet agents = (AgentSet) obj ;
			// only call count() once, since it's expensive
			// on some turtlesets - ST 11/5/03
			int count = agents.count() ;
			if( n > count )
			{
				throw new EngineException
					(context , this , I18N.errors().getNJava("org.nlogo.prim.etc._nof.requestMoreItemsThanInList",
                            new String [] {new Integer(n).toString(),new Integer(count).toString()}));
			}
			return agents.randomSubset( n , count , context.job.random ) ;
		}
		else
		{
			throw new ArgumentTypeException
				( context , this , 1 , Syntax.TYPE_LIST | Syntax.TYPE_AGENTSET , obj ) ;
		}
	}
	private LogoList randomSubset( LogoList list , int n , 
								   org.nlogo.util.MersenneTwisterFast random )
	{
		int size = list.size() ;
		LogoListBuilder result = new LogoListBuilder() ;
		int i = 0 ;
		int j = 0 ;
		for( Iterator<Object> it = list.iterator() ;
			 it.hasNext() && j < n ;
			 i++ )
		{
			Object elt = it.next() ;
			if( random.nextInt( size - i ) < n - j )
			{
				result.add( elt ) ;
				j++ ;
			}
		}
		return result.toLogoList() ;
	}
	@Override
	public Syntax syntax()
	{
		return Syntax.reporterSyntax
			( new int[] { Syntax.TYPE_NUMBER,
						  Syntax.TYPE_AGENTSET | Syntax.TYPE_LIST } ,
			  Syntax.TYPE_AGENTSET | Syntax.TYPE_LIST ) ;
	}
}
