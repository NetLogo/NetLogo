// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.io.Writer
import java.net.URI

import org.nlogo.api.{ ComponentSerialization, LabProtocol, ModelFormat }
import org.nlogo.core.Model

import scala.util.{ Failure, Success, Try }

class MockFormat(val model: Model, error: Option[Exception]) extends ModelFormat[String, MockFormat] {
  type Section = String
  def name: String = if(model.version.contains("3D")) "nlogox3d" else "nlogox"
  def isCompatible(source: String) = true
  def isCompatible(uri: java.net.URI) = true
  def isCompatible(model: Model) = true
  override def baseModel = model
  def sections(location: java.net.URI): Try[Map[String, String]] =
    error.map(Failure.apply).getOrElse(Success(Map[String, String]()))
  def sectionsFromSource(source: String): Try[Map[String, Section]] =
    error.map(Failure.apply).getOrElse(Success(Map[String, String]()))
  def sectionsToSource(sections: Map[String, Section]): Try[Section] =
    Failure(new UnsupportedOperationException("MockFormat doesn't support this operation"))
  def writeSections(sections: Map[String, String], location: URI): Try[URI] =
    error.map(Failure.apply).getOrElse(Success(location))
  override def readExperiments(source: String, editNames: Boolean, existingNames: Set[String]): Try[(Seq[LabProtocol], Set[String])] =
    Failure(new UnsupportedOperationException("MockFormat doesn't support this operation"))
  override def writeExperiments(experiments: Seq[LabProtocol], writer: Writer): Try[Unit] =
    Failure(new UnsupportedOperationException("MockFormat doesn't support this operation"))
  object DefaultSerialization extends ComponentSerialization[String, MockFormat] {
    def componentName: String = "org.nlogo.modelsection.code"
    def serialize(m: Model): String = ""
    def validationErrors(m: Model): Option[String] = None
  }
  def codeComponent = DefaultSerialization
  def infoComponent = DefaultSerialization
  def interfaceComponent = DefaultSerialization
  def shapesComponent = DefaultSerialization
  def linkShapesComponent = DefaultSerialization
  def version = DefaultSerialization
}
