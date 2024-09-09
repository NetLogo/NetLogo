// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import
  org.nlogo.core.{ ExternalResource, Model, Shape, Widget },
    Shape.{ LinkShape, VectorShape }

import
  ModelSections._

private[nlogo] trait ModelSections {
  def procedureSource:    String
  def widgets:            Seq[Widget]
  def info:               String
  def turtleShapes:       Seq[VectorShape]
  def linkShapes:         Seq[LinkShape]
  def additionalSections: Seq[ModelSaveable]
  def openTempFiles:      Seq[String]
  def resources:          Seq[ExternalResource]
}

private[nlogo] object ModelSections {
  trait ModelSaveable {
    def updateModel(m: Model): Model
  }
}
