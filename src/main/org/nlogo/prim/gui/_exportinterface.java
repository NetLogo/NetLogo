package org.nlogo.prim.gui ;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;
import org.nlogo.window.GUIWorkspace;

public final strictfp class _exportinterface
	extends org.nlogo.nvm.Command
{
	@Override
	public Syntax syntax()
	{
		int[] right = { Syntax.TYPE_STRING } ;
		return Syntax.commandSyntax( right ) ;
	}
	@Override
	public void perform( final org.nlogo.nvm.Context context )
		throws LogoException
	{
		if( ! ( workspace instanceof GUIWorkspace ) )
		{
			throw new EngineException(
				context , this , token().name() + " can only be used in the GUI" ) ;
		}
		( (GUIWorkspace) workspace).updateUI() ;
		final String filePath = argEvalString( context , 0 ) ;
		workspace.waitFor
			( new org.nlogo.api.CommandRunnable() {
					public void run() throws LogoException {
						try
						{
							workspace.exportInterface
								( workspace.fileManager().attachPrefix( filePath ) ) ;
						}
						catch( java.io.IOException ex )
						{
							throw new EngineException
								( context , _exportinterface.this ,
								  token().name() + 
								  ": " + ex.getMessage() ) ;
						}
					} } ) ;
		context.ip = next ;
	}
}
