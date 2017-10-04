package org.nlogo.workspace

import java.nio.file.Files

class DummyWorkspace extends ExtendableWorkspace {
  val dummyFileManager = new DummyFileManager()

  var _isTesting = false
  def setTesting(t: Boolean) = { _isTesting = t }
  def attachModelDir(fileName: String): String =
    dummyFileManager.attachModelDir(fileName)
  def compilerTestingMode: Boolean = _isTesting
  val fileManager: org.nlogo.nvm.FileManager = dummyFileManager
  def getSource(x$1: String): String = "abcdef"
  def profilingEnabled: Boolean = ???
  def readFromString(s: String): Object = s
  def warningMessage(x$1: String): Boolean = ???
  def setProfilingTracer(tracer: org.nlogo.nvm.Tracer) = ???
}

class DummyFileManager extends org.nlogo.nvm.FileManager with ModelTracker {
  val tempdir = Files.createTempDirectory("modeldir")
  val extDir = new java.io.File(tempdir.toFile, "extensions")
  extDir.mkdir()
  val fooExt = new java.io.File(extDir, "foo")
  fooExt.mkdir()
  val foobarFile = new java.io.File(tempdir.toFile, "foobar")
  foobarFile.createNewFile()

  override def attachModelDir(f: String): String =
    new java.io.File(tempdir.toFile, f).getAbsolutePath

  val dummyFile = new org.nlogo.core.File {
    def close(ok: Boolean): Unit = ???
    def flush(): Unit = ???
    def getAbsolutePath: String = ???
    def getInputStream: java.io.InputStream = ???
    def getPath: String = ???
    def getPrintWriter: java.io.PrintWriter = ???
    def open(mode: org.nlogo.core.FileMode): Unit = ???
    def print(str: String): Unit = ???
    def println(): Unit = ???
    def println(line: String): Unit = ???
  }

  def attachPrefix(x$1: String): String = ???
  def closeAllFiles(): Unit = ???
  def closeCurrentFile(): Unit = ???
  def currentFile: Option[org.nlogo.core.File] = ???
  def deleteFile(x$1: String): Unit = ???
  def ensureMode(x$1: org.nlogo.core.FileMode): Unit = ???
  def eof: Boolean = ???
  def fileExists(x$1: String): Boolean = ???
  def findOpenFile(x$1: String): Option[org.nlogo.core.File] = ???
  def flushCurrentFile(): Unit = ???
  def getErrorInfo: String = ???
  def getFile(x$1: String): org.nlogo.core.File = dummyFile
  def prefix: String = ???
  def hasCurrentFile: Boolean = ???
  def openFile(x$1: String): Unit = ???
  def read(x$1: org.nlogo.agent.World): Object = ???
  def readChars(x$1: Int): String = ???
  def readLine(): String = ???
  def setPrefix(x$1: java.net.URL): Unit = ???
  def setPrefix(x$1: String): Unit = ???
  def handleModelChange(): Unit = ???
  def writeOutputObject(x$1: org.nlogo.agent.OutputObject): Unit = ???
}
