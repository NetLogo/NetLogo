// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import org.nlogo.mirror.ModelRun
import javax.swing.{ BorderFactory, JList, ListSelectionModel }
import javax.swing.event.{ ListSelectionEvent, ListSelectionListener }
import org.nlogo.awt.UserCancelException

class RunList(reviewTab: ReviewTab)
  extends JList(reviewTab.state)
  with ReviewTabState#Sub {

  def selectedRun: Option[ModelRun] =
    if (getSelectedIndex == -1) None
    else Some(getSelectedValue.asInstanceOf[ModelRun])

  reviewTab.state.subscribe(this)

  setBorder(BorderFactory.createLoweredBevelBorder())
  setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

  override def setSelectionInterval(anchor: Int, lead: Int) {
    for (run <- reviewTab.state.runs.lift(anchor)) {
      try {
        reviewTab.loadModelIfNeeded(run.modelString)
        reviewTab.state.currentRun = Some(run)
        super.setSelectionInterval(anchor, lead)
      } catch {
        case _: UserCancelException => // do nothing
        case e: Exception           => throw e // rethrow anything else
      }
    }
  }

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
