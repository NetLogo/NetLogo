// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

case class ResolvedModule(prefix: String, path: String, paths: Seq[String])

trait CompilationEnvironment {
  def profilingEnabled: Boolean
  def resolvePath(path: String): String
  def resolveModulePath(currentFile:Option[String], pathComponents: Seq[String]): Option[ResolvedModule]
  def exists(path: String): Boolean
  def getSource(filename: String): String
}

class DummyCompilationEnvironment extends CompilationEnvironment {
  def profilingEnabled = false
  def resolvePath(filename: String): String = filename
  def resolveModulePath(currentFile: Option[String], modulePath: Seq[String]) = None
  def exists(filename: String): Boolean = false
  def getSource(filename: String): String =
    if (filename == "aggregate") "" else throw new UnsupportedOperationException
}
