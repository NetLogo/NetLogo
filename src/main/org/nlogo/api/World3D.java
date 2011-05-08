package org.nlogo.api;

public interface World3D extends World
{
	int worldDepth() ;
	Protractor3D protractor3D() ;
	int minPzcor() ;
	int maxPzcor() ;
	double wrappedObserverZ( double z ) ;
	double wrapZ( double z ) ; // World3D always wraps at present, so no AgentException - ST 3/3/09
	double followOffsetZ() ;
	Patch getPatchAt( double x , double y , double z ) throws AgentException ;
}
