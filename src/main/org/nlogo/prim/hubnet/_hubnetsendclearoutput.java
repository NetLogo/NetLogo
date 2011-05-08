package org.nlogo.prim.hubnet ;

import java.util.Iterator ;

import org.nlogo.api.Dump;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Syntax;
import org.nlogo.util.JCL;

public final strictfp class _hubnetsendclearoutput
	extends org.nlogo.nvm.Command
{
	@Override
	public void perform( final org.nlogo.nvm.Context context ) throws LogoException
	{
		Object clients = args[ 0 ].report( context ) ;

		java.util.List<String> nodes = new java.util.ArrayList<String>() ;
		if( clients instanceof LogoList )
		{			
			for( Iterator<Object> nodesIter = ((LogoList) clients).iterator() ;
				 nodesIter.hasNext() ; )
			{
				Object node = nodesIter.next() ;
				if( ! ( node instanceof String ) )
				{
					throw new EngineException
						( context , this, "HUBNET-SEND expected "
						  + Syntax.aTypeName( Syntax.TYPE_STRING| Syntax.TYPE_LIST )
						  + " of strings as the first input, but one item is the "
						  + Syntax.typeName( node ) + " " +
						  Dump.logoObject( node )
						  + " instead" ) ;
				}
				nodes.add( (String) node ) ;
			}
		}
		else if( clients instanceof String ) 
		{
			nodes.add( (String) clients ) ;
		}
		else
		{
			throw new org.nlogo.nvm.ArgumentTypeException
				( context , this , 0 , Syntax.TYPE_LIST | Syntax.TYPE_STRING , clients ) ;
		}

		workspace.getHubNetManager().clearText( JCL.toScalaSeq(nodes) ) ;
		context.ip = next ;
	}
	@Override
	public Syntax syntax()
	{
		int[] right = { Syntax.TYPE_STRING | Syntax.TYPE_LIST } ;
		return Syntax.commandSyntax( right ) ;
	}
}
