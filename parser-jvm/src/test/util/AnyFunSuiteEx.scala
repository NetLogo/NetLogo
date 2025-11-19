// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

import org.scalatest.{ Failed, Outcome, Succeeded }
import org.scalatest.funsuite.AnyFunSuite

// this wrapper trait provides a mechanism for ensuring that exceptions in stray threads
// result in a failed test, when they would otherwise silently print to stdout (Isaac B 11/18/25)
trait AnyFunSuiteEx extends AnyFunSuite {
  private var outcome: Outcome = Succeeded

  Thread.setDefaultUncaughtExceptionHandler { (_, t) =>
    if (outcome == Succeeded)
      outcome = Failed(t)
  }

  override def withFixture(test: NoArgTest): Outcome = {
    outcome = Succeeded

    super.withFixture(test) match {
      case Succeeded => outcome
      case o => o
    }
  }
}
