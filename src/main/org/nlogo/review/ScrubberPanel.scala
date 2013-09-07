// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import java.awt.BorderLayout
import java.beans.{ PropertyChangeEvent, PropertyChangeListener }
import org.nlogo.api
import javax.swing.{ JButton, JLabel, JPanel, JSlider }
import javax.swing.event.{ ChangeEvent, ChangeListener }
import scala.math.BigDecimal
import scala.math.BigDecimal.RoundingMode
import javax.swing.event.ListSelectionListener
import javax.swing.event.ListSelectionEvent
import scala.collection.mutable.Publisher
import scala.collection.mutable.Subscriber
import java.awt.Dimension
import javax.swing.SwingConstants

class ScrubberPanel(
  indexedNotesTable: IndexedNotesTable,
  currentFrame: () => Option[Int],
  currentTick: () => Option[Double],
  reviewTabStatePub: ReviewTabState#Pub,
  runRecorderPub: RunRecorder#Pub)
  extends JPanel {

  val scrubber = new Scrubber(indexedNotesTable, reviewTabStatePub, runRecorderPub)
  val tickPanel = new TickPanel(currentFrame, currentTick, scrubber)
  val scrubberButtonsPanel = new ScrubberButtonsPanel(scrubber)

  setLayout(new BorderLayout)
  add(tickPanel, BorderLayout.WEST)
  add(scrubber, BorderLayout.CENTER)
  add(scrubberButtonsPanel, BorderLayout.EAST)
}

class Scrubber(
  indexedNotesTable: IndexedNotesTable,
  reviewTabStatePub: ReviewTabState#Pub,
  runRecorderPub: RunRecorder#Pub)
  extends JSlider {

  setValue(0)
  setEnabled(false)
  reviewTabStatePub.subscribe(ReviewTabStateSub)
  runRecorderPub.subscribe(RunRecorderSub)

  object ReviewTabStateSub extends ReviewTabState#Sub {
    override def notify(pub: ReviewTabState#Pub, event: CurrentRunChangeEvent) {
      event match {
        case AfterCurrentRunChangeEvent(_, newRun) =>
          setValue(newRun.flatMap(_.currentFrameIndex).getOrElse(0))
          setMaximum(newRun.flatMap(_.lastFrameIndex).getOrElse(0))
          setEnabled(newRun.filter(_.size > 1).isDefined)
        case _ =>
      }
    }
  }

  object RunRecorderSub extends RunRecorder#Sub {
    override def notify(pub: RunRecorder#Pub, event: RunRecorderEvent) {
      event match {
        case FrameAddedEvent(run, _) =>
          run.lastFrameIndex.foreach { i =>
            setMaximum(i)
            setEnabled(i > 0)
          }
        case _ =>
      }
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
