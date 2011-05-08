package org.nlogo.prim.gui ;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;
import org.nlogo.window.GUIWorkspace;

public final strictfp class _reload extends Command
{
	@Override public Syntax syntax()
	{
		return Syntax.commandSyntax( "O---" , true ) ;
	}
	@Override public void perform( Context context ) throws LogoException
	{
		if( ! ( workspace instanceof GUIWorkspace ) )
		{
			throw new EngineException(
				context , this , token().name() + " can only be used in the GUI" ) ;
		}
		( (GUIWorkspace) workspace ).reload() ;
		context.ip = next ;
	}
}
