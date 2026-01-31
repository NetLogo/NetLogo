// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ BorderLayout, Component, Frame }
import javax.swing.{ DefaultListModel, DefaultListSelectionModel, Box, BoxLayout, JDialog, JLabel, JList, JPanel,
                     ListCellRenderer, ListSelectionModel }
import javax.swing.event.{ ListSelectionEvent, ListSelectionListener }
import javax.swing.border.EmptyBorder

import org.nlogo.analytics.Analytics
import org.nlogo.core.{ I18N, NetLogoPreferences }
import org.nlogo.swing.{ Button, Positioning, ScrollPane, Transparent }
import org.nlogo.theme.{ ColorTheme, InterfaceColors, ThemeSync }

class ThemesManager(appFrame: Frame & ThemeSync)
  extends JDialog(null: Frame, I18N.gui.get("menu.tools.themesManager"), false) with ThemeSync {

  private val message = new JLabel(s"<html>${I18N.gui.get("menu.tools.themesManager.message")}</html>")

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

  private val duplicateButton = new Button(I18N.gui.get("menu.tools.themesManager.duplicate"), duplicate)
  private val editButton = new Button(I18N.gui.get("menu.tools.themesManager.edit"), edit)
  private val deleteButton = new Button(I18N.gui.get("menu.tools.themesManager.delete"), delete)

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
        add(Box.createHorizontalGlue)
      }

      add(buttonPanel, BorderLayout.SOUTH)
    }

    add(panel)

    themeModel.addElement(DivEntry(I18N.gui.get("menu.tools.themesManager.permanent")))

    InterfaceColors.getPermanentThemes.foreach(theme => themeModel.addElement(ThemeEntry(theme)))

    themeModel.addElement(DivEntry(I18N.gui.get("menu.tools.themesManager.custom")))

    InterfaceColors.getCustomThemes.foreach(theme => themeModel.addElement(ThemeEntry(theme)))

    themeList.setSelectedValue(ThemeEntry(InterfaceColors.getTheme), true)

    updateButtons()
    pack()

    Positioning.center(this, appFrame)
  }

  private def duplicate(): Unit = {
    themeList.getSelectedValue match {
      case ThemeEntry(baseTheme) =>
        new ThemeEditor(this, baseTheme, appFrame).getTheme.foreach { theme =>
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
        new ThemeEditor(this, baseTheme, appFrame).getTheme.foreach { theme =>
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
            Analytics.preferenceChange("colorTheme", theme.name)
          } else {
            Analytics.preferenceChange("colorTheme", "Custom")
          }

          NetLogoPreferences.put("colorTheme", theme.name)
        }

        appFrame.syncTheme()

      case _ =>
    }
  }

  def themeExists(name: String): Boolean = {
    themeModel.toArray.exists {
      case ThemeEntry(theme) if theme.name == name =>
        true

      case _ =>
        false
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
