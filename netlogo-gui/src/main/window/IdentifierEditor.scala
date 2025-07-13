// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.{ CompilerServices, ExtensionManager }

class IdentifierEditor(accessor: PropertyAccessor[String], compiler: CompilerServices,
                       extensionManager: ExtensionManager) extends StringEditor(accessor) {
  override def get: Option[String] =
    super.get.map(_.trim).filter(s => compiler.isValidIdentifier(s, extensionManager))
}
