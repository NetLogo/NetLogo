package org.nlogo.prim.gui ;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;
import org.nlogo.window.GUIWorkspace;

public final strictfp class _load3Dshapes
	extends Command
{
	@Override
	public Syntax syntax()
	{
		int[] right = { Syntax.TYPE_STRING } ;
		return Syntax.commandSyntax( right , "O---" , true ) ;
	}
	@Override
	public void perform( final org.nlogo.nvm.Context context ) throws LogoException
	{
		String filename = argEvalString( context , 0 ) ;
		if( workspace instanceof GUIWorkspace )
		{
			try
			{
				( (GUIWorkspace) workspace ).addCustomShapes( filename ) ;
			}
			catch( java.io.IOException e )
			{
				throw new EngineException( context , this , e.getMessage() ) ;
			}
			catch( org.nlogo.shape.InvalidShapeDescriptionException e )
			{
				throw new EngineException( context , this , "Invalid shape file" ) ;
			}
		}
		context.ip = next ;
	}
}
