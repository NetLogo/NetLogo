// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ BorderLayout, Component, FileDialog => AWTFileDialog, Frame }
import java.nio.file.{ Files, Paths }
import javax.swing.{ DefaultListModel, DefaultListSelectionModel, Box, BoxLayout, JDialog, JLabel, JList, JPanel,
                     ListCellRenderer, ListSelectionModel }
import javax.swing.event.{ ListSelectionEvent, ListSelectionListener }
import javax.swing.border.EmptyBorder

import org.nlogo.analytics.Analytics
import org.nlogo.core.{ I18N, NetLogoPreferences }
import org.nlogo.swing.{ Button, FileDialog, OptionPane, Positioning, ScrollPane, Transparent }
import org.nlogo.theme.{ ColorTheme, InterfaceColors, ThemeSync }

class ThemesManager(appFrame: Frame & ThemeSync)
  extends JDialog(null: Frame, I18N.gui.get("menu.tools.themesManager"), false) with ThemeSync {

  private implicit val i18nPrefix: I18N.Prefix = I18N.Prefix("menu.tools.themesManager")

  private val message = new JLabel(s"<html>${I18N.gui("message")}</html>")

  private val themeModel = new DefaultListModel[ListEntry]

  private val themeList = new JList[ListEntry] {
    setModel(themeModel)
    setSelectionModel(new ThemeSelectionModel)
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
    setCellRenderer(new ThemeCellRenderer)

    addListSelectionListener(new ListSelectionListener {
      override def valueChanged(e: ListSelectionEvent): Unit = {
        applyTheme()
        updateButtons()
      }
    })
  }

  private val scrollPane = new ScrollPane(themeList)

  private val duplicateButton = new Button(I18N.gui("duplicate"), duplicate)
  private val editButton = new Button(I18N.gui("edit"), edit)
  private val deleteButton = new Button(I18N.gui("delete"), delete)
  private val importButton = new Button(I18N.gui("import"), importTheme)
  private val exportButton = new Button(I18N.gui("export"), exportTheme)

  locally {
    val panel = new JPanel with Transparent {
      setLayout(new BorderLayout(6, 6))
      setBorder(new EmptyBorder(6, 6, 6, 6))

      add(message, BorderLayout.NORTH)
      add(scrollPane, BorderLayout.CENTER)

      val buttonPanel = new JPanel with Transparent {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS))
        setBorder(new EmptyBorder(6, 6, 6, 6))

        add(Box.createHorizontalGlue)
        add(duplicateButton)
        add(Box.createHorizontalStrut(6))
        add(editButton)
        add(Box.createHorizontalStrut(6))
        add(deleteButton)
        add(Box.createHorizontalStrut(6))
        add(importButton)
        add(Box.createHorizontalStrut(6))
        add(exportButton)
        add(Box.createHorizontalGlue)
      }

      add(buttonPanel, BorderLayout.SOUTH)
    }

    add(panel)

    themeModel.addElement(DivEntry(I18N.gui("permanent")))

    InterfaceColors.getPermanentThemes.foreach(theme => themeModel.addElement(ThemeEntry(theme)))

    themeModel.addElement(DivEntry(I18N.gui("custom")))

    InterfaceColors.getCustomThemes.foreach(theme => themeModel.addElement(ThemeEntry(theme)))

    themeList.setSelectedValue(ThemeEntry(InterfaceColors.getTheme), true)

    updateButtons()
    pack()

    Positioning.center(this, appFrame)
  }

  private def duplicate(): Unit = {
    themeList.getSelectedValue match {
      case ThemeEntry(baseTheme) =>
        new ThemeEditor(this, baseTheme, appFrame, true).getTheme.foreach { theme =>
          val entry = ThemeEntry(theme)

          themeModel.addElement(entry)
          themeList.setSelectedValue(entry, true)

          InterfaceColors.saveTheme(theme)
        }

        applyTheme()

      case _ =>
    }
  }

  private def edit(): Unit = {
    themeList.getSelectedValue match {
      case ThemeEntry(baseTheme) =>
        new ThemeEditor(this, baseTheme, appFrame, false).getTheme.foreach { theme =>
          themeModel.set(themeList.getSelectedIndex, ThemeEntry(theme))

          if (theme.name != baseTheme.name)
            InterfaceColors.deleteTheme(baseTheme)

          InterfaceColors.saveTheme(theme)
        }

        applyTheme()

      case _ =>
    }
  }

  private def delete(): Unit = {
    themeList.getSelectedValue match {
      case entry @ ThemeEntry(theme) =>
        val index = themeList.getSelectedIndex

        themeModel.removeElement(entry)

        if (index >= themeModel.size) {
          themeList.setSelectedIndex(3)
        } else {
          themeList.setSelectedIndex(index)
        }

        InterfaceColors.deleteTheme(theme)

      case _ =>
    }
  }

  private def importTheme(): Unit = {
    val path = Paths.get(FileDialog.showFiles(appFrame, I18N.gui("import"), AWTFileDialog.LOAD))

    if (!path.toString.endsWith(".theme")) {
      new OptionPane(appFrame, I18N.gui("importError"), I18N.gui("notTheme"), OptionPane.Options.Ok,
                     OptionPane.Icons.Error)

      return
    }

    val name = path.getFileName.toString.stripSuffix(".theme").trim

    if (InterfaceColors.reservedName(name)) {
      new OptionPane(appFrame, I18N.gui("importError"), I18N.gui("reservedName"), OptionPane.Options.Ok,
                     OptionPane.Icons.Error)

      return
    }

    if (!InterfaceColors.themeExists(name) || new OptionPane(appFrame, I18N.gui("confirmOverwrite"),
                                                              I18N.gui("themeOverwrite", name),
                                                              OptionPane.Options.YesNo, OptionPane.Icons.Warning)
                                                .getSelectedIndex == 0) {
      if (!InterfaceColors.importTheme(path)) {
        new OptionPane(appFrame, I18N.gui("importError"), I18N.gui("invalidTheme"), OptionPane.Options.Ok,
                        OptionPane.Icons.Error)
      }
    }
  }

  private def exportTheme(): Unit = {
    themeList.getSelectedValue match {
      case ThemeEntry(theme) =>
        val path = Paths.get(FileDialog.showDirectories(appFrame, I18N.gui("export")))
        val dest = path.resolve(s"${theme.name}.theme")

        if (!Files.exists(dest) || new OptionPane(appFrame, I18N.gui("confirmOverwrite"),
                                                  I18N.gui("fileOverwrite", dest.getFileName),
                                                  OptionPane.Options.YesNo, OptionPane.Icons.Warning)
                                     .getSelectedIndex == 0) {
          InterfaceColors.saveTheme(theme, Option(path))
        }

      case _ =>
    }
  }

  private def updateButtons(): Unit = {
    themeList.getSelectedValue match {
      case ThemeEntry(theme) =>
        duplicateButton.setEnabled(true)
        editButton.setEnabled(!theme.permanent)
        deleteButton.setEnabled(!theme.permanent)

      case _ =>
        duplicateButton.setEnabled(false)
        editButton.setEnabled(false)
        deleteButton.setEnabled(false)
    }
  }

  private def applyTheme(): Unit = {
    themeList.getSelectedValue match {
      case ThemeEntry(theme) =>
        InterfaceColors.setTheme(theme)

        if (theme.name != InterfaceColors.prefsTheme.name) {
          if (theme.permanent) {
            Analytics.preferenceChange("colorTheme", theme.prefName)
          } else {
            Analytics.preferenceChange("colorTheme", "custom")
          }

          NetLogoPreferences.put("customColorTheme", theme.prefName)
        }

        appFrame.syncTheme()

      case _ =>
    }
  }

  override def syncTheme(): Unit = {
    getContentPane.setBackground(InterfaceColors.dialogBackground())
    themeList.setBackground(InterfaceColors.dialogBackground())
    scrollPane.setBackground(InterfaceColors.dialogBackground())

    message.setForeground(InterfaceColors.dialogText())

    duplicateButton.syncTheme()
    editButton.syncTheme()
    deleteButton.syncTheme()
    importButton.syncTheme()
    exportButton.syncTheme()
  }

  private abstract trait ListEntry

  private case class ThemeEntry(theme: ColorTheme) extends ListEntry
  private case class DivEntry(text: String) extends ListEntry

  private class ThemeSelectionModel extends DefaultListSelectionModel {
    override def setSelectionInterval(start: Int, end: Int): Unit = {
      themeModel.get(start.min(end)) match {
        case ThemeEntry(_) =>
          super.setSelectionInterval(start, end)

        case _ =>
      }
    }
  }

  private class ThemeCellRenderer extends JPanel with ListCellRenderer[ListEntry] {
    private val label = new JLabel

    setLayout(new BoxLayout(this, BoxLayout.X_AXIS))

    add(label)
    add(Box.createHorizontalGlue)

    override def getListCellRendererComponent(list: JList[? <: ListEntry], value: ListEntry, index: Int,
                                              isSelected: Boolean, cellHasFocus: Boolean): Component = {
      value match {
        case ThemeEntry(theme) =>
          label.setText(theme.name)

          if (isSelected) {
            setBackground(InterfaceColors.dialogBackgroundSelected())
            label.setForeground(InterfaceColors.dialogTextSelected())
          } else {
            setBackground(InterfaceColors.dialogBackground())
            label.setForeground(InterfaceColors.dialogText())
          }

          setBorder(new EmptyBorder(6, 6, 6, 6))

        case DivEntry(text) =>
          label.setText(s"<html><u>$text</u></html>")

          setBackground(InterfaceColors.dialogBackground())
          label.setForeground(InterfaceColors.menuTextDisabled())

          setBorder(new EmptyBorder(2, 6, 2, 6))
      }

      this
    }
  }
}
