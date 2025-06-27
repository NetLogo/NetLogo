// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

private case class Choice[T](name: String, value: T)

class Options[T] {
  protected var choices = Seq[Choice[T]]()
  protected var current: Option[Choice[T]] = None

  def chosenName = current.get.name
  def chosenValue = current.get.value

  def addOption(name: String, value: T): Unit = {
    choices = choices :+ Choice(name, value)
  }

  def names: Seq[String] =
    choices.map(_.name)

  def values: Seq[T] =
    choices.map(_.value)

  def selectByName(s: String): Unit = {
    current = choices.find(_.name == s)
  }

  def selectValue(obj: T): Unit = {
    current = choices.find(_.value == obj)
  }

  override def equals(other: Any): Boolean = {
    other match {
      case opts: Options[_] => choices == opts.choices && current == opts.current
      case _ => false
    }
  }
}
