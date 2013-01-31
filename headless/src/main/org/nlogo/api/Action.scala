// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import scala.collection.mutable

trait Action

trait ActionRunner[A <: Action] {
  def run(action: A): Unit
}

trait ActionBroker[A <: Action]
  extends mutable.Publisher[A] {
  val runner: ActionRunner[A]
  override def publish(action: A) {
    super.publish(action)
    runner.run(action)
  }
}
