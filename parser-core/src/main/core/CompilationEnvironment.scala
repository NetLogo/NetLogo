// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

trait CompilationEnvironment {
  def profilingEnabled: Boolean

  def resolvePath(path: String): String

  def getSource(filename: String): String
}

class DummyCompilationEnvironment extends CompilationEnvironment {
  def profilingEnabled = false
  def resolvePath(filename: String): String = filename
  def getSource(filename: String): String =
    if (filename == "aggregate") "" else throw new UnsupportedOperationException
}
