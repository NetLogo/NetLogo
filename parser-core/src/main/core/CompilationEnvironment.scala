// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

sealed abstract trait ResolvedModule

case class FileModule(prefix: String, path: String, paths: Seq[String]) extends ResolvedModule
case class BundledModule(path: String, files: Seq[ResolvedFile]) extends ResolvedModule
case class ResolvedFile(path: String, relative: String, source: String)

trait CompilationEnvironment {
  def profilingEnabled: Boolean
  def resolvePath(path: String): String
  def resolveModulePath(currentFile:Option[String], pathComponents: Seq[String]): Option[ResolvedModule]
  def exists(path: String): Boolean
  def getSource(filename: String): String
  def findBundledInclude(path: String): Option[String]
  def findBundledImport(path: Seq[String]): Option[ResolvedModule]
}

class DummyCompilationEnvironment extends CompilationEnvironment {
  def profilingEnabled = false
  def resolvePath(filename: String): String = filename
  def resolveModulePath(currentFile: Option[String], modulePath: Seq[String]) = None
  def exists(filename: String): Boolean = false
  def getSource(filename: String): String =
    if (filename == "aggregate") "" else throw new UnsupportedOperationException
  def findBundledInclude(path: String): Option[String] = None
  def findBundledImport(path: Seq[String]): Option[ResolvedModule] = None
}
