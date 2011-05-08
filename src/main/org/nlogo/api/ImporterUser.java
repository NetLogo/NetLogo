package org.nlogo.api;

import java.util.List;

public interface ImporterUser 
	extends WorldResizer
{ 
    // handle output area
    void setOutputAreaContents( String text ) ;
	// handle importing plots
	void currentPlot( String plot )  ;
	PlotInterface getPlot( String plot ) ;
	// handle importing extensions
	boolean isExtensionName( String name ) ;
	void importExtensionData( String name , List<String[]> data , 
							  ImportErrorHandler handler ) 
		throws ExtensionException ;
}
