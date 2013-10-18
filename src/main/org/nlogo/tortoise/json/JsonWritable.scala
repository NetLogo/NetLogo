// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise.json

import org.json4s._

trait JsonWritable {
  def toJsonObj: JValue
  def toJson:    String = toJsonObj.toString
}

trait JsonConverter[T] extends JsonWritable {

  protected def target: T
  protected def extraProps: JObject
  protected def baseProps : JObject = JObject(List())

  final override def toJsonObj: JObject = extraProps merge baseProps

}

