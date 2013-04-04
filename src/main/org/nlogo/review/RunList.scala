// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import org.nlogo.mirror.ModelRun

import javax.swing.{ BorderFactory, JList, ListSelectionModel }
import javax.swing.event.{ ListSelectionEvent, ListSelectionListener }

class RunList(reviewTab: ReviewTab) extends JList(reviewTab.tabState) {
  setBorder(BorderFactory.createLoweredBevelBorder())
  setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
  this.getSelectionModel.addListSelectionListener(
    new ListSelectionListener {
      def valueChanged(p1: ListSelectionEvent) {
        if (getSelectedIndex != -1) {
          val run = getSelectedValue.asInstanceOf[ModelRun]
          reviewTab.tabState.currentRun = Some(run)
          reviewTab.loadModelIfNeeded(run.modelString)
          reviewTab.refreshInterface()
        }
      }
    })
}
