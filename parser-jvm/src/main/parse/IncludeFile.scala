// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.CompilationEnvironment

import scala.io.Source
import scala.util.Try

object IncludeFile {
  def apply(compilationEnvironment: CompilationEnvironment, suppliedPath: String): Option[(String, String)] = {
    val resolvedPath = compilationEnvironment.resolvePath(suppliedPath)
    Try(Source.fromFile(resolvedPath).mkString).toOption.map(fileText => (resolvedPath, fileText))
  }
}
