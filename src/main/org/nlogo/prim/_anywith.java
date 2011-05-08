package org.nlogo.prim ;

import org.nlogo.agent.Agent;
import org.nlogo.api.Dump;
import org.nlogo.agent.AgentSet;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;

public final strictfp class _anywith
	extends Reporter
{
	@Override public Syntax syntax()
	{
		return Syntax.reporterSyntax
			( new int[] { Syntax.TYPE_AGENTSET , Syntax.TYPE_BOOLEAN_BLOCK } ,
			  Syntax.TYPE_BOOLEAN , "OTPL" , "?" ) ;
	}
	@Override public Object report( Context context ) throws LogoException
	{
		return report_1( context , argEvalAgentSet( context , 0 ) , args[ 1 ] ) ;
	}
	public boolean report_1( Context context , AgentSet sourceSet , Reporter arg1 )
		throws LogoException
	{
		Context freshContext = new Context( context , sourceSet ) ;
		arg1.checkAgentSetClass( sourceSet , context ) ;
		for( AgentSet.Iterator iter = sourceSet.iterator() ; iter.hasNext() ; )
		{
			Agent tester = iter.next() ;
			Object value = freshContext.evaluateReporter( tester , arg1 ) ;
			if( ! ( value instanceof Boolean ) )
			{
				throw new EngineException
					( context , this , "WITH expected a true/false value from " + Dump.logoObject( tester ) + ", " +
					  "but got "+ Dump.logoObject( value ) + " instead" ) ;
			}
			if( ( ( Boolean ) value ).booleanValue() )
			{
				return true ;
			}
		}
		return false ;
	}
}
