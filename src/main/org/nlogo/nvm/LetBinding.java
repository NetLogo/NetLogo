package org.nlogo.nvm ;

import org.nlogo.api.Let;

strictfp class LetBinding
{
	final Let let ;
	Object value ;
	LetBinding( Let let , Object value )
	{
		this.let = let ;
		this.value = value ;
	}
}
