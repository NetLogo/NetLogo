// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import org.nlogo.awt.UserCancelException
import org.nlogo.mirror.ModelRun

import javax.swing.BorderFactory
import javax.swing.JList
import javax.swing.ListSelectionModel
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

class RunList(reviewTab: ReviewTab)
  extends JList(reviewTab.state)
  with ReviewTabState#Sub {

  def selectedRun: Option[ModelRun] =
    if (getSelectedIndex == -1) None
    else Some(getSelectedValue.asInstanceOf[ModelRun])

  reviewTab.state.subscribe(this)

  setBorder(BorderFactory.createLoweredBevelBorder())
  setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

  addListSelectionListener(new ListSelectionListener {
    override def valueChanged(e: ListSelectionEvent) {
      if (!e.getValueIsAdjusting) {
        for (run <- reviewTab.state.runs.lift(getSelectedIndex)) {
          try {
            reviewTab.loadModelIfNeeded(run.modelString)
            reviewTab.state.currentRun = Some(run)
          } catch {
            case _: UserCancelException => // do nothing
            case e: Exception           => throw e // rethrow anything else
          }
        }
      }
    }
  })

  override def notify(pub: ReviewTabState#Pub, event: CurrentRunChangeEvent) {
    event match {
      case AfterCurrentRunChangeEvent(_, None) =>
        clearSelection()
      case AfterCurrentRunChangeEvent(_, Some(newRun)) if newRun != selectedRun =>
        setSelectedValue(newRun, true)
      case _ =>
    }
  }
}
