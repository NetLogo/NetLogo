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
  extends JList[ModelRun](reviewTab.state) {

  def selectedRun: Option[ModelRun] =
    if (getSelectedIndex == -1) None
    else Some(getSelectedValue.asInstanceOf[ModelRun])

  setBorder(BorderFactory.createLoweredBevelBorder())
  setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

  addListSelectionListener(new ListSelectionListener {
    override def valueChanged(e: ListSelectionEvent) {
      if (!e.getValueIsAdjusting) {
        for (run <- reviewTab.state.runs.lift(getSelectedIndex)) {
          try {
            if (!reviewTab.isLoaded(run.modelString))
              reviewTab.loadModel(run.modelString)
            reviewTab.state.setCurrentRun(Some(run), true)
            requestFocusInWindow()
            reviewTab.interfacePanel.widgetPanels.foreach(_.revalidate())
            reviewTab.interfacePanel.repaint()
          } catch {
            case _: UserCancelException => // do nothing
            case e: Exception           => throw e // rethrow anything else
          }
        }
      }
    }
  })

  reviewTab.state.afterRunChangePub.newSubscriber {
    _.newRun match {
      case None                                  => clearSelection()
      case Some(newRun) if newRun != selectedRun => setSelectedValue(newRun, true)
    }
  }
}
