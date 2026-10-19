package org.nlogo.extensions.vid

import java.awt.image.BufferedImage

trait VideoSource {
  def play(): Unit
  def stop(): Unit
  def close(): Unit
  def isPlaying: Boolean
  def captureImage(): BufferedImage
  def setTime(timeInSeconds: Double): Unit
  def videoNode(bounds: Option[(Double, Double)]): BoundedNode
}

trait VideoSourceContainer {
  def closeSource(): Unit
  def videoSource: Option[VideoSource]
  def videoSource_=(source: Option[VideoSource]): Unit
}
