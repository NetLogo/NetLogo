package org.nlogo.api ;

/**
 * Wrapper class for exceptions thrown by NetLogo extensions.
 */
public strictfp class ExtensionException
	extends Exception
{
   /**
    * Creates a new ExtensionException
    * @param message	error message displayed to NetLogo user
    */
	public ExtensionException( String message )
	{
		super( message ) ;
	}

   /**
    * Creates a new ExtensionException
    * @param ex extension to be wrapped
    */
	public ExtensionException( Exception ex )
	{
		this( ex.getMessage() ) ;
		setStackTrace( ex.getStackTrace() ) ;
	}
}
