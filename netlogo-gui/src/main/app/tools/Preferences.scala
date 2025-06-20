// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Frame, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.ActionEvent
import java.io.File
import java.util.Locale
import javax.swing.{ AbstractAction, JComponent, JFileChooser, JLabel, JPanel }

import org.nlogo.app.common.TabsInterface
import org.nlogo.core.{ I18N, NetLogoPreferences }
import org.nlogo.swing.{ Button, CheckBox, ComboBox, TextField, Transparent }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.AbstractWidgetPanel

object Preferences {
  abstract class BooleanPreference(val i18nKey: String, val requirement: Option[RequiredAction], default: Boolean) extends Preference {
    private val checkBox = new CheckBox

    override def component: CheckBox = checkBox

    def load() = {
      val value = NetLogoPreferences.get(i18nKey, default.toString).toBoolean
      checkBox.setSelected(value)
    }

    def save() = {
      NetLogoPreferences.put(i18nKey, checkBox.isSelected.toString)
    }
  }

  abstract class StringPreference(val i18nKey: String, val requirement: Option[RequiredAction], default: String) extends Preference {
    val textField = new TextField(20, default) {
      override def getInsets: Insets =
        new Insets(3, 3, 3, 0)
    }

    def component: JComponent & ThemeSync = textField

    def load() = {
      val value = NetLogoPreferences.get(i18nKey, default)
      textField.setText(value)
    }

    def save() = {
      NetLogoPreferences.put(i18nKey, textField.getText)
    }
  }

  object Language extends Preference {
    sealed trait LocaleOption

    case object DetectLocale extends LocaleOption {
      override def toString = I18N.gui.get("tools.preferences.detectLanguage")
    }

    case class LocaleWrapper(val locale: Locale) extends LocaleOption {
      override def toString = locale.getDisplayLanguage
    }

    val languages: Seq[LocaleOption] = DetectLocale +: I18N.availableLocales.distinctBy(_.getLanguage)
                                                           .map(LocaleWrapper(_)).sortBy(_.toString).toSeq

    val i18nKey = "uiLanguage"
    val comboBox = new ComboBox(languages) {
      addItemListener(_ => {
        getSelectedItem match {
          case Some(DetectLocale) =>
            label.setText(I18N.gui.defaultLocale.getDisplayLanguage)

          case _ =>
            label.setText("")
        }
      })
    }
    val requirement = Some(RequiredAction.Restart)

    override val anchor: Int = GridBagConstraints.NORTHWEST

    private val label = new JLabel

    private val panel = new JPanel(new GridBagLayout) with Transparent with ThemeSync {
      locally {
        val c = new GridBagConstraints

        c.gridx = 0
        c.fill = GridBagConstraints.HORIZONTAL
        c.weightx = 1
        c.insets = new Insets(0, 0, 3, 0)

        add(comboBox, c)

        c.anchor = GridBagConstraints.EAST
        c.fill = GridBagConstraints.NONE

        add(label, c)
      }

      override def syncTheme(): Unit = {
        label.setForeground(InterfaceColors.dialogText())

        comboBox.syncTheme()
      }
    }

    label.setFont(label.getFont.deriveFont(10f))

    def component: JComponent & ThemeSync = panel

    def load(): Unit = {
      comboBox.setSelectedItem(I18N.localeFromPreferences.map(LocaleWrapper(_)).getOrElse(DetectLocale))
    }

    def save(): Unit = {
      comboBox.getSelectedItem.foreach(_ match {
        case DetectLocale =>
          NetLogoPreferences.remove("user.language")
          NetLogoPreferences.remove("user.country")

        case LocaleWrapper(locale) =>
          NetLogoPreferences.put("user.language", locale.getLanguage)
          NetLogoPreferences.put("user.country", locale.getCountry)
      })
    }
  }

  object LoadLastOnStartup extends BooleanPreference("loadLastOnStartup", None, false) {}

  class ReloadOnExternalChanges(tabs: TabsInterface) extends Preference {
    val i18nKey = "reloadOnExternalChanges"
    val checkBox = new CheckBox
    val requirement = None

    def component: JComponent & ThemeSync = checkBox

    def load() = {
      val enabled = NetLogoPreferences.get("reloadOnExternalChanges", "false").toBoolean
      checkBox.setSelected(enabled)
    }

    def save() = {
      val enabled = checkBox.isSelected
      NetLogoPreferences.put("reloadOnExternalChanges", enabled.toString)
      tabs.watchingFiles = enabled
    }
  }

  object IsLoggingEnabled extends BooleanPreference("loggingEnabled", Some(RequiredAction.Restart), false) {}

  class LogDirectory(val frame: Frame) extends Preference {
    val i18nKey = "logDirectory"
    val requirement = Some(RequiredAction.Restart)
    val textField = new TextField(20)
    val component =
      new JPanel(new BorderLayout(6, 0)) with Transparent with ThemeSync {
        add(textField, BorderLayout.CENTER)

        private val browseButton = new Button(new AbstractAction("Browse...") {
          def actionPerformed(e: ActionEvent): Unit = {
            askForConfigFile(textField.getText).foreach(textField.setText)
          }
        })

        add(browseButton, BorderLayout.EAST)

        override def syncTheme(): Unit = {
          textField.syncTheme()
          browseButton.syncTheme()
        }
      }

    def load() = {
      val logDirectory = NetLogoPreferences.get("logDirectory", "")
      textField.setText(logDirectory)
    }

    def save() = {
      NetLogoPreferences.put("logDirectory", textField.getText)
    }

    def askForConfigFile(current: String): Option[String] = {
      val dialog = new JFileChooser(new File(current))
      dialog.setDialogTitle("Log Directory")
      dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
      if (dialog.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
        val file = dialog.getSelectedFile
        val dir  = if (file.isDirectory) { file } else { dialog.getCurrentDirectory }
        val path = dir.getAbsolutePath
        Option(path)
      } else {
        None
      }
    }

  }

  object LogEvents extends StringPreference("logEvents", Some(RequiredAction.Restart), "")

  class IncludedFilesMenu(tabs: TabsInterface) extends Preference {
    val i18nKey = "includedFilesMenu"
    val requirement = None

    private val checkBox = new CheckBox("", (selected) => {
      tabs.setIncludedFilesShown(selected)
    })

    def component: CheckBox = checkBox

    def load(): Unit = {
      val value = NetLogoPreferences.get(i18nKey, "true").toBoolean

      checkBox.setSelected(value)
      tabs.setIncludedFilesShown(value)
    }

    def save(): Unit = {
      NetLogoPreferences.put(i18nKey, checkBox.isSelected.toString)
    }
  }

  object ProceduresMenuSortOrder extends Preference {
    val i18nKey = "proceduresMenuSortOrder"

    val options = List(
      I18N.gui.get("tools.preferences.proceduresSortByOrderOfAppearance"),
      I18N.gui.get("tools.preferences.proceduresSortAlphabetical")
    )

    val comboBox = new ComboBox(options)
    val requirement = None

    def component: JComponent & ThemeSync = comboBox

    def load(): Unit = {
      val sortOrder = NetLogoPreferences.get("proceduresMenuSortOrder", options(0))
      comboBox.setSelectedItem(sortOrder)
    }

    def save(): Unit = {
      comboBox.getSelectedItem.foreach(NetLogoPreferences.put("proceduresMenuSortOrder", _))
    }
  }

  object FocusOnError extends BooleanPreference("focusOnError", None, true) {}

  object StartSeparateCodeTab extends BooleanPreference("startSeparateCodeTab", None, false) {}

  class BoldWidgetText(widgetPanel: AbstractWidgetPanel) extends Preference {
    val i18nKey = "boldWidgetText"
    val requirement = None

    private val checkBox = new CheckBox("", (selected) => {
      widgetPanel.setBoldWidgetText(selected)
    })

    override def component: CheckBox = checkBox

    def load(): Unit = {
      val value = NetLogoPreferences.get(i18nKey, "true").toBoolean

      checkBox.setSelected(value)
      widgetPanel.setBoldWidgetText(value)
    }

    def save(): Unit = {
      NetLogoPreferences.put(i18nKey, checkBox.isSelected.toString)
    }
  }

  object UIScale extends Preference {
    val i18nKey = "uiScale"
    val requirement = Some(RequiredAction.Restart)
    val textField = new TextField(20, "1.0")

    def component: JComponent & ThemeSync = textField

    def load() = {
      textField.setText(NetLogoPreferences.getDouble(i18nKey, 1.0).toString)
    }

    def save() = {
      NetLogoPreferences.putDouble(i18nKey, textField.getText.toDouble)
    }
  }

  class IndentAutomatically(tabs: TabsInterface) extends Preference {
    val i18nKey = "indentAutomatically"
    val requirement = None

    private val checkBox = new CheckBox("", (selected) => {
      tabs.smartTabbingEnabled = selected
    })

    override def component: CheckBox = checkBox

    def load(): Unit = {
      val value = NetLogoPreferences.get(i18nKey, "true").toBoolean

      checkBox.setSelected(value)
      tabs.smartTabbingEnabled = value
    }

    def save(): Unit = {
      NetLogoPreferences.put(i18nKey, checkBox.isSelected.toString)
    }
  }

  class EditorLineNumbers(tabs: TabsInterface) extends Preference {
    val i18nKey = "editorLineNumbers"
    val requirement = None

    private val checkBox = new CheckBox("", (selected) => {
      tabs.lineNumbersVisible = selected
    })

    override def component: CheckBox = checkBox

    def load(): Unit = {
      val value = NetLogoPreferences.get(i18nKey, "true").toBoolean

      checkBox.setSelected(value)
      tabs.lineNumbersVisible = value
    }

    def save(): Unit = {
      NetLogoPreferences.put(i18nKey, checkBox.isSelected.toString)
    }
  }

  class JumpOnClick(tabs: TabsInterface) extends Preference {
    val i18nKey = "jumpOnClick"
    val requirement = None

    private val checkBox = new CheckBox("", (selected) => {
      tabs.setJumpOnClick(selected)
    })

    override def component: CheckBox = checkBox

    def load(): Unit = {
      val value = NetLogoPreferences.get(i18nKey, "true").toBoolean

      checkBox.setSelected(value)
      tabs.setJumpOnClick(value)
    }

    def save(): Unit = {
      NetLogoPreferences.put(i18nKey, checkBox.isSelected.toString)
    }
  }
}
