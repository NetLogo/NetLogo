// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import
  ModelSections._

trait ModelSections {
  def procedureSource:  String
  def widgets:          Seq[Saveable]
  def info:             String
  def turtleShapes:     Seq[Shape]
  def version:          String
  def previewCommands:  String
  def aggregateManager: Saveable
  def labManager:       Saveable
  def hubnetManager:    BufSaveable
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
