// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.CompilerServices

class IdentifierEditor(accessor: PropertyAccessor[String], compiler: CompilerServices) extends StringEditor(accessor) {
  override def get: Option[String] =
    super.get.map(_.trim).filter(compiler.isValidIdentifier)
}
