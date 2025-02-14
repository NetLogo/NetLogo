// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

// Property is more convenient to use from Scala than Java, thanks to named and default arguments,
// so we put all of our Properties for this package here - ST 2/23/10

import org.nlogo.api.{ Property => P }
import org.nlogo.core.I18N

object Properties {
  implicit val i18nPrefix = I18N.Prefix("edit")

  val converter = Seq(
    P("nameWrapper", P.Identifier, "Name"),
    P("inputs", P.StringOptions, "Inputs", dependentPropertyNames = Set("expressionWrapper")),
    P("expressionWrapper", P.Reporter, "Expression")
  )

  val stock = Seq(
    P("nameWrapper", P.Identifier, "Name"),
    P("initialValueExpressionWrapper", P.Reporter, "Initial value"),
    P("allowNegative", P.Boolean, "Allow negative values")
  )

  val rate = Seq(
    P("nameWrapper", P.Identifier, "Name"),
    P("inputs", P.StringOptions, "Inputs", dependentPropertyNames = Set("expressionWrapper")),
    P("expressionWrapper", P.Reporter, "Expression")
  )

}
