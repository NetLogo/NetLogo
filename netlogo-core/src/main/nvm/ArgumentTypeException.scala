// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api.{ Dump, TypeNames => ApiTypeNames }
import org.nlogo.core.{ Nobody, TypeNames}

object ArgumentTypeException {
  def buildMessage(instruction: Instruction, wantedType: Int, argumentValue: Object): String = {
    val result = new StringBuilder()

    result ++= s"${instruction.displayName} expected input to be ${TypeNames.aName(wantedType)}"

    // if badValue is a Class object, then it's not REALLY
    // a value at all -- it's just something to tell us what
    // kind of bad value was returned.
    result ++= (butGot(argumentValue) match {
      case Some(name) => s" but got $name instead."
      case None       => "."
    })

    result.toString
  }

  private def butGot(badValue: AnyRef): Option[String] =
    badValue match {
      // if badValue is a Class object, then it's not REALLY a value at all -- it's just something
      // to tell us what kind of bad value was returned.
      case c: Class[?] =>
        Some(TypeNames.aName(ApiTypeNames.getTypeConstant(c)))
      case Nobody =>
        Some("NOBODY")
      case null =>
        None
      case _ =>
        Some("the " + ApiTypeNames.name(badValue) + " " + Dump.logoObject(badValue, true, false))
    }
}

import ArgumentTypeException._

class ArgumentTypeException(context: Context, baseInstruction: Instruction, badArgIndex: Int, wantedType: Int, badValue: Object)
  extends RuntimePrimitiveException(context, baseInstruction, "bad message") {

  override def getMessage: String = {
    responsibleInstruction.map(i => buildMessage(i, wantedType, badValue)).getOrElse("Argument types were not as expected, but no further details can be found.")
  }

}
