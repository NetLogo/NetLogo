package org.nlogo.prim.gui ;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;
import org.nlogo.window.GUIWorkspace;

public final strictfp class _moviestatus
	extends org.nlogo.nvm.Reporter
{
	@Override
	public Syntax syntax()
	{
		return Syntax.reporterSyntax( Syntax.TYPE_STRING ) ;
	}
	@Override
	public Object report( final org.nlogo.nvm.Context context ) throws LogoException
	{
		if( ! ( workspace instanceof GUIWorkspace ) )
		{
			throw new EngineException(
				context , this , token().name() + " can only be used in the GUI" ) ;
		}
		org.nlogo.awt.MovieEncoder encoder = 
			( (GUIWorkspace) workspace ).movieEncoder;
			
		if ( encoder == null )
		{
			return "No movie.";
		}
		String status =  "" + encoder.getNumFrames() + " frames"
			+ "; frame rate = " + encoder.getFrameRate() ;
		
		if ( encoder.isSetup() ) 
		{
			java.awt.Dimension size = encoder.getFrameSize();
			status += "; size = " + size.width + "x" + size.height;
		}
		return status;
	}
}

