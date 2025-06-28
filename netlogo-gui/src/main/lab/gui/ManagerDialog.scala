// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import java.awt.{ Component, Dimension, FlowLayout, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, JDialog, JLabel, JList, JMenuBar, JPanel, ListCellRenderer }
import javax.swing.event.ListSelectionListener

import org.nlogo.api.{ RefEnumeratedValueSet, LabProtocol, LabRunOptions }
import org.nlogo.window.{ EditDialogFactory, MenuBarFactory }

import org.nlogo.core.I18N
import org.nlogo.swing.{ Button, FileDialog, OptionPane, ScrollPane, Transparent, Utils }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

import scala.io.Source

private class ManagerDialog(manager:       LabManager,
                            dialogFactory: EditDialogFactory,
                            menuFactory:   MenuBarFactory)
  extends JDialog(manager.workspace.getFrame) with ListSelectionListener with ThemeSync {

  def saveProtocol(protocol: LabProtocol): Unit = {
    manager.protocols(editIndex) = protocol
    update()
    select(protocol)
  }
  private val jlist = new JList[LabProtocol]
  private val listModel = new javax.swing.DefaultListModel[LabProtocol]
  private implicit val i18NPrefix: org.nlogo.core.I18N.Prefix = I18N.Prefix("tools.behaviorSpace")

  /// actions
  private def makeAction(name: String, fn: () => Unit) = {
    new AbstractAction(name) {
      def actionPerformed(e: ActionEvent): Unit = {
        fn()
      }
    }
  }

  private val editAction = makeAction(I18N.gui("edit"), edit)
  private val newAction = makeAction(I18N.gui("new"), makeNew)
  private val deleteAction = makeAction(I18N.gui("delete"), delete)
  private val duplicateAction = makeAction(I18N.gui("duplicate"), duplicate)
  private val importAction = makeAction(I18N.gui("import"), importnl)
  private val exportAction = makeAction(I18N.gui("export"), `export`)
  private val closeAction = makeAction(I18N.gui("close"), manager.close)
  private val abortAction = makeAction(I18N.gui("abort"), abort)
  private val runAction = makeAction(I18N.gui("run"), run)

  private var blockActions = false
  private var editIndex = 0

  /// initialization
  setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE)
  addWindowListener(new java.awt.event.WindowAdapter {
    override def windowClosing(e: java.awt.event.WindowEvent): Unit = { closeAction.actionPerformed(null) } })
  setTitle(I18N.gui.get("menu.tools.behaviorSpace"))
  // set up the list
  jlist.setVisibleRowCount(5)
  jlist.setModel(listModel)
  jlist.addListSelectionListener(this)
  // Listen for double-clicks, and edit the selected protocol
  jlist.addMouseListener(new javax.swing.event.MouseInputAdapter {
    override def mouseClicked(e: java.awt.event.MouseEvent): Unit = {
      if (e.getClickCount > 1 && jlist.getSelectedIndices.length == 1
          && selectedProtocol.runsCompleted == 0 && !blockActions) {
        edit()
      }
    } })
  jlist.setCellRenderer(new ProtocolRenderer)

  private val listLabel = new JLabel(I18N.gui("experiments"))
  private val scrollPane = new ScrollPane(jlist)

  private val newButton = new Button(newAction)
  private val editButton = new Button(editAction)
  private val duplicateButton = new Button(duplicateAction)
  private val deleteButton = new Button(deleteAction)
  private val importButton = new Button(importAction)
  private val exportButton = new Button(exportAction)
  private val abortButton = new Button(abortAction)
  private val runButton = new Button(runAction)

  getContentPane.setLayout(new GridBagLayout)

  locally {
    val c = new GridBagConstraints

    c.gridx = 0
    c.anchor = GridBagConstraints.WEST
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(6, 6, 6, 6)

    getContentPane.add(listLabel, c)

    c.anchor = GridBagConstraints.CENTER
    c.fill = GridBagConstraints.BOTH
    c.weighty = 1
    c.insets = new Insets(0, 6, 6, 6)

    getContentPane.add(scrollPane, c)

    c.fill = GridBagConstraints.HORIZONTAL
    c.weighty = 0
    c.insets = new Insets(0, 6, 6, 6)

    getContentPane.add(new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0)) with Transparent {
      add(newButton)
      add(editButton)
      add(duplicateButton)
      add(deleteButton)
    }, c)

    getContentPane.add(new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0)) with Transparent {
      add(importButton)
      add(exportButton)
      add(abortButton)
      add(runButton)
    }, c)
  }

  pack()

  // set location
  private val maxBounds = getGraphicsConfiguration.getBounds
  setLocation(maxBounds.x + maxBounds.width / 3,
              maxBounds.y + maxBounds.height / 2)

  // menu - make a file menu available for saving, but don't show it
  private val menus = new JMenuBar() {
    add(menuFactory.createFileMenu)
    setPreferredSize(new Dimension(0, 0))
  }
  setJMenuBar(menus)
  // misc
  Utils.addEscKeyAction(this, closeAction)
  getRootPane.setDefaultButton(runButton)
  /// implement ListSelectionListener
  def valueChanged(e: javax.swing.event.ListSelectionEvent): Unit = {
    if (blockActions) {
      editAction.setEnabled(false)
      newAction.setEnabled(false)
      deleteAction.setEnabled(false)
      duplicateAction.setEnabled(false)
      importAction.setEnabled(false)
      exportAction.setEnabled(false)
      closeAction.setEnabled(false)
      abortAction.setEnabled(false)
      runAction.setEnabled(false)
    } else {
      val count = jlist.getSelectedIndices.length
      editAction.setEnabled(count == 1 && selectedProtocol.runsCompleted == 0)
      newAction.setEnabled(true)
      deleteAction.setEnabled(count > 0)
      duplicateAction.setEnabled(count == 1)
      importAction.setEnabled(true)
      exportAction.setEnabled(count > 0)
      closeAction.setEnabled(true)
      abortAction.setEnabled(count == 1 && selectedProtocol.runsCompleted != 0)
      runAction.setEnabled(count == 1)
    }
  }
  /// action implementations
  private def run(): Unit = {
    try {
      editIndex = selectedIndex

      manager.prepareForRun()

      new Supervisor(this, manager.workspace, selectedProtocol, manager.workspaceFactory, dialogFactory, saveProtocol).start()
    }
    catch { case ex: org.nlogo.awt.UserCancelException => org.nlogo.api.Exceptions.ignore(ex) }
  }
  private def makeNew(): Unit = {
    editProtocol(
      new LabProtocol(constants = manager.workspace.world.synchronized {
                                    manager.workspace.world.program.interfaceGlobals.toList
                                    .map{case variableName: String =>
                                      new RefEnumeratedValueSet(
                                        variableName,
                                        List(manager.workspace.world.getObserverVariableByName(variableName)))}}),
      true)
  }
  private def duplicate(): Unit = { editProtocol(selectedProtocol.copy(name = selectedProtocol.name + " (copy)",
                                                               runsCompleted = 0), true) }
  private def edit(): Unit = { editProtocol(selectedProtocol, false) }
  private def editProtocol(protocol: LabProtocol, isNew: Boolean): Unit = {
    blockActions = true
    if (!isNew) editIndex = selectedIndex
    update()
    val editable = new ProtocolEditable(protocol, manager.workspace.getFrame,
                                        manager.workspace, dialogFactory.colorizer, manager.workspace.world,
                                        manager.protocols.map(_.name).filter(isNew || _ != protocol.name).toSeq)
    dialogFactory.create(manager.workspace.getFrame, editable, success => {
      blockActions = false
      if (success) {
        val newProtocol = editable.get.get
        if (isNew) {
          manager.protocols += newProtocol
        } else {
          manager.protocols(editIndex) = newProtocol
        }
        update()
        select(newProtocol)
      } else {
        update()
      }
    })
  }
  private def delete(): Unit = {
    val selected = jlist.getSelectedIndices
    val message = {
      if (selected.length > 1) {
        I18N.gui("delete.confirm.multiple", selected.length.toString)
      } else {
        I18N.gui("delete.confirm.one", listModel.getElementAt(selected(0)).asInstanceOf[LabProtocol].name)
      }
    }
    if (new OptionPane(this, I18N.gui("delete"), message, OptionPane.Options.YesNo, OptionPane.Icons.Question)
          .getSelectedIndex == 0) {
      for(i <- 0 until selected.length)
        manager.protocols -= listModel.getElementAt(selected(i)).asInstanceOf[LabProtocol]
      update()
      // it's annoying if nothing is left selected, so select something
      val newSize = manager.protocols.size
      if (newSize > 0) select(
        if (selected(0) >= newSize) {
          (selected(0) - 1) min (newSize - 1)
        } else {
          selected(0)
        })
      manager.dirty()
    }
  }
  private def importnl(): Unit = {
    try {
      class XMLFilter extends java.io.FilenameFilter {
        def accept(dir: java.io.File, name: String): Boolean = {
          val split = name.split('.')

          split.length == 2 && split(1) == "xml"
        }
      }

      val dialog = new java.awt.FileDialog(manager.workspace.getFrame, I18N.gui("import.dialog"))

      dialog.setDirectory(System.getProperty("user.home"))
      dialog.setFilenameFilter(new XMLFilter)
      dialog.setMultipleMode(true)
      dialog.setVisible(true)

      for (file <- dialog.getFiles) {
        try {
          manager.modelLoader.readExperiments(Source.fromFile(file).mkString, true,
                                              manager.protocols.map(_.name).toSet)
                             .foreach(_._1.foreach(manager.addProtocol(_)))
        } catch {
          case e: org.xml.sax.SAXParseException => {
            if (!java.awt.GraphicsEnvironment.isHeadless) {
              new OptionPane(manager.workspace.getFrame, I18N.gui("invalid"), I18N.gui("error.import", file.getName),
                             OptionPane.Options.Ok, OptionPane.Icons.Error)
            }
          }
        }
      }

      update()
    } catch {
      case e: org.nlogo.awt.UserCancelException => org.nlogo.api.Exceptions.ignore(e)
    }
  }
  private def `export`(): Unit = {
    try {
      val indices = jlist.getSelectedIndices

      val modelName =
        if (manager.workspace.getModelFileName == null) {
          ""
        } else {
          manager.workspace.getModelFileName.split('.')(0) + '-'
        }

      var path = FileDialog.showFiles(manager.workspace.getFrame, I18N.gui("export.dialog"), java.awt.FileDialog.SAVE,
                  if (indices.length == 1) {
                    modelName + selectedProtocol.name + "-experiment.xml"
                  } else {
                    modelName + "experiments.xml"
                  })

      if (!path.endsWith(".xml"))
        path += ".xml"

      val out = new java.io.PrintWriter(path)

      manager.modelLoader.writeExperiments(manager.protocols.toSeq, out)

      out.close()
    } catch {
      case e: org.nlogo.awt.UserCancelException => org.nlogo.api.Exceptions.ignore(e)
    }
  }
  private def abort(): Unit = {
    saveProtocol(selectedProtocol.copy(runsCompleted = 0, runOptions = LabRunOptions()))
  }
  /// helpers
  def update(): Unit = {
    listModel.clear
    manager.protocols.foreach(listModel.addElement(_))
    manager.workspace.setBehaviorSpaceExperiments(manager.protocols.toSeq)
    valueChanged(null)
    if (manager.protocols.size > 0) jlist.setSelectedIndices(Array(0))
  }
  private def select(index: Int): Unit = {
    jlist.setSelectedIndices(Array(index))
    jlist.ensureIndexIsVisible(index)
  }
  private def select(targetProtocol: LabProtocol): Unit = {
    val index = manager.protocols.indexWhere(_ eq targetProtocol)
    jlist.setSelectedIndices(Array(index))
    jlist.ensureIndexIsVisible(index)
  }
  private def selectedIndex: Int = {
    jlist.getSelectedIndices match {
      case Array(i: Int) => i
      case _ => -1
    }
  }
  private def selectedProtocol =
    manager.protocols(jlist.getSelectedIndices()(0))

  override def syncTheme(): Unit = {
    getContentPane.setBackground(InterfaceColors.dialogBackground())
    listLabel.setForeground(InterfaceColors.dialogText())
    scrollPane.setBackground(InterfaceColors.dialogBackground())
    jlist.setBackground(InterfaceColors.dialogBackground())

    newButton.syncTheme()
    editButton.syncTheme()
    duplicateButton.syncTheme()
    deleteButton.syncTheme()
    importButton.syncTheme()
    exportButton.syncTheme()
    abortButton.syncTheme()
    runButton.syncTheme()

    dialogFactory.syncTheme()
  }

  class ProtocolRenderer extends JPanel(new FlowLayout(FlowLayout.LEFT)) with ListCellRenderer[LabProtocol] {
    private val label = new JLabel

    add(label)

    def getListCellRendererComponent(list: JList[? <: LabProtocol], proto: LabProtocol, index: Int,
                                     isSelected: Boolean, cellHasFocus: Boolean): Component = {
      label.setText(
        if (proto.runsCompleted != 0) {
          I18N.gui("inProgress", proto.name, proto.runsCompleted.toString, proto.countRuns.toString)
        } else {
          s"${proto.name} (${proto.countRuns} run${(if (proto.countRuns != 1) "s" else "")})"
        })

      if (isSelected) {
        setBackground(InterfaceColors.dialogBackgroundSelected())
        label.setForeground(InterfaceColors.dialogTextSelected())
      } else {
        setBackground(InterfaceColors.dialogBackground())
        label.setForeground(InterfaceColors.dialogText())
      }

      this
    }
  }
}
