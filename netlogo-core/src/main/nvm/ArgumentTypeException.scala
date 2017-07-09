// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.{ core, api },
  core.{Nobody, TypeNames},
  api.{ Context => ApiContext, Dump }

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
      case c: Class[_] =>
        Some(TypeNames.aName(api.TypeNames.getTypeConstant(c)))
      case Nobody =>
        Some("NOBODY")
      case null =>
        None
      case _ =>
        Some("the " + api.TypeNames.name(badValue) + " " + Dump.logoObject(badValue, true, false))
    }
}

import ArgumentTypeException._

class ArgumentTypeException(context: Context, baseInstruction: Instruction, badArgIndex: Int, wantedType: Int, badValue: Object)
  extends RuntimePrimitiveException(context, baseInstruction, "bad message") { // buildMessage(baseInstruction, wantedType, badValue)) {
    override def computeRuntimeErrorMessage(ctx: ApiContext, instruction: Option[Instruction], cause: Option[Exception], defaultMessage: String) = {
      instruction.map(i => buildMessage(i, wantedType, badValue)).getOrElse(defaultMessage)
    }

    override def getMessage: String = runtimeErrorMessage
  }
