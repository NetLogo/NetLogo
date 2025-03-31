// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.editor.Colorizer

class NonEmptyCodeEditor(accessor: PropertyAccessor[String], colorizer: Colorizer)
  extends CodeEditor(accessor, colorizer) {

  override def get: Option[String] =
    super.get.map(_.trim).filter(_.nonEmpty)
}
