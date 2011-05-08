package org.nlogo.prim.etc ;

import org.nlogo.agent.Link;
import org.nlogo.agent.Turtle;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;
import org.nlogo.nvm.Syntax;

public final strictfp class _otherend
	extends Reporter
{
	@Override
	public Syntax syntax()
	{
		return Syntax.reporterSyntax
			( Syntax.TYPE_AGENT , "-T-L" ) ;
	}
	@Override
	public Object report( final org.nlogo.nvm.Context context ) throws LogoException
	{
		Link link ;
		Turtle node ;
		if( context.agent instanceof Link )
		{
			link = (Link) context.agent ;
			if( ! ( context.myself() instanceof Turtle ) )
			{
				throw new EngineException( context , this , 
										   "Only a turtle can get the OTHER-END of a link" ) ;
			}
			node = (Turtle) context.myself() ;
		}
		else
		{
			node = (Turtle) context.agent ;
			if( ! ( context.myself() instanceof Link ) )
			{
				throw new EngineException( context , this , 
										   "Only a link can get the OTHER-END from a turtle." ) ;
			}
			link = (Link) context.myself() ;
		}
		
		Turtle dest = link.end2() ;
		Turtle src = link.end1() ;
		if( dest == node )
		{
			return src ;
		}
		if( src == node )
		{
			return dest ;
		}
		
		throw new EngineException( context , this ,
								   node.toString() + " is not linked by " + 
								   link.toString() + ".");
	}
}
