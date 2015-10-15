// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.ExtensionManager

import scala.io.Source
import scala.util.Try

object IncludeFile {
  def apply(extensionManager: ExtensionManager, suppliedPath: String): Option[(String, String)] = {
    val resolvedPath = extensionManager.resolvePath(suppliedPath)
    Try(Source.fromFile(resolvedPath).mkString).toOption.map(fileText => (resolvedPath, fileText))
  }
}
