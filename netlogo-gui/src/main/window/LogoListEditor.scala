// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.CompilerServices
import org.nlogo.core.{ CompilerException, LogoList, Nobody }
import org.nlogo.editor.Colorizer

class LogoListEditor(accessor: PropertyAccessor[String], compiler: CompilerServices, colorizer: Colorizer)
  extends CodeEditor(accessor, colorizer) {

  private def nobodyFree(a: AnyRef): Boolean = {
    a match {
      case Nobody => false
      case list: LogoList => list.forall(nobodyFree)
      case _ => true
    }
  }

  override def get: Option[String] = {
    super.get.filter { code =>
      try {
        compiler.readFromString(s"[ $code ]") match {
          case list: LogoList => list.nonEmpty && list.forall(nobodyFree)
          case _ => false
        }
      } catch {
        case _: CompilerException => false
      }
    }
  }
}
