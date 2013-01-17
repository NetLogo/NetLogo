// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import org.nlogo.{ nvm, prim }

object Prims {

  object InfixReporter {
    def unapply(r: nvm.Reporter): Option[String] =
      PartialFunction.condOpt(r) {
        case _: prim._plus     => "+"
        case _: prim._minus    => "-"
        case _: prim.etc._mult => "*"
        case _: prim.etc._div  => "/"
      }
  }

  object SpecialCommand {
    def unapply(c: nvm.Command): Option[String] =
      PartialFunction.condOpt(c) {
        case _: prim._return           => "return;"
        case _: prim._done             => ""
        case _: prim.etc._observercode => ""
      }
  }

  object NormalCommand {
    def unapply(c: nvm.Command): Option[String] =
      PartialFunction.condOpt(c) {
        case _: prim.etc._outputprint => "println"
      }
  }

}
