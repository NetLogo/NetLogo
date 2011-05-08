package org.nlogo.api ;

import java.util.Collections;
import java.util.List;

public final strictfp class Let
{
	public final String varName ;
	public final int startPos ;
	public final int endPos ;
	public final List<Let> children ;
	public Let()
	{
		varName = null ;
		startPos = -1 ;
		endPos = -1 ;
		children = Collections.emptyList() ;
	}
	public Let( String varName , int startPos , int endPos , List<Let> children )
	{
		this.varName = varName ;
		this.startPos = startPos ;
		this.endPos = endPos ;
		this.children = children ;
	}
}
