// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ FileDialog => AWTFileDialog, Frame, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.ActionEvent
import java.io.FileOutputStream
import java.nio.file.{ Files, Paths }
import java.util.Base64
import javax.swing.{ AbstractAction, JButton, JDialog, JList, JPanel, JScrollPane, ListSelectionModel }
import javax.swing.event.{ ListSelectionEvent, ListSelectionListener }

import org.nlogo.api.{ Workspace }
import org.nlogo.awt.{ Positioning, UserCancelException }
import org.nlogo.core.{ ExternalResource, I18N }
import org.nlogo.swing.{ FileDialog, InputDialog, OptionDialog }
import org.nlogo.window.Events.DirtyEvent

// sync with this theme and new GUI helper classes after integrating new GUI
class ResourceManagerDialog(parent: Frame, workspace: Workspace)
  extends JDialog(parent, I18N.gui.get("resource.manager"), true) {

  private val manager = workspace.getResourceManager
  private val resourceList = new JList(manager.getResources.map(_.name).toArray) {
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

    addListSelectionListener(new ListSelectionListener {
      def valueChanged(e: ListSelectionEvent) {
        enableButtons()
      }
    })
  }

  private val importButton = new JButton(new AbstractAction(I18N.gui.get("resource.import")) {
    def actionPerformed(e: ActionEvent) {
      try {
        val file = FileDialog.showFiles(parent, I18N.gui.get("resource.select"), AWTFileDialog.LOAD)
        val path = Paths.get(file)

        try {
          // use org.nlogo.swing.InputOptionPane after new GUI is integrated
          val name = new InputDialog(parent, I18N.gui.get("resource.name"), I18N.gui.get("resource.name"),
                                     I18N.gui.get, path.getFileName.toString.split('.')(0)).showInputDialog().trim

          if (name.isEmpty) {
            // use org.nlogo.swing.OptionPane after new GUI is integrated
            OptionDialog.showMessage(parent, I18N.gui.get("common.messages.error"), I18N.gui.get("resource.nameEmpty"),
                                     Array(I18N.gui.get("common.buttons.ok")))
          }

          else {
            val resource = new ExternalResource(name, file.split('.')(1),
                                                Base64.getEncoder.encodeToString(Files.readAllBytes(path)))

            if (manager.addResource(resource)) {
              refreshList()

              new DirtyEvent(None).raise(parent)
            }

            else {
              // use org.nlogo.swing.OptionPane after new GUI is integrated
              OptionDialog.showMessage(parent, I18N.gui.get("common.messages.error"),
                                      I18N.gui.getN("resource.alreadyExists", name),
                                      Array(I18N.gui.get("common.buttons.ok")))
            }
          }
        }

        catch {
          case t: Throwable => // this will be properly handled after switching to InputOptionPane
        }
      }

      catch {
        case e: UserCancelException =>
      }
    }
  })

  private val exportButton = new JButton(new AbstractAction(I18N.gui.get("resource.export")) {
    def actionPerformed(e: ActionEvent) {
      try {
        val resource = manager.getResource(resourceList.getSelectedValue).get
        val stream = new FileOutputStream(FileDialog.showFiles(parent, I18N.gui.get("resource.select"),
                                                               AWTFileDialog.SAVE,
                                                               s"${resource.name}.${resource.extension}"))

        stream.write(Base64.getDecoder.decode(resource.data))
        stream.close()
      }

      catch {
        case e: UserCancelException =>
      }
    }
  })

  private val renameButton = new JButton(new AbstractAction(I18N.gui.get("resource.rename")) {
    def actionPerformed(e: ActionEvent) {
      try {
        val resource = manager.getResource(resourceList.getSelectedValue).get

        // use org.nlogo.swing.InputOptionPane after new GUI is integrated
        val name = new InputDialog(parent, I18N.gui.get("resource.newName"), I18N.gui.get("resource.newName"),
                                  I18N.gui.get, resource.name).showInputDialog().trim

        if (name.isEmpty) {
          // use org.nlogo.swing.OptionPane after new GUI is integrated
          OptionDialog.showMessage(parent, I18N.gui.get("common.messages.error"), I18N.gui.get("resource.nameEmpty"),
                                   Array(I18N.gui.get("common.buttons.ok")))
        }

        else if (name != resource.name) {
          manager.removeResource(resource.name)

          if (manager.addResource(resource.copy(name = name))) {
            refreshList()

            new DirtyEvent(None).raise(parent)
          }

          else {
            manager.addResource(resource)

            // use org.nlogo.swing.OptionPane after new GUI is integrated
            OptionDialog.showMessage(parent, I18N.gui.get("common.messages.error"),
                                    I18N.gui.getN("resource.alreadyExists", name),
                                    Array(I18N.gui.get("common.buttons.ok")))
          }
        }
      }

      catch {
        case t: Throwable => // this will be properly handled after switching to InputOptionPane
      }
    }
  })

  private val removeButton = new JButton(new AbstractAction(I18N.gui.get("resource.remove")) {
    def actionPerformed(e: ActionEvent) {
      manager.removeResource(resourceList.getSelectedValue)

      refreshList()

      new DirtyEvent(None).raise(parent)
    }
  })

  setLayout(new GridBagLayout)

  locally {
    val c = new GridBagConstraints

    c.gridx = 0
    c.fill = GridBagConstraints.HORIZONTAL
    c.insets = new Insets(6, 6, 6, 6)

    add(new JScrollPane(resourceList), c)

    c.fill = GridBagConstraints.HORIZONTAL
    c.weighty = 0

    add(new JPanel(new GridBagLayout) {
      val c = new GridBagConstraints

      c.insets = new Insets(0, 6, 6, 6)

      add(importButton, c)

      c.insets = new Insets(0, 0, 6, 6)

      add(exportButton, c)
      add(renameButton, c)
      add(removeButton, c)
    }, c)
  }

  enableButtons()

  pack()

  Positioning.center(this, parent)

  private def refreshList() {
    resourceList.setListData(manager.getResources.map(_.name).toArray)

    enableButtons()
  }

  private def enableButtons() {
    exportButton.setEnabled(!resourceList.isSelectionEmpty)
    renameButton.setEnabled(!resourceList.isSelectionEmpty)
    removeButton.setEnabled(!resourceList.isSelectionEmpty)
  }
}
