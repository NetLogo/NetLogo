// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser

case class NativeLibraryException(message: String) extends Exception(message)

object NativeFileChooser {
  def createFileChooser: NativeFileChooser = {
    try {
      val os = System.getProperty("os.name").toLowerCase

      if (os.contains("windows")) {
        new WindowsFileChooser
      } else if (os.contains("mac")) {
        // new MacFileChooser
        new WrapperFileChooser
      } else {
        // new LinuxFileChooser
        new WrapperFileChooser
      }
    } catch {
      // if something goes wrong with native code, silently fall back on JFileChooser (Isaac B 7/9/25)
      case NativeLibraryException(_) => new WrapperFileChooser
    }
  }
}

// mimic the JFileChooser API so it can easily be used as a fallback if the native option fails (Isaac B 7/9/25)
abstract class NativeFileChooser {
  protected var title = ""
  protected var startDirectory: Option[File] = None
  protected var startFile: Option[File] = None
  protected var fileSelectionMode: Int = JFileChooser.FILES_AND_DIRECTORIES
  protected var fileTypes: Option[Seq[(String, String)]] = None
  protected var defaultExtension: Option[String] = None

  def setCurrentDirectory(directory: File): Unit = {
    startDirectory = Option(directory)
  }

  def setSelectedFile(file: File): Unit = {
    startFile = Option(file)
  }

  def setDialogTitle(title: String): Unit = {
    this.title = title
  }

  def setFileSelectionMode(mode: Int): Unit = {
    fileSelectionMode = mode
  }

  def setFileTypes(types: Seq[(String, String)]): Unit = {
    fileTypes = Option(types)
  }

  def setDefaultExtension(extension: String): Unit = {
    defaultExtension = Option(extension)
  }

  def showOpenDialog(frame: Frame): Int
  def showSaveDialog(frame: Frame): Int
  def getSelectedFile: File
  def cleanup(): Unit
}

class MacFileChooser extends NativeFileChooser {
  override def showOpenDialog(frame: Frame): Int = JFileChooser.CANCEL_OPTION
  override def showSaveDialog(frame: Frame): Int = JFileChooser.CANCEL_OPTION
  override def getSelectedFile: File = null
  override def cleanup(): Unit = {}
}

class LinuxFileChooser extends NativeFileChooser {
  override def showOpenDialog(frame: Frame): Int = JFileChooser.CANCEL_OPTION
  override def showSaveDialog(frame: Frame): Int = JFileChooser.CANCEL_OPTION
  override def getSelectedFile: File = null
  override def cleanup(): Unit = {}
}

// this is the fallback class, it wraps JFileChooser so that the calling code doesn't need to know whether
// the native option succeeded or not, it just uses the NativeFileChooser API regardless (Isaac B 7/9/25)
class WrapperFileChooser extends NativeFileChooser {
  private val chooser = new JFileChooser

  override def showOpenDialog(frame: Frame): Int = {
    startFile match {
      case Some(file) =>
        chooser.setSelectedFile(file)

      case _ =>
        startDirectory.foreach(chooser.setCurrentDirectory)
    }

    chooser.setDialogTitle(title)
    chooser.setFileSelectionMode(fileSelectionMode)
    chooser.showOpenDialog(frame)
  }

  override def showSaveDialog(frame: Frame): Int = {
    startFile match {
      case Some(file) =>
        chooser.setSelectedFile(file)

      case _ =>
        startDirectory.foreach(chooser.setCurrentDirectory)
    }

    chooser.setDialogTitle(title)
    chooser.setFileSelectionMode(fileSelectionMode)
    chooser.showSaveDialog(frame)
  }

  override def getSelectedFile: File =
    chooser.getSelectedFile

  override def cleanup(): Unit = {}
}
