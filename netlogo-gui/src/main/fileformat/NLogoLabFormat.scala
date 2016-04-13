// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.core.Model
import org.nlogo.lab.Protocol
import org.nlogo.api.ComponentSerialization

class NLogoLabSerialization(autoConvert: String => String)
  extends ComponentSerialization[Array[String], NLogoFormat] {
  def componentName = "org.nlogo.modelsection.behaviorspace"

  override def addDefault = identity
    // manager

  def serialize(m: Model): Array[String] = Array()
    // ProtocolSaver.save(c.getComponent).lines.toArray

  def validationErrors(m: Model) =
    None

  override def deserialize(s: Array[String]) = {(m: Model) =>
    /*
    def autoConvertProtocol(protocol:Protocol):Protocol = {
      import protocol._
      new Protocol(name,
                   autoConvert(setupCommands),
                   autoConvert(goCommands),
                   autoConvert(finalCommands),
                   repetitions, runMetricsEveryStep, timeLimit,
                   autoConvert(exitCondition),
                   metrics.map(autoConvert),
                   valueSets)
    }
    loader.loadAll(s.mkString("\n")).map(autoConvertProtocol).foreach(manager.addProtocol)
    manager
    */
   m
  }
}
