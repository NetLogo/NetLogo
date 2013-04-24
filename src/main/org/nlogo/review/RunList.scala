// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import org.nlogo.mirror.ModelRun

import javax.swing.{ BorderFactory, JList, ListSelectionModel }
import javax.swing.event.{ ListSelectionEvent, ListSelectionListener }

class RunList(reviewTab: ReviewTab)
  extends JList(reviewTab.state)
  with ReviewTabState#Sub {

  def selectedRun: Option[ModelRun] =
    if (getSelectedIndex == -1) None
    else Some(getSelectedValue.asInstanceOf[ModelRun])

  reviewTab.state.subscribe(this)

  setBorder(BorderFactory.createLoweredBevelBorder())
  setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
  this.getSelectionModel.addListSelectionListener(
    new ListSelectionListener {
      def valueChanged(p1: ListSelectionEvent) {
        for (run <- selectedRun) {
          reviewTab.state.currentRun = Some(run)
          reviewTab.loadModelIfNeeded(run.modelString)
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
