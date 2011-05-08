package org.nlogo.prim.threed ;

import org.nlogo.agent.Observer3D;
import org.nlogo.agent.Turtle3D;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Syntax;

public final strictfp class _facexyz
	extends Command
{
	@Override
	public Syntax syntax()
	{
		return Syntax.commandSyntax
			( new int[] { Syntax.TYPE_NUMBER ,
						  Syntax.TYPE_NUMBER ,
						  Syntax.TYPE_NUMBER } ,
			  "OT--" , true ) ;
	}
	@Override
	public void perform( final org.nlogo.nvm.Context context ) throws LogoException
	{
		if ( context.agent instanceof org.nlogo.agent.Turtle )
		{
			Turtle3D turtle = (Turtle3D) context.agent ;	
			turtle.face( argEvalDoubleValue( context , 0 ) ,
						 argEvalDoubleValue( context , 1 ) , 
						 argEvalDoubleValue( context , 2 ) , true ) ; 
		}
		else
		{
			Observer3D observer = (Observer3D) context.agent ;
			observer.face( argEvalDoubleValue( context , 0 ) ,
						   argEvalDoubleValue( context , 1 ) , 
						   argEvalDoubleValue( context , 2 ) ) ; 			
		}

		context.ip = next ;
	}
}
