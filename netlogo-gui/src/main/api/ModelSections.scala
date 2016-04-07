// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.{ Shape, Widget }

import
  ModelSections._

trait ModelSections {
  def procedureSource:  String
  def widgets:          Seq[Widget]
  def info:             String
  def turtleShapes:     Seq[Shape]
  def version:          String
  def previewCommands:  PreviewCommands
  def aggregateManager: Saveable
  def labManager:       Saveable
  def hubnetInterface:  Option[Seq[Widget]]
  def linkShapes:       Seq[Shape]
  def snapOn:           Boolean
}

object ModelSections {
  trait Saveable {
    def save: String
  }
  trait BufSaveable {
    def save(buf: StringBuilder)
  }
}
