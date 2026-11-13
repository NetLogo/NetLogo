// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Component, Font, Frame, GraphicsEnvironment, Insets }
import java.awt.event.ActionEvent
import java.io.File
import java.util.Locale
import javax.swing.{ AbstractAction, JComponent, JFileChooser, JLabel, JPanel }
import javax.swing.event.{ DocumentEvent, DocumentListener }

import org.nlogo.analytics.Analytics
import org.nlogo.app.common.AppTabsInterface
import org.nlogo.core.{ I18N, LocaleContext, NetLogoPreferences }
import org.nlogo.swing.{ BoxAlign, BoxColumn, BoxRow, Button, CheckBox, ComboBox, PopupMenu, TextField, Transparent,
                         Zoomable }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.AbstractWidgetPanel
import org.nlogo.window.Events.AutoIndentEvent

object Preferences {
  abstract class BooleanPreference(key: String, name: String, requirement: Option[RequiredAction], default: Boolean)
    extends Preference(key, name, requirement) {

    val checkBox = new CheckBox("", selected => {
      if (selected != getPreference)
        Analytics.preferenceChange(key, selected.toString)

      onSelect(selected)
    }) with ThemeSync {
      override def syncTheme(): Unit = {}
    }

    override def component: JComponent & ThemeSync =
      checkBox

    private def getPreference: Boolean =
      NetLogoPreferences.getBoolean(key, default)

    override def load(): Unit = {
      val pref = getPreference

      if (checkBox.isSelected != pref)
        checkBox.doClick()
    }

    override def save(): Unit = {
      NetLogoPreferences.putBoolean(key, checkBox.isSelected)
    }

    override def changed: Boolean =
      getPreference != checkBox.isSelected

    override def scramble(): Unit = {
      checkBox.doClick()
    }

    protected def onSelect(selected: Boolean): Unit = {}
  }

  abstract class StringPreference(key: String, name: String, requirement: Option[RequiredAction], default: String)
    extends Preference(key, name, requirement) {

    private var loading = false

    val textField = new TextField(20, default) {
      getDocument.addDocumentListener(new DocumentListener {
        override def changedUpdate(e: DocumentEvent): Unit = changed()
        override def insertUpdate(e: DocumentEvent): Unit = changed()
        override def removeUpdate(e: DocumentEvent): Unit = changed()
      })

      override def getInsets: Insets =
        new Insets(zoom(3), zoom(3), zoom(3), 0)

      private def changed(): Unit = {
        if (!loading && getText != getPreference)
          Analytics.preferenceChange(key, getText)
      }
    }

    override def component: JComponent & ThemeSync =
      textField

    private def getPreference: String =
      NetLogoPreferences.get(key, default)

    override def load(): Unit = {
      loading = true

      textField.setText(getPreference)

      loading = false
    }

    override def save(): Unit = {
      NetLogoPreferences.put(key, textField.getText)
    }

    override def changed: Boolean =
      getPreference != textField.getText
  }

  object Language extends Preference("uiLanguage", loc"User Interface language", Some(RequiredAction.Restart), true) {
    abstract sealed trait LocaleOption

    case object DetectLocale extends LocaleOption {
      override def toString = loc"<detect language>"
    }

    case class LocaleWrapper(val locale: Locale) extends LocaleOption {
      override def toString = locale.getDisplayLanguage(locale)
    }

    private val languages: Seq[LocaleOption] = DetectLocale +: I18N.availableLocales.distinctBy(_.getLanguage)
                                                                   .map(LocaleWrapper(_)).sortBy(_.toString).toSeq

    private val comboBox = new ComboBox(languages) {
      addItemListener(_ => {
        getSelectedItem match {
          case Some(DetectLocale) =>
            val text = Locale.getDefault.getDisplayLanguage

            label.setText(text)

            if (!getSelectedItem.contains(getPreference))
              Analytics.preferenceChange(key, text)

          case _ =>
            label.setText("")

            if (!getSelectedItem.contains(getPreference))
              Analytics.preferenceChange(key, "")
        }
      })
    }

    private val label = new JLabel with Zoomable {
      setBaseFont(getFont.deriveFont(10f))
    }

    private val panel = new BoxColumn(Seq(
      comboBox,
      new BoxRow(label, BoxAlign.End)
    ), 3) with ThemeSync {
      override def syncTheme(): Unit = {
        label.setForeground(InterfaceColors.dialogText())

        comboBox.syncTheme()
      }
    }

    override def component: JComponent & ThemeSync =
      panel

    private def getPreference: LocaleOption =
      I18N.localeFromPreferences.map(LocaleWrapper(_)).getOrElse(DetectLocale)

    override def load(): Unit = {
      comboBox.setSelectedItem(getPreference)
    }

    override def save(): Unit = {
      comboBox.getSelectedItem.foreach(_ match {
        case DetectLocale =>
          NetLogoPreferences.remove("user.language")
          NetLogoPreferences.remove("user.country")

        case LocaleWrapper(locale) =>
          NetLogoPreferences.put("user.language", locale.getLanguage)
          NetLogoPreferences.put("user.country", locale.getCountry)
      })
    }

    override def changed: Boolean =
      !comboBox.getSelectedItem.contains(getPreference)

    override def scramble(): Unit = {
      if (comboBox.getSelectedItem.contains(DetectLocale)) {
        comboBox.setSelectedIndex(1)
      } else {
        comboBox.setSelectedItem(DetectLocale)
      }
    }
  }

  object LoadLastOnStartup
    extends BooleanPreference("loadLastOnStartup", loc"Reload the last used model on startup", None, false)

  class ReloadOnExternalChanges(tabs: AppTabsInterface)
    extends BooleanPreference("reloadOnExternalChanges", loc"Reload model on external changes", None, false) {

    override def onSelect(selected: Boolean): Unit = {
      tabs.watchingFiles = selected
    }
  }

  class EnableRemoteCommands(tabs: AppTabsInterface)
    extends BooleanPreference("enableRemoteCommands", loc"Enable remote commands", None, false) {

    override def onSelect(selected: Boolean): Unit = {
      tabs.remoteCommandsEnabled = selected
    }
  }

  object IsLoggingEnabled
    extends BooleanPreference("loggingEnabled", loc"Enable logging", Some(RequiredAction.Restart), false)

  class LogDirectory(val frame: Frame)
    extends StringPreference("logDirectory", loc"Directory to store logs", Some(RequiredAction.Restart), "") {

    private val panel = new JPanel(new BorderLayout(6, 0)) with Transparent with ThemeSync {
      setFocusable(false)

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

    override def component: JComponent & ThemeSync =
      panel

    private def askForConfigFile(current: String): Option[String] = {
      val dialog = new JFileChooser(new File(current))

      dialog.setDialogTitle("Log Directory")
      dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)

      if (dialog.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
        val file = dialog.getSelectedFile

        if (file.isDirectory) {
          Option(file.getAbsolutePath)
        } else {
          Option(dialog.getCurrentDirectory.getAbsolutePath)
        }
      } else {
        None
      }
    }

    override def scramble(): Unit = {
      if (textField.getText.isEmpty) {
        textField.setText("/Users/stromboli/Documents/logs/")
      } else {
        textField.setText("")
      }
    }
  }

  object LogEvents extends StringPreference("logEvents", loc"Events to log", Some(RequiredAction.Restart), "") {
    override def scramble(): Unit = {
      if (textField.getText.isEmpty) {
        textField.setText("all")
      } else {
        textField.setText("")
      }
    }
  }

  class IncludedFilesMenu(tabs: AppTabsInterface)
    extends BooleanPreference("includedFilesMenu", loc"""Always show "Included Files" menu""", None, true) {

    override def onSelect(selected: Boolean): Unit = {
      tabs.setIncludedFilesShown(selected)
    }
  }

  object ProceduresMenuSortOrder
    extends Preference("proceduresMenuSortOrder", loc"""Sort "Procedures" menu by""", None) {

    private val options = List(loc"Definition order", loc"Alphabetical order")

    private val comboBox = new ComboBox(options) {
      addItemListener(_ => {
        if (!getSelectedItem.contains(getPreference))
          Analytics.preferenceChange(key, getSelectedItem.map(_.toString).orNull)
      })
    }

    override def component: JComponent & ThemeSync =
      comboBox

    private def getPreference: String =
      NetLogoPreferences.get(key, options(0))

    override def load(): Unit = {
      comboBox.setSelectedItem(getPreference)
    }

    override def save(): Unit = {
      comboBox.getSelectedItem.foreach(NetLogoPreferences.put(key, _))
    }

    override def changed: Boolean =
      !comboBox.getSelectedItem.contains(getPreference)

    override def scramble(): Unit = {
      if (comboBox.getSelectedIndex == 0) {
        comboBox.setSelectedIndex(1)
      } else {
        comboBox.setSelectedIndex(0)
      }
    }
  }

  object FocusOnError
    extends BooleanPreference("focusOnError", loc"Automatically jump to source of compilation error", None, true)

  object StartSeparateCodeTab
    extends BooleanPreference("startSeparateCodeTab", loc"Open code tab in separate window on startup", None, false)

  class BoldWidgetText(widgetPanel: AbstractWidgetPanel)
    extends BooleanPreference("boldWidgetText", loc"Bold widget text", None, false) {

    override def onSelect(selected: Boolean): Unit = {
      widgetPanel.setBoldWidgetText(selected)
    }
  }

  class IndentAutomatically(raiser: Component)
    extends BooleanPreference("indentAutomatically", loc"Auto-indent", None, true) {

    override def onSelect(selected: Boolean): Unit = {
      new AutoIndentEvent(selected).raise(raiser)
    }
  }

  class EditorLineNumbers(tabs: AppTabsInterface)
    extends BooleanPreference("editorLineNumbers", loc"Show line numbers", None, true) {

    override def onSelect(selected: Boolean): Unit = {
      tabs.lineNumbersVisible = selected
    }
  }

  class CompleteOnType(tabs: AppTabsInterface)
    extends BooleanPreference("completeOnType", loc"Show auto-complete popup while typing", None, true) {

    override def onSelect(selected: Boolean): Unit = {
      tabs.setCompleteOnType(selected)
    }
  }

  class JumpOnClick(tabs: AppTabsInterface) extends BooleanPreference("jumpOnClick", {
    loc"<html>Jump sliders to click location<br>(instead of incrementing)</html>"
  }, None, true) {

    override def onSelect(selected: Boolean): Unit = {
      tabs.setJumpOnClick(selected)
    }
  }

  object ConfirmStartup
    extends BooleanPreference("confirmStartup", loc"Ask for confirmation before running startup procedure", None, true)

  object SendAnalytics extends BooleanPreference("sendAnalytics", loc"Send anonymous usage statistics", None, false) {
    override def onSelect(selected: Boolean): Unit = {
      Analytics.setPreference(selected)
    }
  }

  class CodeFont(tabs: AppTabsInterface) extends Preference("codeFont", loc"Code font", None) {
    case class FontWrapper(font: Option[Font]) {
      override def toString: String =
        font.fold(loc"Default")(_.getName)
    }

    val DefaultFont = FontWrapper(None)

    private val fonts: Seq[FontWrapper] = {
      DefaultFont +: GraphicsEnvironment.getLocalGraphicsEnvironment.getAvailableFontFamilyNames
        .filterNot(_.startsWith(".")).map(name => FontWrapper(Some(new Font(name, Font.PLAIN, 12)))).toSeq
    }

    private val comboBox = new ComboBox(fonts, false, true) {
      addItemListener(_ => {
        getSelectedItem.foreach { font =>
          if (font != getPreference)
            Analytics.preferenceChange(key, font.toString)

          tabs.setCodeFont(font.font)
        }
      })

      override def getPopup: PopupMenu = {
        val popup: PopupMenu = super.getPopup

        popup.getComponents.zip(fonts).foreach {
          case (zoomable: Zoomable, FontWrapper(Some(font))) =>
            zoomable.setBaseFont(font)

          case _ =>
        }

        popup
      }
    }

    override def component: JComponent & ThemeSync =
      comboBox

    private def getPreference: FontWrapper =
      FontWrapper(Option(NetLogoPreferences.get(key, null)).map(new Font(_, Font.PLAIN, 12)))

    override def load(): Unit = {
      comboBox.setSelectedItem(getPreference)
    }

    override def save(): Unit = {
      comboBox.getSelectedItem.foreach(_.font match {
        case Some(font) =>
          NetLogoPreferences.put(key, font.getName)

        case _ =>
          NetLogoPreferences.remove(key)
      })
    }

    override def changed: Boolean =
      !comboBox.getSelectedItem.contains(getPreference)

    override def scramble(): Unit = {
      if (comboBox.getSelectedIndex == 0) {
        comboBox.setSelectedIndex(1)
      } else {
        comboBox.setSelectedIndex(0)
      }
    }
  }
}
