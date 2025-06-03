// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Component, FileDialog => AWTFileDialog, Frame, GridBagConstraints, GridBagLayout, Insets }
import java.io.FileOutputStream
import java.nio.file.{ Files, Paths }
import java.util.Base64
import javax.swing.{ JDialog, JLabel, JList, JPanel, ListCellRenderer, ListSelectionModel }
import javax.swing.event.{ ListSelectionEvent, ListSelectionListener }

import org.nlogo.api.{ Workspace }
import org.nlogo.awt.{ Positioning, UserCancelException }
import org.nlogo.core.{ ExternalResource, I18N }
import org.nlogo.swing.{ Button, FileDialog, InputOptionPane, OptionPane, ScrollPane, Transparent }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.Events.DirtyEvent

class ResourceManagerDialog(parent: Frame, workspace: Workspace)
  extends JDialog(parent, I18N.gui.get("resource.manager"), true) with ThemeSync {

  private val manager = workspace.getResourceManager
  private val resourceList = new JList(manager.getResources.toArray) {
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
    setCellRenderer(new ResourceCellRenderer)

    addListSelectionListener(new ListSelectionListener {
      def valueChanged(e: ListSelectionEvent): Unit = {
        enableButtons()
      }
    })
  }

  private val scrollPane = new ScrollPane(resourceList)

  private val addButton = new Button(I18N.gui.get("resource.add"), () => {
    try {
      val file = FileDialog.showFiles(parent, I18N.gui.get("resource.select"), AWTFileDialog.LOAD)
      val path = Paths.get(file)

      val (fileName, extension) = {
        val split = path.getFileName.toString.split('.')

        if (split.size > 1) {
          (split.dropRight(1).mkString("."), split.last)
        } else {
          (split(0), "")
        }
      }

      val name = new InputOptionPane(parent, I18N.gui.get("resource.name"), I18N.gui.get("resource.name"),
                                     fileName).getInput

      if (name != null) {
        val trimmed = name.trim

        if (trimmed.isEmpty) {
          new OptionPane(parent, I18N.gui.get("common.messages.error"), I18N.gui.get("resource.nameEmpty"),
                         OptionPane.Options.Ok, OptionPane.Icons.Error)
        }

        else {
          val resource = new ExternalResource(trimmed, extension,
                                              Base64.getEncoder.encodeToString(Files.readAllBytes(path)))

          if (manager.addResource(resource)) {
            refreshList()

            new DirtyEvent(None).raise(parent)
          }

          else {
            new OptionPane(parent, I18N.gui.get("common.messages.error"), I18N.gui.getN("resource.alreadyExists", trimmed),
                           OptionPane.Options.Ok, OptionPane.Icons.Error)
          }
        }
      }
    }

    catch {
      case e: UserCancelException =>
    }
  })

  private val exportButton = new Button(I18N.gui.get("resource.export"), () => {
    try {
      val resource = manager.getResource(resourceList.getSelectedValue.name).get
      val stream = new FileOutputStream(FileDialog.showFiles(parent, I18N.gui.get("resource.select"),
                                                              AWTFileDialog.SAVE,
                                                              s"${resource.name}.${resource.extension}"))

      stream.write(Base64.getDecoder.decode(resource.data))
      stream.close()
    }

    catch {
      case e: UserCancelException =>
    }
  })

  private val renameButton = new Button(I18N.gui.get("resource.rename"), () => {
    val resource = manager.getResource(resourceList.getSelectedValue.name).get

    val name = new InputOptionPane(parent, I18N.gui.get("resource.newName"), I18N.gui.get("resource.newName"),
                                   resource.name).getInput

    if (name != null) {
      val trimmed = name.trim

      if (trimmed.isEmpty) {
        new OptionPane(parent, I18N.gui.get("common.messages.error"), I18N.gui.get("resource.nameEmpty"),
                       OptionPane.Options.Ok, OptionPane.Icons.Error)
      }

      else if (trimmed != resource.name) {
        manager.removeResource(resource.name)

        if (manager.addResource(resource.copy(name = trimmed))) {
          refreshList()

          new DirtyEvent(None).raise(parent)
        }

        else {
          manager.addResource(resource)

          new OptionPane(parent, I18N.gui.get("common.messages.error"), I18N.gui.getN("resource.alreadyExists", trimmed),
                         OptionPane.Options.Ok, OptionPane.Icons.Error)
        }
      }
    }
  })

  private val removeButton = new Button(I18N.gui.get("resource.remove"), () => {
    manager.removeResource(resourceList.getSelectedValue.name)

    refreshList()

    new DirtyEvent(None).raise(parent)
  })

  setLayout(new GridBagLayout)

  locally {
    val c = new GridBagConstraints

    c.gridx = 0
    c.fill = GridBagConstraints.BOTH
    c.weightx = 1
    c.weighty = 1
    c.insets = new Insets(6, 6, 6, 6)

    add(scrollPane, c)

    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 0
    c.weighty = 0

    add(new JPanel(new GridBagLayout) with Transparent {
      val c = new GridBagConstraints

      c.insets = new Insets(0, 6, 6, 6)

      add(addButton, c)

      c.insets = new Insets(0, 0, 6, 6)

      add(exportButton, c)
      add(renameButton, c)
      add(removeButton, c)
    }, c)
  }

  enableButtons()

  pack()

  Positioning.center(this, parent)

  syncTheme()

  private def refreshList(): Unit = {
    resourceList.setListData(manager.getResources.toArray)

    enableButtons()
  }

  private def enableButtons(): Unit = {
    exportButton.setEnabled(!resourceList.isSelectionEmpty)
    renameButton.setEnabled(!resourceList.isSelectionEmpty)
    removeButton.setEnabled(!resourceList.isSelectionEmpty)
  }

  override def syncTheme(): Unit = {
    getContentPane.setBackground(InterfaceColors.dialogBackground())

    scrollPane.setBackground(InterfaceColors.dialogBackground())

    resourceList.setBackground(InterfaceColors.dialogBackground())

    addButton.syncTheme()
    exportButton.syncTheme()
    renameButton.syncTheme()
    removeButton.syncTheme()
  }

  private class ResourceCellRenderer extends JPanel(new GridBagLayout) with ListCellRenderer[ExternalResource] {
    private val nameLabel = new JLabel
    private val extensionLabel = new JLabel

    locally {
      val c = new GridBagConstraints

      c.anchor = GridBagConstraints.WEST
      c.fill = GridBagConstraints.HORIZONTAL
      c.weightx = 1
      c.insets = new Insets(3, 6, 3, 6)

      add(nameLabel, c)

      c.fill = GridBagConstraints.NONE
      c.weightx = 0
      c.insets = new Insets(3, 0, 3, 6)

      add(extensionLabel, c)
    }

    def getListCellRendererComponent(list: JList[? <: ExternalResource], resource: ExternalResource, index: Int,
                                     isSelected: Boolean, hasFocus: Boolean): Component = {
      nameLabel.setText(resource.name)
      extensionLabel.setText(if (resource.extension.isEmpty) "" else ("." + resource.extension))

      if (isSelected) {
        setBackground(InterfaceColors.dialogBackgroundSelected())

        nameLabel.setForeground(InterfaceColors.dialogTextSelected())
        extensionLabel.setForeground(InterfaceColors.dialogTextSelected())
      }

      else {
        setBackground(InterfaceColors.dialogBackground())

        nameLabel.setForeground(InterfaceColors.dialogText())
        extensionLabel.setForeground(InterfaceColors.dialogText())
      }

      this
    }
  }
}
