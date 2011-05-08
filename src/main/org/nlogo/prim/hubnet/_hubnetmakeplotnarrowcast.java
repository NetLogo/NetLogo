package org.nlogo.prim.hubnet ;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;

public final strictfp class _hubnetmakeplotnarrowcast
	extends org.nlogo.nvm.Command
{
	@Override
	public Syntax syntax()
	{
		return Syntax.commandSyntax
			( new int[] { Syntax.TYPE_STRING } ) ;
	}
	@Override
	public void perform( final Context context )
		throws LogoException
	{
		final String name  = argEvalString( context , 0 ) ;

		workspace.waitFor
			( new org.nlogo.api.CommandRunnable() {
				public void run() throws LogoException {
					if( ! workspace.getHubNetManager().addNarrowcastPlot( name ) )
					{
						throw new EngineException
							( context , _hubnetmakeplotnarrowcast.this ,
							  "no such plot: \"" + name + "\"" ) ;
					}
				} } ) ;
		context.ip = next ;
	}
}
