// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import org.nlogo.api.Editable
import java.lang.reflect.Method
import scala.reflect.{ classTag, ClassTag }

class PropertyAccessor[T : ClassTag](val target: Editable, val displayName: String, val accessString: String) {

  // We assume the getter and setter methods have the same name. - ST 3/14/08

  val getter: Method = target.getClass.getMethod(accessString)
  def erasure = classTag[T].runtimeClass

  val setter: Method =
    try target.getClass.getMethod(accessString, erasure)
    catch {
      case ex: NoSuchMethodException =>
        // didn't find Java setter, look for Scala setter
        target.getClass.getMethod(accessString + "_$eq", erasure)
    }

  def get: T = getter.invoke(target).asInstanceOf[T]
  def set(value: T) { setter.invoke(target, value.asInstanceOf[AnyRef]) }

  def error: Option[Exception] = {
    val e = target.error(accessString)
    if( e == null ) None else Some(e)
  }

}
