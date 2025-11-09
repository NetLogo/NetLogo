// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.util

import java.awt.EventQueue
import java.util.concurrent.TimeoutException

import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.{ Duration, SECONDS }

// various useful methods for automated GUI testing (Isaac B 11/8/25)
object AutomationUtils {
  def waitFor[T](function: () => T, seconds: Int = 5): Option[T] =
    timedFunction(function, seconds)

  def waitForGUI[T](function: () => T, seconds: Int = 5): Option[T] = {
    timedFunction(() => {
      var result: Option[T] = None

      EventQueue.invokeAndWait(() => {
        result = Option(function())
      })

      result
    }, seconds).flatten
  }

  def waitUntil(test: () => Boolean, seconds: Int = 5): Boolean = {
    timedFunction(() => {
      while (!test())
        Thread.sleep(250)
    }, seconds).isDefined
  }

  def waitUntilGUI(test: () => Boolean, seconds: Int = 5): Boolean = {
    timedFunction(() => {
      while {
        var result = false

        EventQueue.invokeAndWait(() => {
          result = test()
        })

        !result
      } do {
        Thread.sleep(250)
      }
    }, seconds).isDefined
  }

  private def timedFunction[T](function: () => T, seconds: Int): Option[T] = {
    try {
      Await.result(Future {
        Option(function())
      }(using ExecutionContext.global), Duration(seconds, SECONDS))
    } catch {
      case _: TimeoutException =>
        None
    }
  }
}
