// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.lang.{ Boolean => JBoolean }
import java.awt.Dimension
import java.beans.{ PropertyChangeEvent, PropertyChangeListener }

import javax.swing.JLabel

import org.nlogo.api.{ Dump, Exceptions, NetLogoListener }
import org.nlogo.window.Events.{ AfterLoadEvent, PeriodicUpdateEvent, LoadBeginEvent }

object TickCounterLabel {
  private val TickCounterLabelDefault = "ticks"
}

import TickCounterLabel._

class TickCounterLabel
  extends JLabel
  with AfterLoadEvent.Handler
  with LoadBeginEvent.Handler
  with PeriodicUpdateEvent.Handler
  with NetLogoListener
  with PropertyChangeListener {
  private var _label: String = TickCounterLabelDefault

  private var lastTickSeen = -1

  override def getPreferredSize: Dimension = getMinimumSize

  override def getMinimumSize: Dimension = {
    val d = super.getMinimumSize
    val fontMetrics = getFontMetrics(getFont)
    d.width = StrictMath.max(d.width, fontMetrics.stringWidth(label + ": 00000000"))
    d
  }

  def handle(e: LoadBeginEvent): Unit = {
    setText("")
    _label = "ticks"
    lastTickSeen = -1
    setVisible(true)
  }

  def handle(e: AfterLoadEvent): Unit = {
    redrawTickCounter()
  }

  def handle(e: PeriodicUpdateEvent): Unit = {
    redrawTickCounter()
  }

  protected def redrawTickCounter(): Unit = {
    val tickText =
        if (lastTickSeen == -1) "" else Dump.number(StrictMath.floor(lastTickSeen))
    setText("     " + _label + ": " + tickText)
  }

  /// tick counter

  def visibility_=(visible: Boolean): Unit =
    setVisible(visible)

  def visibility: Boolean = isVisible

  def label_=(label: String): Unit = {
    _label = label
    redrawTickCounter()
  }

  def label: String = _label

  def tickCounterChanged(ticks: Double): Unit = {
    lastTickSeen = ticks.toInt
  }

  def propertyChange(evt: PropertyChangeEvent): Unit = {
    evt.getPropertyName match {
      case WorldViewSettings.TickCounterLabelProperty =>
        evt.getNewValue match {
          case s: String => label = s
          case other => Exceptions.ignore(new RuntimeException(s"Invalid value supplied for tickCounterLabel: $other"))
        }
      case WorldViewSettings.TickCounterVisibilityProperty =>
        evt.getNewValue match {
          case b: JBoolean => visibility = b.booleanValue
          case other => Exceptions.ignore(new RuntimeException(s"Invalid value supplied for tickCounterVisibility: $other"))
        }
      case _ =>
    }
  }

  def buttonPressed(buttonName: String): Unit = {}
  def buttonStopped(buttonName: String): Unit = {}
  def chooserChanged(name: String,value: AnyRef,valueChanged: Boolean): Unit = {}
  def codeTabCompiled(text: String,errorMsg: org.nlogo.core.CompilerException): Unit = {}
  def commandEntered(owner: String,text: String,agentType: Char,errorMsg: org.nlogo.core.CompilerException): Unit = {}
  def inputBoxChanged(name: String,value: AnyRef,valueChanged: Boolean): Unit = {}
  def modelOpened(name: String): Unit = {}
  def possibleViewUpdate(): Unit = {}
  def sliderChanged(name: String,value: Double,min: Double,increment: Double,max: Double,valueChanged: Boolean,buttonReleased: Boolean): Unit = {}
  def switchChanged(name: String,value: Boolean,valueChanged: Boolean): Unit = {}

}
