package org.nlogo.prim.etc ;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;

public final strictfp class _setdefaultshape
	extends Command
{
	@Override
	public Syntax syntax()
	{
		int[] right = { Syntax.TYPE_TURTLESET | Syntax.TYPE_LINKSET ,
						Syntax.TYPE_STRING } ;
		return Syntax.commandSyntax( right , "O---" ) ;
	}
	@Override
	public void perform( final Context context )
		throws LogoException
	{
		org.nlogo.agent.AgentSet breed = argEvalAgentSet( context , 0 ) ;
		String shape = argEvalString( context , 1 ) ;
		if( breed.type() == org.nlogo.agent.Patch.class )
		{
			throw new EngineException
				( context , this  , "cannot set the default shape of patches, because patches do not have shapes" ) ;
		}
		if( breed.type() == org.nlogo.agent.Observer.class )
		{
			throw new EngineException
				( context , this  , "cannot set the default shape of the observer, because the observer does not have a shape" ) ;
		}
		if( breed != world.turtles() && ! world.isBreed( breed ) &&
			breed != world.links() && ! world.isLinkBreed( breed ) )
		{
			throw new EngineException
				( context , this  , "can only set the default shape of all turtles , all links, or an entire breed" ) ;
		}
		if( breed.type() == org.nlogo.agent.Turtle.class )
		{
			String checkedShape = world.checkTurtleShapeName( shape ) ;
			if( checkedShape == null )
			{
				throw new EngineException
					( context , this  , "\"" + shape + "\" is not a currently defined turtle shape" ) ;
			}
			world.turtleBreedShapes.setBreedShape( breed , checkedShape ) ;
		}
		else if ( breed.type() == org.nlogo.agent.Link.class )
		{
			String checkedShape = world.checkLinkShapeName( shape ) ;
			if( checkedShape == null )
			{
				throw new EngineException
					( context , this  , "\"" + shape + "\" is not a currently defined link shape" ) ;
			}
			world.linkBreedShapes.setBreedShape( breed , checkedShape ) ;
		}
		context.ip = next ;
	}
}
