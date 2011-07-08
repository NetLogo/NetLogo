package org.nlogo.api

abstract sealed trait ModelSection

object ModelSection {
  case object Source extends ModelSection
  case object Widgets extends ModelSection
  case object Info extends ModelSection
  case object Shapes extends ModelSection
  case object Version extends ModelSection
  case object PreviewCommands extends ModelSection
  case object Aggregate extends ModelSection
  case object Experiments extends ModelSection
  case object Client extends ModelSection
  case object LinkShapes extends ModelSection
  case object ModelSettings extends ModelSection
  case object DeltaTick extends ModelSection

  def allSections = Seq(Source, Widgets, Info, Shapes, Version, PreviewCommands, Aggregate,
    Experiments, Client, LinkShapes, ModelSettings, DeltaTick)
}

object ModelSectionJ {
  import ModelSection._
  // (I don't think) Java can access the inner objects without reflection,
  // so we provide these convenience vals for use from the handful of Java
  // clients we still have - ST 7/8/11
  val SOURCE = Source
  val WIDGETS = Widgets
  val INFO = Info
  val SHAPES = Shapes
  val VERSION = Version
  val PREVIEW_COMMANDS = PreviewCommands
  val AGGREGATE = Aggregate
  val EXPERIMENTS = Experiments
  val CLIENT = Client
  val LINK_SHAPES = LinkShapes
  val MODEL_SETTINGS = ModelSettings
  val DELTA_TICK = DeltaTick
}
