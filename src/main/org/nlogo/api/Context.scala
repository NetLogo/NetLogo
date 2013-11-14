// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

/**
 * Provides access to the current execution environment.
 */
trait Context {

  /** Returns the agent that is currently executing this code. */
  def getAgent: Agent

  /** Returns the drawing image. */
  def getDrawing: java.awt.image.BufferedImage

  /** Imports an image into the patch colors either as NetLogo colors or RGB colors. */
  def importPcolors(image: java.awt.image.BufferedImage, asNetLogoColors: Boolean)

  /**
   * Transforms a relative path to an absolute path by prepending the current working directory.  If
   * <code>filePath</code> is an absolute path, it is returned unchanged.
   *
   * The "current working directory" is the current directory used by NetLogo's file I/O primitives,
   * and can be changed by the user at run-time using the <code>file-set-current-directory</code>
   * primitive.  Its initial value is the directory from which the current model was loaded, or the
   * user's home directory if this is a new model.
   */
  @throws(classOf[java.net.MalformedURLException])
  def attachCurrentDirectory(path: String): String

  /**
   * This method returns the Random Number Generator for the current Job.  This allows the creation
   * of random numbers that come from the same predictable reproducible sequence that the other
   * NetLogo primitives use.  Thus, it is generally preferable to pull your random numbers from this
   * source, rather than java.util.Random.
   *
   * @return a random number generator
   */
  def getRNG: org.nlogo.util.MersenneTwisterFast

}
