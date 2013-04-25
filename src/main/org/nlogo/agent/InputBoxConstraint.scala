// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.ValueConstraint

class InputBoxConstraint(var typeName: String, _defaultValue: AnyRef)
  extends ValueConstraint
{
  var defaultValue = coerceValue(_defaultValue)
  def setType(typeName: String, defaultValue: AnyRef) {
    this.typeName = typeName
    this.defaultValue = coerceValue(defaultValue)
  }
  def correctType(obj: AnyRef): Boolean =
    typeName match {
      case "Number" | "Color" => obj.isInstanceOf[java.lang.Double]
      case _ => obj.isInstanceOf[String]
    }
  @throws(classOf[ValueConstraint.Violation])
  def assertConstraint(value: AnyRef) {
    if(!correctType(value))
      throw new ValueConstraint.Violation("You can't set this to " + value)
  }
  def coerceValue(value: AnyRef): AnyRef =
    if(correctType(value)) value
    else defaultValue
}
