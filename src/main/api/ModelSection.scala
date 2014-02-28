// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

abstract sealed trait ModelSection

object ModelSection {
  case object Code extends ModelSection
  case object Interface extends ModelSection
  case object Info extends ModelSection
  case object TurtleShapes extends ModelSection
  case object Version extends ModelSection
  case object PreviewCommands extends ModelSection
  case object SystemDynamics extends ModelSection
  case object BehaviorSpace extends ModelSection
  case object HubNetClient extends ModelSection
  case object LinkShapes extends ModelSection
  case object ModelSettings extends ModelSection
  case object DeltaTick extends ModelSection

  def allSections =
    Seq(Code, Interface, Info, TurtleShapes, Version, PreviewCommands, SystemDynamics,
        BehaviorSpace, HubNetClient, LinkShapes, ModelSettings, DeltaTick)
}

object ModelSectionJ {
  import ModelSection._
  // (I don't think) Java can access the inner objects without reflection, so we provide these
  // convenience vals for use from the handful of Java clients we still have.  and let them
  // use the old section names too - ST 7/8/11
  val SOURCE = Code
  val WIDGETS = Interface
  val INFO = Info
  val SHAPES = TurtleShapes
  val VERSION = Version
  val PREVIEW_COMMANDS = PreviewCommands
  val AGGREGATE = SystemDynamics
  val EXPERIMENTS = BehaviorSpace
  val CLIENT = HubNetClient
  val LINK_SHAPES = LinkShapes
  val MODEL_SETTINGS = ModelSettings
  val DELTA_TICK = DeltaTick
}
