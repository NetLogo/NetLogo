// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

import org.picocontainer.{ DefaultPicoContainer, Parameter }
import org.picocontainer.behaviors.Caching

class Pico extends DefaultPicoContainer(new Caching)
{
  def add(name:String): Unit = {
    addComponent(Class.forName(name))
  }
  def add(key: Class[_], name: String, parameters: Array[Parameter]): Unit = {
    addComponent(key, Class.forName(name), parameters:_*)
  }
  def add(key: Class[_], name: String, parameters: Parameter*): Unit = {
    addComponent(key, Class.forName(name), parameters:_*)
  }
  def add(key: Class[_], name: String): Unit = {
    addComponent(key, Class.forName(name))
  }
  def addScalaObject(name:String): Unit = {
    addComponent(Class.forName(name + "$").getField("MODULE$").get(null))
  }
}
