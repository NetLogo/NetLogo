package org.nlogo.api ;

/**
 * An abstract, partial implementation of ClassManager that implements 
 * <code>runOnce()</code> and <code>unload()</code> with empty methods.
 * @see ClassManager
 */

import java.util.List;

public abstract strictfp class DefaultClassManager
	implements ClassManager
{
   /**
    * Empty implementation.
    */
	@SuppressWarnings("unused")
	public void runOnce( ExtensionManager em ) throws ExtensionException
	{}
	
   /**
    * Loads the primitives in the extension.  
    * This is called once per model compilation.
    * 
    * @param primManager The manager to transport the primitives to NetLogo
    */
	public abstract void load( PrimitiveManager primManager ) throws ExtensionException;

   /**
    * Empty implementation.
    */
	@SuppressWarnings("unused")
	public void unload( ExtensionManager em ) throws ExtensionException
	{}

	/**
	 * Default exports nothing
	 */
	public StringBuilder exportWorld()
	{
		return new StringBuilder() ;
	}

	/**
	 * Default loads nothing
	 */
	@SuppressWarnings("unused")
	public void importWorld( List<String[]> lines , ExtensionManager reader , ImportErrorHandler handler ) 
		throws ExtensionException {}

	/**
	 * Default does nothing
	 */ 
	public void clearAll() {}

	/**
	 *  Default defines no extension objects, thus, we cannot read any extension objects
	 */
	@SuppressWarnings("unused")
	public ExtensionObject readExtensionObject( ExtensionManager em, String typeName, String value )
		throws ExtensionException , CompilerException
	{
		throw new IllegalStateException
			( "readExtensionObject not implemented for " + this ) ;
	}

	public List<String> additionalJars()
	{
		return new java.util.ArrayList<String>() ;
	}
}
