// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import java.awt.{ BorderLayout, Frame, Toolkit }
import java.awt.event.{ ActionEvent, ActionListener, FocusEvent }
import java.util.Locale
import javax.swing.{ AbstractAction, Action, Box, BoxLayout, JDialog, JEditorPane, JLabel, JPanel, SwingConstants }
import javax.swing.border.EmptyBorder
import javax.swing.text.{ BadLocationException, TextAction }

import org.nlogo.core.I18N
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.swing.{ ButtonPanel, CheckBox, DialogButton, NonemptyTextFieldActionEnabler,
                         NonemptyTextFieldButtonEnabler, ScrollableTextComponent, TextField, TextFieldBox, Transparent,
                         UserAction, Utils },
  UserAction.{ EditCategory, EditFindGroup, KeyBindings, MenuAction }

object FindDialog extends ThemeSync {
  class FindAction extends TextAction(I18N.gui.get("menu.edit.find")) with MenuAction {
    category = EditCategory
    group = EditFindGroup
    accelerator = KeyBindings.keystroke('F', withMenu = true)

    setEnabled(false)

    def actionPerformed(e: ActionEvent): Unit = {
      val codeInstance = getCodeInstance

      if (codeInstance.isVisible)
        codeInstance.setVisible(false)

      val instance = getInstance

      instance.setVisible(true)
      instance.findBox.requestFocus()
      instance.findBox.selectAll()

      instance.findBox.setText(instance.target.flatMap(t => Option(t.getSelectedText))
                                              .getOrElse(codeInstance.findBox.getText))

      instance.setLocation(instance.owner.getLocation.x + instance.owner.getWidth - instance.getPreferredSize.width,
                           instance.owner.getLocation.y + instance.owner.getHeight / 2 -
                           instance.getPreferredSize.height / 2)
      instance.notFoundLabel.setVisible(false)
    }
  }

  class FindNextAction extends TextAction(I18N.gui.get("menu.edit.findNext")) with MenuAction {
    category = EditCategory
    group = EditFindGroup
    accelerator = KeyBindings.keystroke('G', withMenu = true)

    setEnabled(false)

    def actionPerformed(e: ActionEvent): Unit = {
      if (!getInstance.next(getInstance.findBox.getText, getInstance.ignoreCaseCheckBox.isSelected,
                            getInstance.wrapAroundCheckBox.isSelected))
        Toolkit.getDefaultToolkit.beep()
    }
  }

  class FindActionCode extends TextAction(I18N.gui.get("menu.edit.find")) with MenuAction {
    category = EditCategory
    group = EditFindGroup

    setEnabled(false)

    def actionPerformed(e: ActionEvent): Unit = {
      val instance = getInstance

      if (instance.isVisible)
        instance.setVisible(false)

      val codeInstance = getCodeInstance

      codeInstance.setVisible(true)
      codeInstance.findBox.requestFocus()
      codeInstance.findBox.selectAll()

      codeInstance.findBox.setText(codeInstance.target.flatMap(t => Option(t.getSelectedText))
                                              .getOrElse(instance.findBox.getText))

      codeInstance.setLocation(codeInstance.owner.getLocation.x + codeInstance.owner.getWidth -
                               codeInstance.getPreferredSize.width, codeInstance.owner.getLocation.y +
                               codeInstance.owner.getHeight / 2 - codeInstance.getPreferredSize.height / 2)
      codeInstance.notFoundLabel.setVisible(false)
    }
  }

  class FindNextActionCode extends TextAction(I18N.gui.get("menu.edit.findNext")) with MenuAction {
    category = EditCategory
    group = EditFindGroup
    accelerator = KeyBindings.keystroke('G', withMenu = true)

    setEnabled(false)

    def actionPerformed(e: ActionEvent): Unit = {
      if (!getCodeInstance.next(getCodeInstance.findBox.getText, getCodeInstance.ignoreCaseCheckBox.isSelected,
                                getCodeInstance.wrapAroundCheckBox.isSelected))
        Toolkit.getDefaultToolkit.beep()
    }
  }

  class FocusListener extends java.awt.event.FocusListener {
    def focusGained(e: FocusEvent): Unit = {
      e.getSource match {
        case s: ScrollableTextComponent =>
          watch(s)

        case _ =>
      }
    }

    def focusLost(e: FocusEvent): Unit = {
      if (!e.isTemporary && e.getSource.isInstanceOf[ScrollableTextComponent])
        dontWatch()
    }
  }

  val FIND_ACTION = new FindAction
  val FIND_NEXT_ACTION = new FindNextAction

  val FIND_ACTION_CODE = new FindActionCode
  val FIND_NEXT_ACTION_CODE = new FindNextActionCode

  private var instance: FindDialog = null
  private var codeInstance: FindDialog = null

  def init(frame: Frame, codeFrame: Frame): Unit = {
    instance = new FindDialog(frame)
    codeInstance = new FindDialog(codeFrame)
  }

  def getInstance: FindDialog = {
    if (instance == null)
      throw new IllegalStateException("FindDialog was never initialized")

    instance
  }

  def getCodeInstance: FindDialog = {
    if (codeInstance == null)
      throw new IllegalStateException("FindDialog was never initialized")

    codeInstance
  }

  def watch(target: ScrollableTextComponent, code: Boolean = false): Unit = {
    if (code) {
      FIND_ACTION_CODE.setEnabled(true)
      getCodeInstance.target = Option(target)
      getCodeInstance.setReplaceEnabled(target.isEditable)
    }

    else {
      FIND_ACTION.setEnabled(true)
      getInstance.target = Option(target)
      getInstance.setReplaceEnabled(target.isEditable)
    }
  }

  def dontWatch(code: Boolean = false): Unit = {
    if (code) {
      getCodeInstance.setVisible(false)
      FIND_ACTION_CODE.setEnabled(false)
    }

    else {
      getInstance.setVisible(false)
      FIND_ACTION.setEnabled(false)
    }
  }

  override def syncTheme(): Unit = {
    if (instance != null)
      instance.syncTheme()

    if (codeInstance != null)
      codeInstance.syncTheme()

    FIND_ACTION.putValue(Action.SMALL_ICON, Utils.iconScaledWithColor("/images/find.png", 15, 15,
                                                                      InterfaceColors.toolbarImage()))
    FIND_ACTION_CODE.putValue(Action.SMALL_ICON, Utils.iconScaledWithColor("/images/find.png", 15, 15,
                                                                           InterfaceColors.toolbarImage()))
    FIND_NEXT_ACTION.putValue(Action.SMALL_ICON, Utils.iconScaledWithColor("/images/find.png", 15, 15,
                                                                           InterfaceColors.toolbarImage()))
    FIND_NEXT_ACTION_CODE.putValue(Action.SMALL_ICON, Utils.iconScaledWithColor("/images/find.png", 15, 15,
                                                                                InterfaceColors.toolbarImage()))
  }
}

class FindDialog(val owner: Frame) extends JDialog(owner, I18N.gui.get("dialog.find.title"), false) with ActionListener
                                                                                                    with ThemeSync {
  private var target: Option[ScrollableTextComponent] = None

  private val nextButton = new DialogButton(false, I18N.gui.get("dialog.find.next"), () => {
    if (!next(findBox.getText, ignoreCaseCheckBox.isSelected, wrapAroundCheckBox.isSelected)) {
      Toolkit.getDefaultToolkit.beep()
      notFoundLabel.setVisible(true)
    } else {
      notFoundLabel.setVisible(false)
    }
  })

  private val prevButton = new DialogButton(false, I18N.gui.get("dialog.find.previous"), () => {
    if (!prev(findBox.getText, ignoreCaseCheckBox.isSelected, wrapAroundCheckBox.isSelected)) {
      Toolkit.getDefaultToolkit.beep()
      notFoundLabel.setVisible(true)
    } else {
      notFoundLabel.setVisible(false)
    }
  })

  private val replaceButton = new DialogButton(false, I18N.gui.get("dialog.find.replace"), () => {
    replace(replaceBox.getText)
  })

  private val replaceAndFindButton = new DialogButton(false, I18N.gui.get("dialog.find.replaceAndFind"), () => {
    target.foreach { t =>
      if (t.getSelectedText != null && (
        if (ignoreCaseCheckBox.isSelected)
          t.getSelectedText.equalsIgnoreCase(findBox.getText)
        else
          t.getSelectedText.equals(findBox.getText)
      )) {
        replace(replaceBox.getText)
      }
    }

    if (!next(findBox.getText, ignoreCaseCheckBox.isSelected, wrapAroundCheckBox.isSelected)) {
      Toolkit.getDefaultToolkit.beep()
      notFoundLabel.setVisible(true);
    } else {
      notFoundLabel.setVisible(false);
    }
  })

  private val replaceAllButton = new DialogButton(false, I18N.gui.get("dialog.find.replaceAll"), () => {
    replaceAll(findBox.getText, ignoreCaseCheckBox.isSelected, replaceBox.getText)
  }: Unit)

  private val findBox = new TextField(25)
  private val replaceBox = new TextField(25)
  private val replaceLabel = new JLabel(I18N.gui.get("dialog.find.replaceWith"))
  private val notFoundLabel = new JLabel(I18N.gui.get("dialog.find.notFound"))

  new NonemptyTextFieldButtonEnabler(nextButton, List(findBox))
  new NonemptyTextFieldButtonEnabler(prevButton, List(findBox))

  private val replaceEnabler = new NonemptyTextFieldButtonEnabler(replaceButton, List(findBox))
  private val replaceAndFindEnabler = new NonemptyTextFieldButtonEnabler(replaceAndFindButton, List(findBox))
  private val replaceAllEnabler = new NonemptyTextFieldButtonEnabler(replaceAllButton, List(findBox))

  private val ignoreCaseCheckBox = new CheckBox(I18N.gui.get("dialog.find.ignoreCase")) {
    setSelected(true)
  }

  private val wrapAroundCheckBox = new CheckBox(I18N.gui.get("dialog.find.wrapAround")) {
    setSelected(true)
  }

  new NonemptyTextFieldActionEnabler(FindDialog.FIND_NEXT_ACTION, List(findBox))
  new NonemptyTextFieldActionEnabler(FindDialog.FIND_NEXT_ACTION_CODE, List(findBox))

  findBox.setEditable(true)
  replaceBox.setEditable(true)
  notFoundLabel.setVisible(false)

  getRootPane.setDefaultButton(nextButton)

  setResizable(false)
  setVisible(false)

  private val findPanel = new TextFieldBox(SwingConstants.LEFT)

  locally {
    findPanel.setBorder(new EmptyBorder(16, 8, 8, 8))

    findPanel.addField(I18N.gui.get("dialog.find.find"), findBox)
    findPanel.addField(replaceLabel, replaceBox)

    val optionsPanel = new JPanel with Transparent

    optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.X_AXIS))
    optionsPanel.setBorder(new EmptyBorder(8, 8, 8, 8))

    optionsPanel.add(ignoreCaseCheckBox)
    optionsPanel.add(Box.createHorizontalStrut(12))
    optionsPanel.add(wrapAroundCheckBox)
    optionsPanel.add(Box.createHorizontalStrut(24))
    optionsPanel.add(notFoundLabel)

    val buttonPanel = new ButtonPanel(
      Seq(
        nextButton,
        prevButton,
        replaceButton,
        replaceAndFindButton,
        replaceAllButton
      ))

    buttonPanel.setBorder(new EmptyBorder(16, 8, 8, 8))

    getContentPane.setLayout(new BorderLayout)

    getContentPane.add(findPanel, BorderLayout.NORTH)
    getContentPane.add(optionsPanel, BorderLayout.CENTER)
    getContentPane.add(buttonPanel, BorderLayout.SOUTH)

    pack()
  }

  Utils.addEscKeyAction(this,
    new AbstractAction {
      def actionPerformed(e: ActionEvent): Unit = {
        setVisible(false)
      }
    })

  def actionPerformed(e: ActionEvent): Unit = {
    e.getSource match {
      case `findBox` =>
        if (!next(findBox.getText, ignoreCaseCheckBox.isSelected, wrapAroundCheckBox.isSelected)) {
          Toolkit.getDefaultToolkit.beep()
          notFoundLabel.setVisible(true)
        } else {
          notFoundLabel.setVisible(false)
        }

      case _ =>
        notFoundLabel.setVisible(false)
    }
  }

  // helper function for centering found or replaced text in the view (Isaac B 7/26/25)
  private def setScrollPosition(offset: Int): Unit = {
    target.foreach { t =>
      t.scrollPane.foreach { pane =>
        val pos = t.modelToView2D(offset)
        val xBar = pane.getHorizontalScrollBar
        val yBar = pane.getVerticalScrollBar

        xBar.setValue((pos.getX - xBar.getVisibleAmount / 2).toInt)
        yBar.setValue((pos.getY - yBar.getVisibleAmount / 2).toInt)
      }
    }
  }

  private def next(search: String, ignoreCase: Boolean, wrapAround: Boolean): Boolean = {
    target.map { t =>
      var searchMut = search
      var text = getTargetText

      if (ignoreCase) {
        // this might get slow with big programs. should be tested. -AZS
        searchMut = searchMut.toUpperCase(Locale.ENGLISH)
        text = text.toUpperCase(Locale.ENGLISH)
      }

      var matchIndex = text.indexOf(searchMut, t.getSelectionEnd)

      if (matchIndex == -1 && wrapAround)
        matchIndex = text.indexOf(searchMut)

      if (matchIndex > -1) {
        setScrollPosition(matchIndex)
        t.setSelectionStart(matchIndex)
        t.setSelectionEnd(matchIndex + searchMut.length)
        true
      } else {
        false
      }
    }.getOrElse(false)
  }

  private def prev(search: String, ignoreCase: Boolean, wrapAround: Boolean): Boolean = {
    target.map { t =>
      var searchMut = search
      var text = getTargetText

      if (ignoreCase) {
        // this might get slow with big programs. should be tested. -AZS
        searchMut = searchMut.toUpperCase(Locale.ENGLISH)
        text = text.toUpperCase(Locale.ENGLISH)
      }

      var matchIndex = text.lastIndexOf(searchMut, t.getSelectionStart - 1)

      if (matchIndex == -1 && wrapAround)
        matchIndex = text.lastIndexOf(searchMut)

      if (matchIndex > -1) {
        setScrollPosition(matchIndex)
        t.setSelectionStart(matchIndex)
        t.setSelectionEnd(matchIndex + searchMut.length)
        true
      } else {
        false
      }
    }.getOrElse(false)
  }

  private def replace(replacement: String): Unit = {
    target.foreach { t =>
      if (t.getSelectedText == null || t.getSelectedText.isEmpty) {
        Toolkit.getDefaultToolkit.beep()
      } else {
        try {
          setScrollPosition(t.getSelectionStart)
          t.getDocument.remove(t.getSelectionStart, t.getSelectionEnd - t.getSelectionStart)
          t.getDocument.insertString(t.getCaretPosition, replacement, null)
        } catch {
          case ex: BadLocationException =>
            Toolkit.getDefaultToolkit.beep()
        }
      }
    }
  }

  private def replaceAll(search: String, ignoreCase: Boolean, replacement: String): Int = {
    target.map { t =>
      t.setSelectionStart(0)
      t.setSelectionEnd(0)

      if (next(search, ignoreCase, false)) {
        var i = 1

        while {
          replace(replacement)

          i += 1

          if (i > 50000)
            throw new IllegalStateException("Replace All replaced too many items.")

          next(search, ignoreCase, false) // never wrap around on replace all
        } do ()

        i
      } else 0
    }.getOrElse(0)
  }

  private def getTargetText: String = {
    target match {
      case Some(editor: JEditorPane) =>
        // we need to get the text this way to avoid returning the HTML
        // tags which screw-up the search - jrn 7/22/05
        try {
          editor.getText(0, editor.getDocument.getLength)
        } catch {
          case ex: BadLocationException =>
            throw new IllegalStateException(ex)
        }

      case Some(t) =>
        t.getText

      case _ => ""
    }
  }

  private def setReplaceEnabled(enabled: Boolean): Unit = {
    replaceEnabler.setEnabled(enabled)
    replaceAndFindEnabler.setEnabled(enabled)
    replaceAllEnabler.setEnabled(enabled)
    replaceBox.setEnabled(enabled)
    replaceLabel.setEnabled(enabled)
  }

  override def syncTheme(): Unit = {
    getContentPane.setBackground(InterfaceColors.dialogBackground())

    nextButton.syncTheme()
    prevButton.syncTheme()
    replaceButton.syncTheme()
    replaceAndFindButton.syncTheme()
    replaceAllButton.syncTheme()

    ignoreCaseCheckBox.setForeground(InterfaceColors.dialogText())
    wrapAroundCheckBox.setForeground(InterfaceColors.dialogText())

    findBox.syncTheme()
    replaceBox.syncTheme()

    replaceLabel.setForeground(InterfaceColors.dialogText())
    notFoundLabel.setForeground(InterfaceColors.dialogText())

    findPanel.syncTheme()
  }
}
