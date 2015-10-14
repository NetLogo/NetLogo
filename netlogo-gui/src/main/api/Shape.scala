// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

// so that org.nlogo.shape doesn't need to depend on org.nlogo.agent

trait Shape {
  def getName: String
  def setName(name: String)
}
