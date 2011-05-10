package org.nlogo.awt;

import java.awt.Dimension;

/**
 * Encodes a sequence of BufferedImages into a movie.
 */
public interface MovieEncoder {
  /**
   * Sets the frame rate.
   *
   * @param frameRate frames per second.
   */
  void setFrameRate(float frameRate)
      throws java.io.IOException;

  /**
   * Returns the frame rate.
   */
  float getFrameRate();

  /**
   * Returns the frame size.
   */
  Dimension getFrameSize();

  /**
   * Returns a string describing the movie format.
   */
  String getFormat();

  /**
   * Adds an image to the movie.
   */
  void add(java.awt.image.BufferedImage image)
      throws java.io.IOException;

  /**
   * Returns true if this encoder has been set up.
   */
  boolean isSetup();

  /**
   * Stops the movie and writes it to a file.
   */
  void stop();

  /**
   * Cancels the current movie.
   */
  void cancel();

  /**
   * Returns the number of frames recorded so far.
   */
  int getNumFrames();
}

