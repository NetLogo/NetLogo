package org.nlogo.prim.threed;

import org.nlogo.agent.Turtle3D;
import org.nlogo.api.Vect;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;

public final strictfp class _tiltdown
	extends Command
{
	@Override
	public Syntax syntax()
	{
		return Syntax.commandSyntax
			( new int[] { Syntax.TYPE_NUMBER } ,
			  "-T--" , true ) ;
	}
	@Override
	public void perform( final Context context )
		throws LogoException
	{
		double delta = argEvalDoubleValue( context , 0 ) ;
		Turtle3D turtle = (Turtle3D) context.agent ;
		Vect[] v = 
			Vect.toVectors( turtle.heading() , 
									 turtle.pitch() , 
									 turtle.roll() ) ;
		Vect pitch = 
			new Vect( 0 , StrictMath.cos( StrictMath.toRadians( - delta ) ) ,
					  StrictMath.sin( StrictMath.toRadians( - delta ) ) ) ;
		Vect orthogonal = v[ 1 ].cross( v[ 0 ] ) ;
		Vect forward =
			Vect.axisTransformation( pitch , v[1] , v[0] , orthogonal ) ;
		
		double[] angles = Vect.toAngles( forward , v[1] ) ;
		turtle.headingPitchAndRoll( angles[0], angles[1], angles[2] ) ;
		
		context.ip = next ;
	}
}
