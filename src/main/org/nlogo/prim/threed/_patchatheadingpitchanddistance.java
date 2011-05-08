package org.nlogo.prim.threed;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _patchatheadingpitchanddistance
	extends Reporter
{
	@Override
	public Syntax syntax()
	{
		return Syntax.reporterSyntax
			( new int[] { Syntax.TYPE_NUMBER , Syntax.TYPE_NUMBER , Syntax.TYPE_NUMBER } ,
			  Syntax.TYPE_PATCH , "-TP-" ) ;
	}
	@Override
	public Object report( final org.nlogo.nvm.Context context )
		throws LogoException
	{
		try
		{
			if( context.agent instanceof org.nlogo.agent.Turtle )
			{
				org.nlogo.agent.Turtle3D turtle =
					(org.nlogo.agent.Turtle3D) context.agent ;
				return ((org.nlogo.agent.Protractor3D)world.protractor()).getPatchAtHeadingPitchAndDistance
					( turtle.xcor() , turtle.ycor() , turtle.zcor() ,
					  argEvalDoubleValue( context , 0 ) ,
					  argEvalDoubleValue( context , 1 ) ,
					  argEvalDoubleValue( context , 2 ) ) ;
			}
			else
			{
				org.nlogo.agent.Patch3D patch =
					(org.nlogo.agent.Patch3D) context.agent ;
				return ((org.nlogo.agent.Protractor3D)world.protractor()).getPatchAtHeadingPitchAndDistance
					( patch.pxcor , patch.pycor , patch.pzcor ,
					  argEvalDoubleValue( context , 0 ) ,
					  argEvalDoubleValue( context , 1 ) ,
					  argEvalDoubleValue( context , 2 ) ) ;
			}
		}
		catch( org.nlogo.api.AgentException exc )
		{
			return org.nlogo.api.Nobody.NOBODY ;
		}
	}
}
