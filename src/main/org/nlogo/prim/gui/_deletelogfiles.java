package org.nlogo.prim.gui ;

import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.Syntax;

public final strictfp class _deletelogfiles
	extends Command
{
	@Override
	public Syntax syntax()
	{
		return Syntax.commandSyntax( "O---", true ) ;
	}
	@Override
	public void perform( final Context context )
	{
		perform_1( context ) ; 
	}
	public void perform_1( final Context context )
	{
		workspace.deleteLogFiles() ;
		context.ip = next ;
	}
}
