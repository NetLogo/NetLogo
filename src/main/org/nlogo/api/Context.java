package org.nlogo.api;


/**
 * Provides access to the current execution environment.
 */
public interface Context {

  /**
   * Returns the agent that is currently executing this code.
   */
  org.nlogo.api.Agent getAgent();

  /**
   * Returns the drawing image. ( experimental )
   */
  java.awt.image.BufferedImage getDrawing();

  /**
   * Imports an image into the the patch colors either as NetLogo colors or RGB colors
   */
  void importPcolors(java.awt.image.BufferedImage image, boolean asNetLogoColors);

  /**
   * Transforms a relative path to a model into an absolute path by
   * prepending the current model directory.
   * If this is a new model, and therefore doesn't have a model directory yet,
   * the user's platform-dependent home directory is prepended instead.
   * If <code>filePath</code> is an absolute path, it is returned unchanged.
   *
   * @param filePath the path to be processed
   * @return an absolute path
   */
  String attachModelDir(String filePath)
      throws java.net.MalformedURLException;

  /**
   * Transforms a relative path to an absolute path by prepending
   * the current working directory.
   * If <code>filePath</code> is an absolute path, it is returned unchanged.
   * <p/>
   * The "current working directory" is the current
   * directory used by NetLogo's file I/O primitives, and can be changed by
   * the user at run-time using the <code>file-set-current-directory</code> primitive.
   * Its initial value is the directory from which the current model was
   * loaded, or the user's home directory if this is a new model.
   */
  String attachCurrentDirectory(String path)
      throws java.net.MalformedURLException;

  /**
   * This method returns the Random Number Generator
   * for the current Job.  This allows the creation
   * of random numbers that come from the same
   * predictable reproducible sequence that the
   * other NetLogo primitives use.  Thus, it is
   * generally preferable to pull your random numbers
   * from this source, rather than java.util.Random.
   *
   * @return a random number generator
   */
  org.nlogo.util.MersenneTwisterFast getRNG();

}
