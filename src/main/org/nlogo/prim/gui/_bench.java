package org.nlogo.prim.gui ;

import org.nlogo.api.LogoException;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;
import org.nlogo.workspace.AbstractWorkspace;
import org.nlogo.workspace.Benchmarker;

public final strictfp class _bench
	extends org.nlogo.nvm.Command
{
	@Override public Syntax syntax()
	{
		return Syntax.commandSyntax
			( new int[] { Syntax.TYPE_NUMBER , Syntax.TYPE_NUMBER } ,
				"O---" ) ;
	}
	@Override public void perform( Context context ) throws LogoException
	{
		final int minTime = argEvalIntValue( context , 0 ) ;
		final int maxTime = argEvalIntValue( context , 1 ) ;
		new Thread( "__bench" ) {
			@Override public void run() {
				Benchmarker.benchmark( (AbstractWorkspace) workspace , minTime , maxTime ) ;
			} }.start() ;
		context.ip = next ;
	}
}
