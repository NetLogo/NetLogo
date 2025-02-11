// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Component, Dimension, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.{ MouseAdapter, MouseEvent }
import javax.swing.{ JLabel, JPanel }

import org.nlogo.app.codetab.TemporaryCodeTab
import org.nlogo.awt.UserCancelException
import org.nlogo.core.I18N
import org.nlogo.swing.{ CheckBox, CustomOptionPane, ScrollPane, Transparent }
import org.nlogo.theme.InterfaceColors

import scala.collection.mutable.Map

object OfferSaveExternalsDialog {
  def offer(dirtyExternalFiles: Set[TemporaryCodeTab], parent: Component) = {
    implicit val i18nPrefix = I18N.Prefix("file.save.offer.external")

    if (dirtyExternalFiles.nonEmpty) {
      val saveStatus = Map[TemporaryCodeTab, CheckBox]()

      val panel = new JPanel(new GridBagLayout) with Transparent {
        val c = new GridBagConstraints

        c.gridx = 0
        c.anchor = GridBagConstraints.WEST
        c.fill = GridBagConstraints.HORIZONTAL
        c.weightx = 1
        c.insets = new Insets(0, 0, 6, 0)

        add(new JLabel(I18N.gui("filesChanged")) {
          setForeground(InterfaceColors.dialogText)
        }, c)

        c.insets = new Insets(0, 0, 0, 0)

        val filesPanel = new JPanel(new GridBagLayout) with Transparent {
          val c = new GridBagConstraints

          c.gridy = 0

          dirtyExternalFiles.foreach(file => {
            c.gridx = 0
            c.fill = GridBagConstraints.NONE
            c.weightx = 0
            c.insets = new Insets(3, 3, 3, 6)

            val checkBox = new CheckBox

            saveStatus += ((file, checkBox))

            add(checkBox, c)

            c.gridx = 1
            c.fill = GridBagConstraints.HORIZONTAL
            c.weightx = 1
            c.insets = new Insets(3, 0, 3, 3)

            add(new JLabel(file.filename.merge) {
              setForeground(InterfaceColors.toolbarText)

              addMouseListener(new MouseAdapter {
                override def mouseClicked(e: MouseEvent) {
                  checkBox.doClick()
                }
              })
            }, c)

            c.gridy += 1
          })

          c.gridwidth = 2
          c.fill = GridBagConstraints.BOTH
          c.weighty = 1

          add(new JPanel with Transparent, c)
        }

        add(new ScrollPane(filesPanel) {
          setBackground(InterfaceColors.toolbarControlBackground)

          override def getPreferredSize: Dimension =
            new Dimension(super.getPreferredSize.width, 200)
        }, c)
      }

      new CustomOptionPane(parent, I18N.gui.get("common.netlogo"), panel,
                           List(I18N.gui("saveSelected"),
                                I18N.gui("discardAll"),
                                I18N.gui.get("common.buttons.cancel"))).getSelectedIndex match {
        case 0 =>
          dirtyExternalFiles.foreach(file => {
            if (saveStatus(file).isSelected)
              file.save(false)
          })
        case 1 =>
        case _ => throw new UserCancelException
      }
    }
  }
}
