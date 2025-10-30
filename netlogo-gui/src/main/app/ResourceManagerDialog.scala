// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Component, FileDialog => AWTFileDialog, Font, Frame, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.ActionEvent
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.charset.{ MalformedInputException, StandardCharsets }
import java.nio.file.{ Files, Paths }
import java.util.Base64
import javax.swing.{ AbstractAction, JDialog, JLabel, JPanel, JTable, ListSelectionModel }
import javax.swing.border.MatteBorder
import javax.swing.event.{ ListSelectionEvent, ListSelectionListener }
import javax.swing.table.{ DefaultTableModel, TableCellRenderer }

import org.nlogo.api.{ Workspace }
import org.nlogo.awt.{ Positioning, UserCancelException }
import org.nlogo.core.{ ExternalResource, I18N }
import org.nlogo.swing.{ AutomateWindow, Button, FileDialog, InputOptionPane, OptionPane, ScrollPane, Transparent,
                         Utils }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.Events.{ DirtyEvent, ResourcesChangedEvent }

class ResourceManagerDialog(parent: Frame, workspace: Workspace)
  extends JDialog(parent, I18N.gui.get("resource.manager"), true) with ThemeSync with AutomateWindow {

  private val manager = workspace.getResourceManager

  private val tableModel = new DefaultTableModel(0, 2)

  private val table = new JTable(tableModel) {
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
    setCellSelectionEnabled(false)
    setRowSelectionAllowed(true)

    getTableHeader.setReorderingAllowed(false)

    setDefaultRenderer(classOf[ResourceCellRenderer], new ResourceCellRenderer)

    getSelectionModel.addListSelectionListener(new ListSelectionListener {
      def valueChanged(e: ListSelectionEvent): Unit = {
        enableButtons()
      }
    })

    val nameColumn = getColumnModel.getColumn(0)

    nameColumn.setHeaderValue("Name")
    nameColumn.setCellRenderer(new ResourceCellRenderer)
    nameColumn.setHeaderRenderer(new HeaderCellRenderer)

    val extensionColumn = getColumnModel.getColumn(1)

    extensionColumn.setHeaderValue("Extension")
    extensionColumn.setCellRenderer(new ResourceCellRenderer)
    extensionColumn.setHeaderRenderer(new HeaderCellRenderer)

    override def isCellEditable(row: Int, column: Int): Boolean = false
  }

  private val scrollPane = new ScrollPane(table)

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
          val bytes = Files.readAllBytes(path)

          val text = {
            try {
              StandardCharsets.UTF_8.newDecoder.decode(ByteBuffer.wrap(bytes)).toString
            } catch {
              case _: MalformedInputException => Base64.getEncoder.encodeToString(bytes)
            }
          }

          val resource = new ExternalResource(trimmed, extension, text)

          if (manager.addResource(resource)) {
            refreshList()

            new DirtyEvent(None).raise(parent)
            new ResourcesChangedEvent().raise(parent)
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
      val resource = manager.getResource(table.getValueAt(table.getSelectedRow, 0).toString).get
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
    val resource = manager.getResource(table.getValueAt(table.getSelectedRow, 0).toString).get

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
          new ResourcesChangedEvent().raise(parent)
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
    manager.removeResource(table.getValueAt(table.getSelectedRow, 0).toString)

    refreshList()

    new DirtyEvent(None).raise(parent)
    new ResourcesChangedEvent().raise(parent)
  })

  locally {
    setLayout(new GridBagLayout)

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

    refreshList()

    pack()

    Positioning.center(this, parent)

    Utils.addEscKeyAction(this, new AbstractAction {
      override def actionPerformed(e: ActionEvent): Unit = {
        setVisible(false)
      }
    })

    syncTheme()
  }

  private def refreshList(): Unit = {
    tableModel.setRowCount(0)

    manager.getResources.foreach { resource =>
      tableModel.addRow(Array[Object](resource.name, resource.extension))
    }

    enableButtons()
  }

  private def enableButtons(): Unit = {
    exportButton.setEnabled(table.getSelectedRow != -1)
    renameButton.setEnabled(table.getSelectedRow != -1)
    removeButton.setEnabled(table.getSelectedRow != -1)
  }

  override def syncTheme(): Unit = {
    getContentPane.setBackground(InterfaceColors.dialogBackground())

    scrollPane.setBackground(InterfaceColors.dialogBackground())

    table.setBackground(InterfaceColors.dialogBackground())

    addButton.syncTheme()
    exportButton.syncTheme()
    renameButton.syncTheme()
    removeButton.syncTheme()
  }

  private class ResourceCellRenderer extends JPanel(new GridBagLayout) with TableCellRenderer {
    private val label = new JLabel

    locally {
      val c = new GridBagConstraints

      c.anchor = GridBagConstraints.WEST
      c.weightx = 1
      c.insets = new Insets(3, 6, 3, 6)

      add(label, c)
    }

    def getTableCellRendererComponent(table: JTable, value: Object, isSelected: Boolean, hasFocus: Boolean, row: Int,
                                      column: Int): Component = {
      label.setText(value.toString)

      if (isSelected) {
        setBackground(InterfaceColors.dialogBackgroundSelected())

        label.setForeground(InterfaceColors.dialogTextSelected())
      }

      else {
        setBackground(InterfaceColors.dialogBackground())

        label.setForeground(InterfaceColors.dialogText())
      }

      if (column == 0) {
        setBorder(new MatteBorder(0, 0, 1, 0, InterfaceColors.dialogText()))
      } else {
        setBorder(new MatteBorder(0, 1, 1, 0, InterfaceColors.dialogText()))
      }

      this
    }
  }

  private class HeaderCellRenderer extends JPanel(new GridBagLayout) with TableCellRenderer {
    private val label = new JLabel

    locally {
      label.setFont(label.getFont.deriveFont(Font.BOLD))

      val c = new GridBagConstraints

      c.anchor = GridBagConstraints.WEST
      c.weightx = 1
      c.insets = new Insets(3, 6, 3, 6)

      add(label, c)
    }

    def getTableCellRendererComponent(table: JTable, value: Object, isSelected: Boolean, hasFocus: Boolean, row: Int,
                                      column: Int): Component = {
      label.setText(value.toString)

      setBackground(InterfaceColors.dialogBackground())

      if (column == 0) {
        setBorder(new MatteBorder(0, 0, 1, 0, InterfaceColors.dialogText()))
      } else {
        setBorder(new MatteBorder(0, 1, 1, 0, InterfaceColors.dialogText()))
      }

      label.setForeground(InterfaceColors.dialogText())

      this
    }
  }
}
