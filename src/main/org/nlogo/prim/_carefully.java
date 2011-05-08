package org.nlogo.prim ;

import org.nlogo.agent.AgentSet;
import org.nlogo.api.LogoException;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.api.Let;
import org.nlogo.nvm.Syntax;

public final strictfp class _carefully
	extends Command
	implements org.nlogo.nvm.CustomAssembled
{
	public final Let let = new Let() ;
	@Override
	public Syntax syntax()
	{
		return Syntax.commandSyntax
			( new int[] { Syntax.TYPE_COMMAND_BLOCK ,
						  Syntax.TYPE_COMMAND_BLOCK } ) ;
	}
	@Override
	public String toString()
	{
		return super.toString() + ":+" + offset ;
	}
	@Override
	public void perform( final Context context )
	{
		perform_1( context ) ;
	}
	public void perform_1( final Context context )
	{
		AgentSet agentset =
			new org.nlogo.agent.ArrayAgentSet
			( context.agent.getAgentClass() , 1 , false , world ) ;
		agentset.add( context.agent ) ;
		try
		{
			// start new job that skips over the _goto command
			context.runExclusiveJob( agentset , next + 1 ) ;
			// move on to the _goto command, which will skip to the end.
			context.ip = next ;
		}
		catch( LogoException ex )
		{
			context.let( let , ex ) ;
			context.ip = offset ; // jump to error handler
		}
	}
	public void assemble( org.nlogo.nvm.AssemblerAssistant a )
	{
		a.add( this ) ;
		a.goTo() ;
		a.block( 0 ) ;
		a.done() ;
		a.resume() ;
		a.block( 1 ) ;
		a.comeFrom() ;
	}
}
