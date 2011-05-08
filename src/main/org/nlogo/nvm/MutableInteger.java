package org.nlogo.nvm ;

public strictfp class MutableInteger
{
	public int value ;
	public MutableInteger( int value )
	{
		this.value = value ;
	}
	
	@Override
	public String toString() 
	{
		return super.toString() + ":" + value ;
	}
}
