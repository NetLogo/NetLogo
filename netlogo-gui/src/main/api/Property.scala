// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.LogoList
import java.awt.GridBagConstraints

object Property {
  sealed abstract class Type
  case object AgentOptions extends Type
  case object BigString extends Type
  case object Boolean extends Type
  case object Color extends Type
  case object Commands extends Type
  case object Double extends Type
  case object Error extends Type
  case object Identifier extends Type
  case object InputBoxOptions extends Type
  case object Integer extends Type
  case object Key extends Type
  case object LogoListString extends Type
  case object NegativeInteger extends Type
  case object NonEmptyString extends Type
  case object PlotOptions extends Type
  case object PlotPens extends Type
  case object StrictlyPositiveDouble extends Type
  case object PositiveInteger extends Type
  case object Reporter extends Type
  case object ReporterOrEmpty extends Type
  case object ReporterLine extends Type
  case object String extends Type
}

case class Property(accessString: String, tpe: Property.Type, name: String,
                    notes: String = "",
                    gridWidth: Int = GridBagConstraints.REMAINDER,
                    focus: Boolean = false,
                    setLive: Boolean = false,
                    enabled: Boolean = true,
                    collapsible: Boolean = false,
                    collapseByDefault: Boolean = false )
