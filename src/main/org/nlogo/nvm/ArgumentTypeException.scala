// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api.{ Dump, Nobody, Syntax, TypeNames }

class ArgumentTypeException(context: Context, problemInstr: Instruction, badArgIndex: Int, wantedType: Int, badValue: AnyRef)
extends EngineException(context, problemInstr, "") { // message will be built later

  /**
   * this method should really only be called after the resolveErrorInstruction() method has been
   * called, otherwise it may give faulty results.  ~Forrest (10/24/2006)
   */
  override def getMessage =
    Option(instruction).map(_.displayName).getOrElse("") +
    " expected input to be " + TypeNames.aName(wantedType) +
    (butGot match {
      case Some(s) =>
        " but got " + s + " instead."
      case None =>
        "."})

  private def butGot: Option[String] =
    badValue match {
      // if badValue is a Class object, then it's not REALLY a value at all -- it's just something
      // to tell us what kind of bad value was returned.
      case c: Class[_] =>
        Some(TypeNames.aName(Syntax.getTypeConstant(c)))
      case Nobody =>
        Some("NOBODY")
      case null =>
        None
      case _ =>
        Some("the " + TypeNames.name(badValue) + " " + Dump.logoObject(badValue, true, false))
    }

}
