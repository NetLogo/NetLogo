// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import scala.{ Option => _ }
import collection.mutable.ArrayBuffer

class Options[T] {

  private case class Option(name: String, value: T)

  private val choices = ArrayBuffer[Option]()
  private var current: scala.Option[Option] = None

  def chosenName = current.get.name
  def chosenValue = current.get.value

  def addOption(name: String, value: T) {
    choices += Option(name, value)
  }

  def names: List[String] =
    choices.map(_.name).toList

  def values: List[T] =
    choices.map(_.value).toList

  def selectByName(s: String) {
    current = choices.find(_.name == s)
  }

  def selectValue(obj: T) {
    current = choices.find(_.value == obj)
  }

}
