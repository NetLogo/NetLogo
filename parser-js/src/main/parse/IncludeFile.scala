// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ CompilationEnvironment, ExtensionManager }

object IncludeFile {
  def apply(compilationEnvironment: CompilationEnvironment, suppliedPath: String) =
    Option.empty[(String, String)]
}
