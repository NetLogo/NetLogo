package org.nlogo.prim.etc ;

import org.nlogo.nvm.Command;
import org.nlogo.nvm.Syntax;

public final strictfp class _turtlecode
	extends Command
	implements org.nlogo.nvm.CustomAssembled
{
	@Override
	public Syntax syntax()
	{
		return Syntax.commandSyntax( "-T--" , false ) ;
	}
	@Override
	public void perform( final org.nlogo.nvm.Context context )
	{
	    throw new UnsupportedOperationException() ;
	}
	public void assemble( org.nlogo.nvm.AssemblerAssistant a )
	{
		// do nothing -- drop out of existence
	}
}
