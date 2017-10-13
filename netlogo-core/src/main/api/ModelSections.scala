// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import
  org.nlogo.core.{ Model, Shape, Widget },
    Shape.{ LinkShape, VectorShape }

import
  ModelSections._

@deprecated("ModelSections should no longer be used", "6.1.0")
private[nlogo] trait ModelSections {
  def procedureSource:    String
  def widgets:            Seq[Widget]
  def info:               String
  def turtleShapes:       Seq[VectorShape]
  def linkShapes:         Seq[LinkShape]
  def additionalSections: Seq[ModelSaveable]
  def version:            String
}

private[nlogo] object ModelSections {
  trait ModelSaveable {
    def updateModel(m: Model): Model
  }
}
