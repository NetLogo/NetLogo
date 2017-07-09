// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.CompilationEnvironment

import scala.util.Try

object IncludeFile {
  def apply(compilationEnvironment: CompilationEnvironment, suppliedPath: String): Option[(String, String)] = {
    val resolvedPath = compilationEnvironment.resolvePath(suppliedPath)
    Try(compilationEnvironment.getSource(resolvedPath)).toOption.map(fileText => (resolvedPath, fileText))
  }
}
