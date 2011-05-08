package org.nlogo.api ;

public interface RandomServices
{
	org.nlogo.util.MersenneTwisterFast auxRNG() ;
	org.nlogo.util.MersenneTwisterFast mainRNG() ;
}
