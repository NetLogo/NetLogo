package org.nlogo.prim.etc ;

import java.util.LinkedHashSet;
import java.util.Set;

import org.nlogo.api.Dump;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Link;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.nvm.ArgumentTypeException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _linkset
	extends Reporter
{
	@Override
	public Syntax syntax()
	{
		int[] right = { Syntax.TYPE_REPEATABLE | Syntax.TYPE_LINK
						| Syntax.TYPE_LINKSET | Syntax.TYPE_NOBODY 
						| Syntax.TYPE_LIST } ;
		int ret = Syntax.TYPE_LINKSET;
		return Syntax.reporterSyntax( right , ret , 1 , 0 ) ;
	}
	@Override
	public Object report( final Context context )
		throws LogoException
	{
		LinkedHashSet<Link> resultSet = new LinkedHashSet<Link>() ;
		for( int i = 0 ; i < args.length ; i ++ )
		{
			Object elt = args[ i ].report( context ) ;
			if( elt instanceof AgentSet )
			{
				AgentSet tempSet = (AgentSet) elt ;
				if( tempSet.type() != org.nlogo.agent.Link.class )
				{
					throw new ArgumentTypeException
						( context , this , i , Syntax.TYPE_LINK | Syntax.TYPE_LINKSET , elt ) ;
				}
				for( AgentSet.Iterator iter = tempSet.iterator() ; iter.hasNext() ; )
				{
					resultSet.add( (Link) iter.next() ) ;
				}
			}
			else if( elt instanceof LogoList )
			{
				descendList( context , (LogoList) elt , resultSet ) ;
			}
			else if (elt instanceof Link)
			{
				resultSet.add( (Link) elt ) ;
			}
			else if( ! ( elt instanceof org.nlogo.api.Nobody ) )
			{
				throw new ArgumentTypeException
					( context , this , i , Syntax.TYPE_LINK | Syntax.TYPE_LINKSET , elt ) ;
			}
		}
        return new org.nlogo.agent.ArrayAgentSet(
					org.nlogo.agent.Link.class ,
					resultSet.toArray( new org.nlogo.agent.Link[ resultSet.size() ] ) ,
					world ) ;
	}
	private void descendList( Context context , LogoList tempList , Set<Link> result )
		throws LogoException
	{
		for( Object obj : tempList )
		{
			if (obj instanceof Link)
			{
				result.add( (Link) obj ) ;
			}
			else if( obj instanceof AgentSet )
			{
				AgentSet tempSet = (AgentSet) obj ;
				if( tempSet.type() != org.nlogo.agent.Link.class )
				{
					throw new EngineException( context , this , "List inputs to " + this.displayName() 
											   + " must only contain link, link agentset, or list elements.  The list " 
											   +  Dump.logoObject( tempList , true , false) 
											   + " contained a different type agentset: " 
											   + Dump.logoObject( obj , true , false)); 
				}
				for( AgentSet.Iterator iter2 = tempSet.iterator() ;
					 iter2.hasNext() ; )
				{
					result.add( (Link) iter2.next() ) ;
				}
			}
			else if( obj instanceof LogoList )
			{
				descendList( context , (LogoList) obj , result ) ;
			}
			else if( ! ( obj instanceof org.nlogo.api.Nobody ) )
			{
				throw new EngineException( context , this , "List inputs to " + this.displayName() 
										   + " must only contain link, link agentset, or list elements.  The list " 
										   +  Dump.logoObject( tempList , true , false) 
										   + " contained " + Dump.logoObject( obj , true , false) 
										   + " which is NOT a link or link agentset") ;
			}
		}
	}
}
