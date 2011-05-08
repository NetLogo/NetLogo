package org.nlogo.prim.threed;

import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _neighbors6
	extends Reporter
{
	@Override
	public Syntax syntax()
	{
		return Syntax.reporterSyntax
			( Syntax.TYPE_PATCHSET , "-TP-" ) ;
	}
	@Override
	public Object report( final org.nlogo.nvm.Context context )
	{
		org.nlogo.agent.Patch3D patch ;
		if( context.agent instanceof org.nlogo.agent.Turtle )
		{
			patch = (org.nlogo.agent.Patch3D)( (org.nlogo.agent.Turtle) context.agent ).getPatchHere() ;
		}
		else
		{
			patch = (org.nlogo.agent.Patch3D) context.agent ;
		}
		return patch.getNeighbors6() ;
	}
}
