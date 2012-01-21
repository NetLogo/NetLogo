// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring

import java.lang.reflect.{ InvocationTargetException, Method }

object Overridable {
  def getOverrideIndex(variables: Array[String], varName: String): Int =
    variables.indexWhere(_ equalsIgnoreCase varName)
  def getOverrideIndex(agentType: AgentType, varName: String) =
    agentType match {
      case AgentType.Turtle =>
        TurtleData.getOverrideIndex(varName)
      case AgentType.Patch =>
        PatchData.getOverrideIndex(varName)
      case AgentType.Link =>
        LinkData.getOverrideIndex(varName)
      case AgentType.Observer =>
        throw new IllegalArgumentException
    }
}

abstract class Overridable {

  private var stack: List[(Method, AnyRef)] = Nil

  def getterName(varName: Int): String  // abstract
  def setterName(varName: Int): String  // abstract

  def set(variable: Int, value: Any) {
    val gName = getterName(variable)
    val getter = getClass.getMethod(gName)
    val sName = setterName(variable)
    val setter = getSetter(sName, value.getClass)
    // note that the setter for the old value might be different
    // than the setter for the new value ev 4/29/08
    val oldValue = getter.invoke(this)
    stack ::= (getSetter(sName, oldValue.getClass), oldValue)
    setter.invoke(this, value.asInstanceOf[AnyRef])
  }

  private def getSetter(methodName: String, clazz: Class[_]): Method = {
    // when a value type is passed as a parameter to a method that takes Any, it gets boxed, so
    // when we call getClass above, we get a boxed class.  but the setter method we're looking
    // for is unboxed, so when we do method lookup we need to substitute a Class object
    // representing the (not actually existing) unboxed class - ST 1/21/12
    val unboxedClass =
      if(clazz eq classOf[java.lang.Double])
        classOf[Double]
      else if(clazz eq classOf[java.lang.Integer])
        classOf[Int]
      else if(clazz eq classOf[java.lang.Boolean])
        classOf[Boolean]
      else
        clazz
    try getClass.getMethod(methodName, unboxedClass)
    catch {
      case ex: NoSuchMethodException =>
        // if we don't find a setter specific for this type look for an Object type setter ev 5/16/08
        getClass.getMethod(methodName, classOf[AnyRef])
    }
  }

  def rollback() {
    for((method, value) <- stack)
      method.invoke(this, value)
    stack = Nil
  }

}
