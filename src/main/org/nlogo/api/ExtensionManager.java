package org.nlogo.api;

/**
 * <code>ExtensionManager</code> provides some methods that can be used in runOnce
 */
public interface ExtensionManager {
  /**
   * Stores an object for the extension that can be retrieved in runOnce
   * if the extension is loaded again.  This is useful if the extension
   * has initialization it needs to perform that should only happen once,
   * even if the extension is loaded multiple times.
   *
   * @param obj the object to be stored
   */
  void storeObject(Object obj);

  /**
   * Returns the stored object.
   *
   * @return the stored object
   */
  Object retrieveObject();

  ExtensionObject readExtensionObject(String extname, String typeName, String value)
      throws CompilerException;

  Object readFromString(String src) throws CompilerException;

  /**
   * Instructs any loaded extensions to unload. Should be called previous
   * to a new model load.
   */
  void reset();

  /**
   * During compilation, we reach the extensions [ ... ] block.
   * When that happens, the compiler tells the ExtensionManager that it needs to
   * forget what extensions are in the extensions [ ... ] block, by calling this method.
   * <p/>
   * The compiler will then call the importExtension method for each extension in the block.
   * Among other things, this lets the ExtensionManager know each extension that is
   * "live", or currently in the block, so that its primitives are available for use
   * elsewhere in the model.
   * <p/>
   * See the top of {@link org.nlogo.workspace.ExtensionManager} for full details.
   */
  void startFullCompilation();

  /**
   * Instructs any extensions which haven't been re-imported during the
   * current compile to shut down. Should be called during each full
   * re-compile.
   * <p/>
   * See the top of {@link org.nlogo.workspace.ExtensionManager} for full details.
   */
  void finishFullCompilation();

  /**
   * Returns true if any extensions have been imported in the current model,
   * false otherwise.
   */
  boolean anyExtensionsLoaded();

  /**
   * Returns the identifier "name" by its imported implementation, if any,
   * or null, if not.
   */
  Primitive replaceIdentifier(String name);

  /**
   * Loads the extension contained in the jar at jarPath.
   *
   * @param jarPath the path to the extension jar. May be relative to the
   *                current model directory.
   * @param errors  the ErrorSource to use when a CompilerException needs
   *                to be thrown.
   */
  void importExtension(String jarPath, ErrorSource errors) throws CompilerException;

  String resolvePath(String path);

  String resolvePathAsURL(String path);

  String dumpExtensions();

  String dumpExtensionPrimitives();

  String getSource(String filename) throws java.io.IOException;

  void addToLibraryPath(Object classManager, String directory);

  File getFile(String path) throws ExtensionException;

  java.util.List<String> getJarPaths();

  java.util.List<String> getExtensionNames();

  boolean profilingEnabled();
}
