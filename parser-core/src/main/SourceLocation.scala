// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

case class SourceLocation(start: Int, end: Int, filename: String)

trait SourceLocatable {
  def sourceLocation: SourceLocation
  def start:    Int    = sourceLocation.start
  def end:      Int    = sourceLocation.end
  def filename: String = sourceLocation.filename
}

trait SourceRelocatable[T] extends SourceLocatable {
  def changeLocation(newLocation: SourceLocation): T
}
