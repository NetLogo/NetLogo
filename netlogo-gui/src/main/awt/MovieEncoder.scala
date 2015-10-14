// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.awt

import java.awt.Dimension
import java.awt.image.BufferedImage

/**
 * Encodes a sequence of BufferedImages into a movie.
 */
trait MovieEncoder {

  /**
   * Sets the frame rate.
   *
   * @param frameRate frames per second.
   */
  @throws(classOf[java.io.IOException])
  def setFrameRate(frameRate: Float)

  /**
   * Returns the frame rate.
   */
  def getFrameRate: Float

  /**
   * Returns the frame size.
   */
  def getFrameSize: Dimension

  /**
   * Returns a string describing the movie format.
   */
  def getFormat: String

  /**
   * Adds an image to the movie.
   */
  @throws(classOf[java.io.IOException])
  def add(image: BufferedImage)

  /**
   * Returns true if this encoder has been set up.
   */
  def isSetup: Boolean

  /**
   * Stops the movie and writes it to a file.
   */
  def stop()

  /**
   * Cancels the current movie.
   */
  def cancel()

  /**
   * Returns the number of frames recorded so far.
   */
  def getNumFrames: Int

}
