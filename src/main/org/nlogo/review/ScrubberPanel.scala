// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import java.awt.BorderLayout
import java.beans.{ PropertyChangeEvent, PropertyChangeListener }

import org.nlogo.api

import javax.swing.{ JButton, JLabel, JPanel, JSlider }
import javax.swing.event.{ ChangeEvent, ChangeListener }

class ScrubberPanel(
  currentFrame: () => Option[Int],
  currentTick: () => Option[Double])
  extends JPanel {

  val scrubber = new Scrubber
  val tickPanel = new TickPanel(currentFrame, currentTick, scrubber)
  val scrubberButtonsPanel = new ScrubberButtonsPanel(scrubber)

  setLayout(new BorderLayout)
  add(scrubberButtonsPanel, BorderLayout.WEST)
  add(scrubber, BorderLayout.CENTER)
  add(tickPanel, BorderLayout.EAST)
}

class Scrubber extends JSlider {
  setValue(0)
  def refresh(value: Int, max: Int, enabled: Boolean) {
    setValue(value)
    setMaximum(max)
    setEnabled(enabled)
    repaint()
  }
}

class ScrubberButtonsPanel(scrubber: JSlider) extends JPanel {
  val buttons: Seq[JButton] = Seq(
    new ScrubberButton("all-back", "Go to beginning of run", { _ => 0 }, scrubber),
    new ScrubberButton("big-back", "Go back five steps", { _ - 5 }, scrubber),
    new ScrubberButton("back", "Go back one step", { _ - 1 }, scrubber),
    new ScrubberButton("forward", "Go forward one step", { _ + 1 }, scrubber),
    new ScrubberButton("big-forward", "Go forward five steps", { _ + 5 }, scrubber),
    new ScrubberButton("all-forward", "Go to end of run", { _ => scrubber.getMaximum }, scrubber))
  setLayout(new org.nlogo.awt.RowLayout(
    1, java.awt.Component.LEFT_ALIGNMENT,
    java.awt.Component.CENTER_ALIGNMENT))
  buttons.foreach(add)
}

class ScrubberButton(name: String, tip: String, newValue: Int => Int, scrubber: JSlider)
  extends JButton {
  val icon = name
  val setNewValue = { () => scrubber.setValue(newValue(scrubber.getValue)) }
  setAction(new ReviewAction(tip, icon, setNewValue))
  setToolTipText(tip)
  setHideActionText(true)

  scrubber.addPropertyChangeListener("enabled", new PropertyChangeListener {
    def propertyChange(evt: PropertyChangeEvent) {
      setEnabled(scrubber.isEnabled)
    }
  })
}

class TickPanel(
  currentFrame: () => Option[Int],
  currentTick: () => Option[Double],
  scrubber: Scrubber)
  extends JPanel {

  add(new JLabel("Frame:"))
  val frame = new JLabel("-")
  val bold = frame.getFont.deriveFont(frame.getFont.getStyle | java.awt.Font.BOLD)
  frame.setFont(bold)
  add(frame)
  add(new JLabel("Tick:"))
  val tick = new JLabel("-")
  tick.setFont(bold)
  add(tick)

  scrubber.addChangeListener(new ChangeListener {
    def stateChanged(e: ChangeEvent) {
      frame.setText(currentFrame().map(_.toString).getOrElse("-"))
      tick.setText(currentTick().map(api.Dump.number).getOrElse("-"))
    }
  })
}
