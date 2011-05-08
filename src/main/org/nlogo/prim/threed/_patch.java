package org.nlogo.prim.threed ;

import org.nlogo.agent.World3D;
import org.nlogo.api.LogoException;
import org.nlogo.api.Nobody;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _patch
	extends Reporter
{
	@Override
	public Syntax syntax()
	{
		int[] right ;
		right = new int[] { Syntax.TYPE_NUMBER ,
							Syntax.TYPE_NUMBER ,
							Syntax.TYPE_NUMBER } ;
		return Syntax.reporterSyntax
			( right , Syntax.TYPE_PATCH | Syntax.TYPE_NOBODY ) ;
	}
	@Override
	public Object report( final Context context ) throws LogoException
	{
		try
		{
			return
				( (World3D) world ).getPatchAt
				( argEvalDoubleValue( context , 0 ) ,
				  argEvalDoubleValue( context , 1 ) ,
				  argEvalDoubleValue( context , 2 ) ) ;
		}
		catch( org.nlogo.api.AgentException ex )
		{
			return Nobody.NOBODY ;
		}
	}
}
