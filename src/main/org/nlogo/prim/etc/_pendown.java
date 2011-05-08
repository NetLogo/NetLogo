package org.nlogo.prim.etc ;

import org.nlogo.agent.Turtle;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Syntax;

public final strictfp class _pendown
	extends Command
{
	@Override
	public Syntax syntax()
	{
		return Syntax.commandSyntax( "-T--" , true ) ;
	}
	@Override
	public void perform( final org.nlogo.nvm.Context context )
	{
		( (Turtle) context.agent ).penMode( Turtle.PEN_DOWN ) ;
		context.ip = next ;
	}
}
