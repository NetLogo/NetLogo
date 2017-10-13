// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import
  java.io.{ BufferedReader, EOFException, File => JFile, FileNotFoundException, IOException },
      JFile.separatorChar

import
  org.nlogo.{ agent, api, core, nvm },
    agent.{ OutputObject, World },
    core.{ CompilerException, CompilerUtilitiesInterface, File, FileMode, I18N },
    api.LocalFile,
    nvm.{ FileManager, ImportHandler }

private[workspace] final class DefaultFileManager(
  private val tracker: ModelTracker,
  extensionManager:    ExtensionManager,
  utilities:           CompilerUtilitiesInterface)
  extends FileManager
  with WorkspaceMessageListener {

  private            var openFiles:    Map[String, File] = Map[String, File]()
  private[workspace] var _currentFile: Option[File]      = None
  private[workspace] var _prefix:      String            = ""

  def currentFile: Option[File] = _currentFile

  def prefix: String = _prefix

  def getErrorInfo: String =
    _currentFile.map {
      file =>

        val position = file.pos
        file.close(true)
        file.open(FileMode.Read)

        var lineNumber:   Int    = 1
        var prevPosition: Long   = 0
        var lastLine:     String = readLine()

        while (file.pos < position) {
          lineNumber   += 1
          prevPosition  = file.pos
          lastLine      = readLine()
        }

        var charPos: Int = (position - prevPosition).toInt
        if (charPos >= lastLine.length && !eof) {
          lastLine    = readLine()
          charPos     = 0
          lineNumber += 1
        }

        closeCurrentFile()

        s" (line number $lineNumber, character ${charPos + 1})"
    }.getOrElse(throw new IOException)

  def getFile(filename: String): File =
    new LocalFile(filename)

  def processWorkspaceEvent(evt: WorkspaceEvent): Unit = {
    evt match {
      case m: ModelPathChanged =>
        m.modelDirName.foreach(dirName => setPrefix(dirName))
      case SwitchModel(_, _) => handleModelChange()
      case _ =>
    }
  }

  def setPrefix(setPrefix: String): Unit = {
    _prefix =
      if (setPrefix == "")
        ""
      else {
        val asDirPath = (s: String) => s + (if (s.last != separatorChar) separatorChar else "")
        val newPrefix = asDirPath(setPrefix)
        if (new JFile(newPrefix).isAbsolute)
          newPrefix
        else
          asDirPath(relativeToAbsolute(newPrefix))
      }
  }

  def attachPrefix(filename: String): String =
    if (new JFile(filename).isAbsolute || prefix == "")
      filename
    else
      relativeToAbsolute(filename)

  def hasCurrentFile: Boolean =
    _currentFile.flatMap(f => openFiles.get(f.getAbsolutePath)).nonEmpty

  def findOpenFile(filename: String): Option[File] = {
    val newFile = new JFile(filename)
    openFiles.get(newFile.getAbsolutePath)
  }

  def ensureMode(openMode: FileMode): Unit = {
    _currentFile.fold(throwNoOpenFile())(throwOnBadFileMode(_, openMode))
  }

  def fileExists(filePath: String): Boolean =
    new JFile(filePath).exists()

  def deleteFile(filePath: String): Unit = {

    if (findOpenFile(filePath).nonEmpty)
      throw new IOException("You need to close the file before deletion")

    val checkFile = new JFile(filePath)

    if (!checkFile.exists)
      throw new IOException(I18N.errorsJ.get("org.nlogo.workspace.DefaultFileManager.cannotDeleteNonExistantFile"))
    else if (!checkFile.canWrite)
      throw new IOException("Modification to this file is denied.")
    else if (!checkFile.isFile)
      throw new IOException(I18N.errorsJ.get("org.nlogo.workspace.DefaultFileManager.canOnlyDeleteFiles"))
    else if (!checkFile.delete)
      throw new IOException("Deletion failed.")

  }

  def openFile(newFileName: String): Unit = {
    _currentFile = Option(attachPrefix(newFileName)).map {
      fullFileName =>
        findOpenFile(fullFileName) orElse {
          val createdFile = new LocalFile(fullFileName)
          openFiles += createdFile.getAbsolutePath -> createdFile
          Option(createdFile)
        }
    }.getOrElse(throw new IOException(s"This filename is illegal, $newFileName"))
  }

  def flushCurrentFile(): Unit = {
    _currentFile.fold(throw new IOException("There is no file to flush"))(_.flush())
  }

  def closeCurrentFile(): Unit = {
    _currentFile.fold(throw new IOException("There is no file to close"))(closeFile)
    _currentFile = None
  }

  def readLine(): String = {

    def readToNewLineOrEof(iter: BufferedIterator[Char], acc: String = ""): String =
      if (iter.hasNext)
        (iter.next(), iter.head) match {
          case ('\r', '\n') => iter.next(); acc
          case ('\r', _)    => acc
          case ('\n', _)    => acc
          case (c,    _)    => readToNewLineOrEof(iter, acc + c)
        }
      else
        acc

    _currentFile.map(asReadableFile _                  andThen
                    asFileNotAtEof                    andThen
                    (new BufferedFileCharIterator(_)) andThen
                    (readToNewLineOrEof(_)))
      .getOrElse(throwNoOpenFile())

  }

  def readChars(num: Int): String = {
    val takeCharsFromFile = (file: File) => new BufferedFileCharIterator(file).take(num).mkString
    _currentFile.map(asReadableFile _ andThen asFileNotAtEof andThen takeCharsFromFile).getOrElse(throwNoOpenFile())
  }

  def read(world: World): AnyRef = {
    val importHandler = new ImportHandler(world, extensionManager)
    val readLiteral = { (file: File) =>
      val oldPos = file.pos
      try {
        utilities.readFromFile(file, importHandler)
      } catch {
        case ex: CompilerException =>
          file.pos = oldPos
          throw ex
      }
    }
    _currentFile.map(asReadableFile _ andThen asFileNotAtEof andThen readLiteral).getOrElse(throwNoOpenFile())
  }

  def eof: Boolean =
    _currentFile.map(asReadableFile _ andThen updateFileEof andThen (_.eof)).getOrElse(throwNoOpenFile())

  def closeAllFiles(): Unit = {
    openFiles.values foreach closeFile
    _currentFile = None
  }

  def writeOutputObject(oo: OutputObject): Unit = {
    _currentFile.fold(throw new IOException)(_.getPrintWriter.print(oo.get))
  }

  def handleModelChange(): Unit = {
    Option(tracker.getModelDir).foreach(setPrefix)
    try closeAllFiles()
    catch {
      case ex: IOException => throw new IllegalStateException(ex)
    }
  }

  private def asFileNotAtEof(file: File): File = {
    updateFileEof(file)
    if (file.eof)
      throw new EOFException()
    file
  }

  private def updateFileEof(file: File): File = {
    if (!file.eof)
      file.eof = !new BufferedFileCharIterator(file).hasNext
    file
  }

  private def asReadableFile(file: File): File = {
    throwOnBadFileMode(file, FileMode.Read)
    file
  }

  private def throwOnBadFileMode(file: File, openMode: FileMode): Unit = {
    (file.mode, openMode) match {
      case (FileMode.None, FileMode.None) =>
        throw new IllegalArgumentException("must specify a valid file mode for opening")
      case (FileMode.None, mode) =>
        try file.open(mode)
        catch {
          case ex: FileNotFoundException =>
            throw new IOException(s"The file ${file.getAbsolutePath} cannot be found")
          case ex: IOException =>
            throw new IOException(ex.getMessage)
        }
      case (FileMode.Read, expectedMode) if expectedMode != FileMode.Read =>
        throw new IOException("You can only use READING primitives with this file")
      case (currentMode, expectedMode) if currentMode != expectedMode =>
        throw new IOException("You can only use WRITING primitives with this file")
      case _ => // Do nothing!
    }
  }

  private def throwNoOpenFile(): Nothing =
    throw new IOException(I18N.errors.get("org.nlogo.workspace.DefaultFileManager.noOpenFile"))

  private def closeFile(file: File): Unit = {
    openFiles -= file.getAbsolutePath
    file.close(true)
  }

  private def relativeToAbsolute(newPath: String): String =
    try new JFile(s"${prefix}$separatorChar$newPath").getCanonicalPath
    catch {
      case ex: IOException => throw new IllegalStateException(ex)
    }

  private class BufferedFileCharIterator(file: File) extends BufferedIterator[Char] {

    private val buffReader: BufferedReader = file.reader

    private def nextChar(reset: Boolean = true): Int = {
      buffReader.mark(1)
      val i = buffReader.read()
      if (reset)
        buffReader.reset()
      else
        file.pos += 1
      i
    }

    override def head:    Char    = nextChar().asInstanceOf[Char]
    override def hasNext: Boolean = nextChar() != -1
    override def next():  Char    = nextChar(false).asInstanceOf[Char]

  }
}
