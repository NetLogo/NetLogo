package org.nlogo.extensions.vid

import java.awt.image.BufferedImage
import java.io.FileNotFoundException
import java.nio.file.{ Files, Path }
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

import org.jcodec.api.SequenceEncoder
import org.jcodec.scale.AWTUtil

class MP4Recorder extends Recorder {
  private var activeRecording = Option.empty[SequenceEncoder]
  private var recordingPath = Option.empty[Path]
  private var activeResolution = Option.empty[(Int, Int)]

  def isRecording: Boolean =
    activeRecording.isDefined

  def start(): Unit = {
    if (activeRecording.isDefined)
      throw Recorder.AlreadyStarted
    val path = Files.createTempFile("vid", "mp4")
    recordingPath   = Some(path)
    activeRecording = Some(new SequenceEncoder(path.toFile))
  }

  def setResolution(width: Int, height: Int): Unit = {
    if (activeResolution.isEmpty)
      activeResolution = Some(roundResolution(width, height))
  }

  private def roundResolution(width: Int, height: Int): (Int, Int) = {
    if (width % 2 == 1 && height % 2 == 1) (width + 1, height + 1)
    else if (width % 2 == 1)               (width + 1, height)
    else if (height % 2 == 1)              (width,     height + 1)
    else                                   (width,     height)
  }

  def save(dest: Path): Unit = {
    if (! activeRecording.isDefined)
      throw Recorder.NotRecording
    if (! Files.exists(dest.toAbsolutePath.getParent))
      throw new FileNotFoundException("no such directory: " + dest.toAbsolutePath.toString)
    try {
      activeRecording.foreach(_.finish())
      recordingPath.foreach(src => Files.copy(src, dest, REPLACE_EXISTING))
    } catch {
      case e: IndexOutOfBoundsException => throw Recorder.NoFrames
    } finally {
      reset()
    }
  }

  def reset(): Unit = {
    recordingPath   = None
    activeRecording = None
    activeResolution = None
  }

  def recordFrame(image: BufferedImage): Unit = {
    if (activeResolution.isEmpty)
      activeResolution = Some(roundResolution(image.getWidth, image.getHeight))
    if (activeRecording.isDefined)
      activeRecording.foreach { recording =>
        activeResolution.foreach { res =>
          val rgbImage = new BufferedImage(res._1, res._2, BufferedImage.TYPE_INT_RGB)
          rgbImage.getGraphics.drawImage(image, 0, 0, res._1, res._2, 0, 0, image.getWidth, image.getHeight, null)
          recording.encodeNativeFrame(AWTUtil.fromBufferedImage(rgbImage))
        }
      }
    else
      throw new RuntimeException("No recording has been started!")
  }
}
