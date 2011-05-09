package org.nlogo.prim.etc ;

import java.util.List;

import org.nlogo.agent.Agent;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.api.I18N;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _incone
	extends Reporter
{
	@Override
	public Syntax syntax()
	{
		return Syntax.reporterSyntax
			( Syntax.TYPE_TURTLESET | Syntax.TYPE_PATCHSET ,          // left
			  new int[] { Syntax.TYPE_NUMBER , Syntax.TYPE_NUMBER } , // right
			  Syntax.TYPE_TURTLESET | Syntax.TYPE_PATCHSET ,          // return
			  Syntax.NORMAL_PRECEDENCE + 2 , false , "OTPL" , "-T--" ) ;
	}
	@Override
	public Object report( final Context context )
		throws LogoException
	{
		return report_1( context , argEvalAgentSet( context , 0 ) ,
						 argEvalDoubleValue( context , 1 ) ,
						 argEvalDoubleValue( context , 2 ) ) ;
	}
	public AgentSet report_1( final Context context , AgentSet sourceSet ,
							  double radius , double angle ) 
		throws LogoException
	{
		if( sourceSet.type() == org.nlogo.agent.Link.class )
		{
			throw new EngineException
				( context , this , "" ) ;
		}
		if( radius < 0 )
		{
			throw new EngineException
				( context , this ,
            I18N.errors().getNJava("org.nlogo.prim.etc.$common.noNegativeRadius", new String [] {displayName()} ));
		}
		if( angle < 0 )
		{
			throw new EngineException
				( context , this ,
            I18N.errors().getNJava("org.nlogo.prim.etc.$common.noNegativeAngle", new String [] {displayName()})) ;
		}
		if( angle > 360 )
		{
			throw new EngineException
				( context , this ,
            I18N.errors().getNJava("org.nlogo.prim.etc.$common.noAngleGreaterThan360", new String [] {displayName()}) ) ;

		}
		List<Agent> result = 
			world.inRadiusOrCone.inCone( (Turtle) context.agent , sourceSet , radius , angle , true ) ;
		return new org.nlogo.agent.ArrayAgentSet
			( sourceSet.type() ,
			  result.toArray( new Agent[ result.size() ] ) ,
			  world ) ;
	}
}
