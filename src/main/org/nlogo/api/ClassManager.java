package org.nlogo.api ;
 
/**
 * Interface specifies the main class of a NetLogo extension.
 * All NetLogo extensions must include a class that implements this interface.
 * Registers primitives with NetLogo and handles extension initialization and deconstruction.
 * 
 * <p>
 * For example: 
 * <pre>
 * public class FibonacciExtension extends org.nlogo.api.DefaultClassManager
 * {
 *     public void load(org.nlogo.api.PrimitiveManager primManager)
 *     {
 *         primManager.addPrimitive("first-n-fibs", new Fibonacci());
 *     }
 * }
 * </pre>
 * 
 **/

import java.util.List;

public interface ClassManager
{
   /**
    * Initializes the extension.  
    * This is called once per NetLogo instance.
    */
	void runOnce( ExtensionManager em ) throws ExtensionException ;
	
   /**
    * Loads the primitives in the extension.  
    * This is called each time a model that uses this extension is compiled.
    * 
    * @param primManager The manager to transport the primitives to NetLogo
    */
	void load( PrimitiveManager primManager ) throws ExtensionException;

   /**
    * Cleans up the extension. This is called once before <code>load</code> is called and once
	* before NetLogo is closed or another model is opened.
    */
	void unload( ExtensionManager em ) throws ExtensionException;

	/**
	 * Return a new NetLogo ExtensionObject
	 *
	 * @param reader An interface that allows the extension to read NetLogo objects
	 * @param typeName The type of ExtensionObject to be returned
	 * @param value The string representation of the object
	 */
	ExtensionObject readExtensionObject( ExtensionManager reader , String typeName , String value )
		throws ExtensionException , CompilerException ;

	/**
	 * Write any state needed to restore the world.
	 * @return a StringBuilder containing all the data to export if the StringBuilder is empty no section is written
	 */
	StringBuilder exportWorld() ;
	
	/**
	 * Reload any state saved in an export world file 
	 * @param lines A list of lines exported by this extension the lines are broken up into an array delimited by commas
	 * @param reader An interface that allows the extension to read NetLogo objects
	 * @param handler An interface that allows the extensions to report non-fatal errors during the import
	 */
	void importWorld( List<String[]> lines , ExtensionManager reader , ImportErrorHandler handler ) throws ExtensionException ;

	/**
	 *  Clear any stored state
	 */
	void clearAll() ;

	List<String> additionalJars() ;
}
