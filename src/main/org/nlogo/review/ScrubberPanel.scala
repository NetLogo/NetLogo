// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import java.awt.BorderLayout
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSlider
import javax.swing.SwingConstants
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class ScrubberPanel(
  indexedNotesTable: IndexedNotesTable,
  currentFrame: () => Option[Int],
  currentTick: () => Option[Double],
  afterRunChangePub: SimplePublisher[AfterRunChangeEvent],
  frameAddedPub: SimplePublisher[FrameAddedEvent])
  extends JPanel {

  val scrubber = new Scrubber(indexedNotesTable, afterRunChangePub, frameAddedPub)
  val tickPanel = new TickPanel(currentFrame, currentTick, scrubber)
  val scrubberButtonsPanel = new ScrubberButtonsPanel(scrubber)

  setLayout(new BorderLayout)
  add(tickPanel, BorderLayout.WEST)
  add(scrubber, BorderLayout.CENTER)
  add(scrubberButtonsPanel, BorderLayout.EAST)
}

class Scrubber(
  indexedNotesTable: IndexedNotesTable,
  afterRunChangePub: SimplePublisher[AfterRunChangeEvent],
  frameAddedPub: SimplePublisher[FrameAddedEvent])
  extends JSlider {

  setValue(0)
  setEnabled(false)

  afterRunChangePub.newSubscriber { event =>
    setValue(event.newRun.flatMap(_.currentFrameIndex).getOrElse(0))
    setMaximum(event.newRun.flatMap(_.lastFrameIndex).getOrElse(0))
    setEnabled(event.newRun.filter(_.size > 1).isDefined)
  }

  frameAddedPub.newSubscriber {
    _.run.lastFrameIndex.foreach { i =>
      setMaximum(i)
      setEnabled(i > 0)
    }
  }

  // Synchronize the scrubber with the indexed notes
  indexedNotesTable.getSelectionModel.addListSelectionListener(
    new ListSelectionListener {
      override def valueChanged(event: ListSelectionEvent) {
        if (!event.getValueIsAdjusting) {
          val i = indexedNotesTable.getSelectionModel.getMinSelectionIndex
          if (i != -1) {
            val note = indexedNotesTable.model.notes(i)
            setValue(note.frame)
          }
        }
      }
    })
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
  setEnabled(false)

  scrubber.addPropertyChangeListener("enabled", new PropertyChangeListener {
    def propertyChange(evt: PropertyChangeEvent) {
      setEnabled(scrubber.isEnabled)
    }
  })
}

class TickPanelLabel(sizeTemplate: String) extends JLabel(sizeTemplate) {
  setPreferredSize(getPreferredSize) // fix to size of template...
  setText("-") // ...but start with "-"
  setFont(getFont.deriveFont(getFont.getStyle | java.awt.Font.BOLD))
  setHorizontalAlignment(SwingConstants.CENTER)
}

class TickPanel(
  currentFrame: () => Option[Int],
  currentTicks: () => Option[Double],
  scrubber: Scrubber)
  extends JPanel {

  add(new JLabel("Frame:"))
  val frame = new TickPanelLabel("999999")
  add(frame)
  add(new JLabel("Ticks:"))
  val tick = new TickPanelLabel("999999.99")
  add(tick)

  scrubber.addChangeListener(new ChangeListener {
    def stateChanged(e: ChangeEvent) {
      frame.setText(currentFrame().map(_.toString).getOrElse("-"))
      tick.setText(currentTicks().map("%.2f".format(_)).getOrElse("-")) // TODO be smarter about decimals?
    }
  })
}
