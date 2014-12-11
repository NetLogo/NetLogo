// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.agent.{ World, OutputObject }
import org.nlogo.api.{FileMode, I18N, File, LocalFile}
import java.net.URL
import java.io.{File => JFile, PrintWriter, BufferedReader, FileNotFoundException, IOException, EOFException}

import scala.collection._

final class DefaultFileManager(val workspace: AbstractWorkspace) extends org.nlogo.nvm.FileManager {
  private var openFiles: Map[String, File] = Map[String, File]()
  private var _currentFile: Option[File] = None
  var prefix: String = System.getProperty("user.home")

  @throws(classOf[java.io.IOException])
  def getErrorInfo: String = {
    currentFile.map { file =>
      val position: Long = file.pos
      file.close(true)
      file.open(FileMode.Read)
      var lineNumber: Int = 1
      var prevPosition: Long = 0
      var lastLine: String = readLine
      while (file.pos < position) {
        lineNumber += 1
        prevPosition = file.pos
        lastLine = readLine
      }
      var charPos: Int = (position - prevPosition).toInt
      if (charPos >= lastLine.length && !eof) {
        lastLine = readLine
        charPos = 0
        lineNumber += 1
      }
      closeCurrentFile()
      " (line number " + lineNumber + ", character " + (charPos + 1) + ")"
    }.getOrElse(throw new IOException)
  }

  def getFile(filename: String): File = {
    new LocalFile(filename)
  }

  def setPrefix(setPrefix: String) = {
    def ensureDirPath(s: String) = if (s.last != java.io.File.separatorChar) s + java.io.File.separatorChar else s
    if (setPrefix == "") {
      prefix = ""
    } else {
      val newPrefix = ensureDirPath(setPrefix)
      if (new JFile(newPrefix).isAbsolute) {
        prefix = newPrefix
      } else {
        prefix = ensureDirPath(relativeToAbsolute(newPrefix))
      }
    }
  }

  def setPrefix(newPrefix: URL) = {
    prefix = newPrefix.toString
  }

  @throws(classOf[java.net.MalformedURLException])
  def attachPrefix(filename: String): String = {
    if (new JFile(filename).isAbsolute || prefix == "") {
      filename
    } else {
      relativeToAbsolute(filename)
    }
  }

  def currentFile: Option[File] = _currentFile

  def hasCurrentFile: Boolean = {
    currentFile.flatMap(f => openFiles.get(f.getAbsolutePath)).isDefined
  }

  def findOpenFile(filename: String): Option[File] = {
    val newFile: JFile = new JFile(filename)
    openFiles.get(newFile.getAbsolutePath)
  }

  @throws(classOf[java.io.IOException])
  def ensureMode(openMode: FileMode) = {
    currentFile.map(ensureFileMode(_, openMode)).getOrElse(fileNotAvailable())
  }

  def fileExists(filePath: String): Boolean = new JFile(filePath).exists

  @throws(classOf[java.io.IOException])
  def deleteFile(filePath: String) = {
    val file: Option[File] = findOpenFile(filePath)
    if (file.isDefined) {
      throw new IOException("You need to close the file before deletion")
    }
    val checkFile = new JFile(filePath)
    if (!checkFile.exists) {
      throw new IOException(I18N.errorsJ.get("org.nlogo.workspace.DefaultFileManager.cannotDeleteNonExistantFile"))
    }
    if (!checkFile.canWrite) {
      throw new IOException("Modification to this file is denied.")
    }
    if (!checkFile.isFile) {
      throw new IOException(I18N.errorsJ.get("org.nlogo.workspace.DefaultFileManager.canOnlyDeleteFiles"))
    }
    if (!checkFile.delete) {
      throw new IOException("Deletion failed.")
    }
  }

  @throws(classOf[java.io.IOException])
  def openFile(newFileName: String) = {
    val fullFileName: String = attachPrefix(newFileName)
    if (fullFileName == null) {
      throw new IOException("This filename is illegal, " + newFileName)
    }
    _currentFile = findOpenFile(fullFileName).orElse {
      val createdFile = new LocalFile(fullFileName)
      openFiles = openFiles + (createdFile.getAbsolutePath -> createdFile)
      Some(createdFile)
    }
  }

  @throws(classOf[java.io.IOException])
  def flushCurrentFile() = {
    currentFile.map(_.flush()).getOrElse(throw new IOException("There is no file to file"))
  }

  @throws(classOf[java.io.IOException])
  def closeCurrentFile() = {
    currentFile.map(closeFile).getOrElse(throw new IOException("There is no file to close"))
    _currentFile = None
  }

  @throws(classOf[java.io.IOException])
  def readLine(): String = {
    currentFile.map(readable).map(notAtEof).map { file =>
      def readToNewLineOrEof(iter: BufferedIterator[Char], acc: String = ""): String = {
        if (iter.hasNext) {
          (iter.next(), iter.head) match {
            case ('\r', '\n') =>
              iter.next()
              acc
            case ('\r', _) => acc
            case ('\n', _) => acc
            case (c, _) => readToNewLineOrEof(iter, acc + c)
          }
        } else {
          acc
        }
      }

      val charIterator = new BufferedFileCharIterator(file)

      readToNewLineOrEof(charIterator)
    }.getOrElse(fileNotAvailable())
  }

  @throws(classOf[java.io.IOException])
  def readChars(num: Int): String = {
    currentFile.map(readable).map(notAtEof).map { file =>
      new BufferedFileCharIterator(file).take(num).mkString
    }
  }.getOrElse(fileNotAvailable())

  @throws(classOf[java.io.IOException])
  def read(world: World): AnyRef = {
    currentFile.map(readable).map(notAtEof).map { file =>
      // This should read in from the file until it hits Whitespace, and pass the string into readFromFile
      // this is something to work on once the compiler divorce is merged in -- RGG 12/9/14
      workspace.compiler.readFromFile(file, world, workspace.getExtensionManager)
    }.getOrElse(fileNotAvailable())
  }

  @throws(classOf[java.io.IOException])
  def eof: Boolean = {
    currentFile.map(readable).map(updateFileEof).map(_.eof).getOrElse(fileNotAvailable())
  }

  @throws(classOf[java.io.IOException])
  def closeAllFiles() {
    openFiles.foreach { case (filePath, file) => closeFile(file) }
    _currentFile = None
  }

  def writeOutputObject(oo: OutputObject) = {
    currentFile.map(_.getPrintWriter.print(oo.get)).getOrElse(throw new IOException)
  }

  def handleModelChange() = {
    if (workspace.getModelDir != null) {
      setPrefix(workspace.getModelDir)
    } else {
      setPrefix(System.getProperty("user.home"))
    }
    try {
      closeAllFiles()
    } catch {
      case ex: IOException => throw new IllegalStateException(ex)
    }
  }

  private def notAtEof(file: File): File = {
    updateFileEof(file)
    if (file.eof)
      throw new EOFException()
    else
      file
  }

  private def updateFileEof(file: File): File = {
    if (!file.eof) {
      file.eof = ! new BufferedFileCharIterator(file).hasNext
    }
    file
  }

  private def readable(file: File): File = ensureFileMode(file, FileMode.Read)

  private def ensureFileMode(file: File, openMode: FileMode): File = {
    (file.mode, openMode) match {
      case (FileMode.None, mode) =>
        try {
          if (mode == FileMode.None) {
            throw new IllegalArgumentException("must specify a valid file mode for opening")
          }
          file.open(mode)
        } catch {
          case ex: FileNotFoundException =>
            throw new IOException("The file " + file.getAbsolutePath + " cannot be found")
          case ex: IOException => throw new IOException(ex.getMessage)
        }
      case (FileMode.Read, expectedMode) if expectedMode != FileMode.Read =>
        throw new IOException("You can only use READING primitives with this file")
      case (currentMode, expectedMode) if currentMode != expectedMode =>
        throw new IOException("You can only use WRITING primitives with this file")
      case _ =>
    }
    file
  }

  private def fileNotAvailable(): Nothing = {
    throw new IOException(I18N.errors.get("org.nlogo.workspace.DefaultFileManager.noOpenFile"))
  }

  @throws(classOf[java.io.IOException])
  private def closeFile(file: File) = {
    openFiles = openFiles - file.getAbsolutePath
    file.close(true)
  }

  private def relativeToAbsolute(newPath: String): String = {
    try {
      new JFile(prefix + java.io.File.separatorChar + newPath).getCanonicalPath
    } catch {
      case ex: IOException => throw new IllegalStateException(ex)
    }
  }

  class BufferedFileCharIterator(file: File) extends BufferedIterator[Char] {
    private val buffReader: BufferedReader = file.reader

    private def nextChar(reset: Boolean = true): Int = {
      buffReader.mark(1)
      val i = buffReader.read()
      if (reset) {
        buffReader.reset()
      } else {
        file.pos += 1
      }
      i
    }

    override def head: Char = nextChar().asInstanceOf[Char]
    override def hasNext: Boolean = nextChar() != -1
    override def next(): Char = nextChar(false).asInstanceOf[Char]
  }
}
