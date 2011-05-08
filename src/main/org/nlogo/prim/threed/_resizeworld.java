package org.nlogo.prim.threed ;

import org.nlogo.agent.World3D;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;

public final strictfp class _resizeworld
	extends Command
{
	@Override public Syntax syntax()
	{
		return Syntax.commandSyntax( new int[] {
				Syntax.TYPE_NUMBER , Syntax.TYPE_NUMBER, 
				Syntax.TYPE_NUMBER , Syntax.TYPE_NUMBER ,
				Syntax.TYPE_NUMBER , Syntax.TYPE_NUMBER } ,
			"O---" , true ) ;
	}
	@Override public void perform( Context context ) throws LogoException
	{
		final int newMinX = argEvalIntValue( context , 0 ) ;
		final int newMaxX = argEvalIntValue( context , 1 ) ;
		final int newMinY = argEvalIntValue( context , 2 ) ;
		final int newMaxY = argEvalIntValue( context , 3 ) ;
		final int newMinZ = argEvalIntValue( context , 4 ) ;
		final int newMaxZ = argEvalIntValue( context , 5 ) ;
		
		final int oldMinX = workspace.world().minPxcor() ;
		final int oldMaxX = workspace.world().maxPxcor() ;
		final int oldMinY = workspace.world().minPycor() ;
		final int oldMaxY = workspace.world().maxPycor() ;
		final int oldMinZ = ((World3D)workspace.world()).minPzcor() ;
		final int oldMaxZ = ((World3D)workspace.world()).maxPzcor() ;
		
		if( newMinX > 0 || newMaxX < 0 || newMinY > 0 || newMaxY < 0 || newMinZ > 0 || newMaxZ < 0 )
		{
			throw new EngineException
				( context , this , "You must include the point (0, 0, 0) in the world." ) ;
		}
		if( oldMinX != newMinX || oldMaxX != newMaxX ||
			oldMinY != newMinY || oldMaxY != newMaxY || 
			oldMinZ != newMinZ || oldMaxZ != newMaxZ )
		{
			workspace.setDimensions
				( new org.nlogo.api.WorldDimensions3D( newMinX , newMaxX ,
													   newMinY , newMaxY ,
													   newMinZ , newMaxZ ) ) ;
			workspace.waitFor
				( new org.nlogo.api.CommandRunnable() {
						public void run() { workspace.resizeView() ; } } ) ;
		}
		context.ip = next ;
	}
}
