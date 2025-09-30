// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

trait CompilationEnvironment {
  def profilingEnabled: Boolean
  def resolvePath(path: String): String
  def resolveModule(currentFile:Option[String], packageName: Option[String], moduleName: String): String
  def exists(path: String): Boolean
  def getSource(filename: String): String
}

class DummyCompilationEnvironment extends CompilationEnvironment {
  def profilingEnabled = false
  def resolvePath(filename: String): String = filename
  def resolveModule(currentFile: Option[String], packageName: Option[String], moduleName: String): String = {
    val separator = System.getProperty("file.separator")

    packageName match {
      case Some(x) => x.toLowerCase + separator + moduleName.toLowerCase + ".nls"
      case None => moduleName.toLowerCase + ".nls"
    }
  }
  def exists(filename: String): Boolean = false
  def getSource(filename: String): String =
    if (filename == "aggregate") "" else throw new UnsupportedOperationException
}
