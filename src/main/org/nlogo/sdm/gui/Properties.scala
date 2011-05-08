package org.nlogo.sdm.gui

// Property is more convenient to use from Scala than Java, thanks to named and default arguments,
// so we put all of our Properties for this package here - ST 2/23/10

import org.nlogo.api.{Property => P}
import org.nlogo.util.JCL.JavaList

object Properties {
  val converter = JavaList(
    P("nameWrapper", P.Identifier, "Name"),
    P("expressionWrapper", P.Reporter, "Expression")
  )
  val stock = JavaList(
    P("nameWrapper", P.Identifier, "Name"),
    P("initialValueExpressionWrapper", P.Reporter, "Initial value"),
    P("allowNegative", P.Boolean, "Allow negative values")
  )
  val rate = JavaList(
    P("nameWrapper", P.Identifier, "Name"),
    P("expressionWrapper", P.Reporter, "Expression")
  )
}
