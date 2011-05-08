package org.nlogo.api ;

public interface HubNetWorkspaceInterface
	extends CompilerServices
{
	WorldPropertiesInterface getPropertiesInterface() ;
	void hubNetRunning( boolean running ) ;
	String modelNameForDisplay() ;
}
