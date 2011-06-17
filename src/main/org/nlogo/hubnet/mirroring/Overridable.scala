package org.nlogo.hubnet.mirroring

import java.lang.reflect.{ InvocationTargetException, Method }

object Overridable {
  def getOverrideIndex(variables: Array[String], varName: String): Int =
    variables.indexWhere(_ equalsIgnoreCase varName)
}

abstract class Overridable {

  private var stack: List[(Method, AnyRef)] = Nil

  def getMethodName(varName: Int): String  // abstract

  def set(variable: Int, value: AnyRef) {
    val methodName = getMethodName(variable)
    val getter = getClass.getMethod(methodName)
    val setter = getSetter(methodName, value.getClass)
    // note that the setter for the old value might be different
    // than the setter for the new value ev 4/29/08
    val oldValue = getter.invoke(this)
    stack ::= (getSetter(methodName, oldValue.getClass), oldValue)
    setter.invoke(this, value)
  }

  private def getSetter(methodName: String, clazz: Class[_]): Method =
    try getClass.getMethod(methodName, clazz)
    catch {
      case ex: NoSuchMethodException =>
        // if we don't find a setter specific for this type look for an Object type setter ev 5/16/08
        getClass.getMethod(methodName, classOf[AnyRef])
    }

  def rollback() {
    for((method, value) <- stack)
      method.invoke(this, value)
    stack = Nil
  }

}
