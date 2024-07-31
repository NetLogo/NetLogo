// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common;

import java.awt.{ BorderLayout, Frame, Toolkit }
import java.awt.event.{ ActionEvent, ActionListener, FocusEvent }
import javax.swing.{ AbstractAction, Action, Box, BoxLayout, JButton, JCheckBox, JComponent, JDialog, JEditorPane,
                     JLabel, JPanel, SwingConstants }
import javax.swing.border.EmptyBorder
import javax.swing.text.{ BadLocationException, JTextComponent, TextAction }

import org.nlogo.core.I18N
import org.nlogo.swing.{ ButtonPanel, NonemptyTextFieldActionEnabler, NonemptyTextFieldButtonEnabler, TextField,
                         TextFieldBox, UserAction, Utils }

object FindDialog {
  class FindAction extends TextAction(I18N.gui.get("menu.edit.find")) {
    putValue(Action.SMALL_ICON, Utils.icon("/images/magnify.gif"))
    putValue(UserAction.ActionCategoryKey, UserAction.EditCategory)
    putValue(UserAction.ActionGroupKey, UserAction.EditFindGroup)
    putValue(Action.ACCELERATOR_KEY, UserAction.KeyBindings.keystroke('F', withMenu = true))

    setEnabled(false)

    def actionPerformed(e: ActionEvent) {
      val codeInstance = getCodeInstance

      if (codeInstance.isVisible)
        codeInstance.setVisible(false)

      val instance = getInstance

      instance.setVisible(true)
      instance.findBox.requestFocus()
      instance.findBox.selectAll()

      val selectedText = instance.target.getSelectedText

      if (selectedText == null)
        instance.findBox.setText(codeInstance.findBox.getText)
      else
        instance.findBox.setText(selectedText)

      instance.setLocation(instance.owner.getLocation.x + instance.owner.getWidth - instance.getPreferredSize.width,
                           instance.owner.getLocation.y + instance.owner.getHeight / 2 -
                           instance.getPreferredSize.height / 2)
      instance.notFoundLabel.setVisible(false)
    }
  }

  class FindNextAction extends TextAction(I18N.gui.get("menu.edit.findNext")) {
    putValue(Action.SMALL_ICON, Utils.icon("/images/magnify.gif"))
    putValue(UserAction.ActionCategoryKey, UserAction.EditCategory)
    putValue(UserAction.ActionGroupKey, UserAction.EditFindGroup)
    putValue(Action.ACCELERATOR_KEY, UserAction.KeyBindings.keystroke('G', withMenu = true))
        
    setEnabled(false)

    def actionPerformed(e: ActionEvent) {
      if (!getInstance.next(getInstance.findBox.getText, getInstance.ignoreCaseCheckBox.isSelected,
                            getInstance.wrapAroundCheckBox.isSelected))
        Toolkit.getDefaultToolkit.beep()
    }
  }

  class FindActionCode extends TextAction(I18N.gui.get("menu.edit.find")) {
    putValue(Action.SMALL_ICON, Utils.icon("/images/magnify.gif"))
    putValue(UserAction.ActionCategoryKey, UserAction.EditCategory)
    putValue(UserAction.ActionGroupKey, UserAction.EditFindGroup)

    setEnabled(false)

    def actionPerformed(e: ActionEvent) {
      val instance = getInstance

      if (instance.isVisible)
        instance.setVisible(false)

      val codeInstance = getCodeInstance

      codeInstance.setVisible(true)
      codeInstance.findBox.requestFocus()
      codeInstance.findBox.selectAll()

      val selectedText = codeInstance.target.getSelectedText

      if (selectedText == null)
        codeInstance.findBox.setText(instance.findBox.getText)
      else
        codeInstance.findBox.setText(selectedText)

      codeInstance.setLocation(codeInstance.owner.getLocation.x + codeInstance.owner.getWidth -
                               codeInstance.getPreferredSize.width, codeInstance.owner.getLocation.y +
                               codeInstance.owner.getHeight / 2 - codeInstance.getPreferredSize.height / 2)
      codeInstance.notFoundLabel.setVisible(false)
    }
  }

  class FindNextActionCode extends TextAction(I18N.gui.get("menu.edit.findNext")) {
    putValue(Action.SMALL_ICON, Utils.icon("/images/magnify.gif"))
    putValue(UserAction.ActionCategoryKey, UserAction.EditCategory)
    putValue(UserAction.ActionGroupKey, UserAction.EditFindGroup)
    putValue(Action.ACCELERATOR_KEY, UserAction.KeyBindings.keystroke('G', withMenu = true))

    setEnabled(false)

    def actionPerformed(e: ActionEvent) {
      if (!getCodeInstance.next(getCodeInstance.findBox.getText, getCodeInstance.ignoreCaseCheckBox.isSelected,
                                getCodeInstance.wrapAroundCheckBox.isSelected))
        Toolkit.getDefaultToolkit.beep()
    }
  }

  class FocusListener extends java.awt.event.FocusListener {
    def focusGained(e: FocusEvent) {
      if (e.getSource.isInstanceOf[JTextComponent])
        watch(e.getSource.asInstanceOf[JTextComponent])
    }

    def focusLost(e: FocusEvent) {
      if (!e.isTemporary && e.getSource.isInstanceOf[JTextComponent])
        dontWatch(e.getSource.asInstanceOf[JTextComponent])
    }
  }

  val FIND_ACTION = new FindAction
  val FIND_NEXT_ACTION = new FindNextAction

  val FIND_ACTION_CODE = new FindActionCode
  val FIND_NEXT_ACTION_CODE = new FindNextActionCode

  private var instance: FindDialog = null
  private var codeInstance: FindDialog = null

  def init(frame: Frame, codeFrame: Frame) {
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

  def watch(target: JTextComponent, code: Boolean = false) {
    if (code) {
      FIND_ACTION_CODE.setEnabled(true)
      getCodeInstance.target = target
      getCodeInstance.setReplaceEnabled(target.isEditable)
    }

    else {
      FIND_ACTION.setEnabled(true)
      getInstance.target = target
      getInstance.setReplaceEnabled(target.isEditable)
    }
  }

  def dontWatch(target: JTextComponent, code: Boolean = false) {
    if (code) {
      getCodeInstance.setVisible(false)
      FIND_ACTION_CODE.setEnabled(false)
    }

    else {
      getInstance.setVisible(false)
      FIND_ACTION.setEnabled(false)
    }
  }
}

class FindDialog(val owner: Frame) extends JDialog(owner, I18N.gui.get("dialog.find.title"), false) with ActionListener {
  private var target: JTextComponent = null

  private val nextButton = new JButton(I18N.gui.get("dialog.find.next"))
  private val prevButton = new JButton(I18N.gui.get("dialog.find.previous"))
  private val replaceButton = new JButton(I18N.gui.get("dialog.find.replace"))
  private val replaceAndFindButton = new JButton(I18N.gui.get("dialog.find.replaceAndFind"))
  private val replaceAllButton = new JButton(I18N.gui.get("dialog.find.replaceAll"))

  private val nextEnabler = new NonemptyTextFieldButtonEnabler(nextButton)
  private val prevEnabler = new NonemptyTextFieldButtonEnabler(prevButton)
  private val replaceEnabler = new NonemptyTextFieldButtonEnabler(replaceButton)
  private val replaceAndFindEnabler = new NonemptyTextFieldButtonEnabler(replaceAndFindButton)
  private val replaceAllEnabler = new NonemptyTextFieldButtonEnabler(replaceAllButton)

  private val ignoreCaseCheckBox = new JCheckBox(I18N.gui.get("dialog.find.ignoreCase"), true)
  private val wrapAroundCheckBox = new JCheckBox(I18N.gui.get("dialog.find.wrapAround"), true)
  private val findBox = new TextField(25)
  private val replaceBox = new TextField(25)
  private val replaceLabel = new JLabel(I18N.gui.get("dialog.find.replaceWith"))
  private val notFoundLabel = new JLabel(I18N.gui.get("dialog.find.notFound"))

  nextButton.addActionListener(this)
  prevButton.addActionListener(this)
  replaceButton.addActionListener(this)
  replaceAndFindButton.addActionListener(this)
  replaceAllButton.addActionListener(this)

  nextEnabler.addRequiredField(findBox)
  prevEnabler.addRequiredField(findBox)
  replaceEnabler.addRequiredField(findBox)
  replaceAndFindEnabler.addRequiredField(findBox)
  replaceAllEnabler.addRequiredField(findBox)

  new NonemptyTextFieldActionEnabler(FindDialog.FIND_NEXT_ACTION).addRequiredField(findBox)
  new NonemptyTextFieldActionEnabler(FindDialog.FIND_NEXT_ACTION_CODE).addRequiredField(findBox)

  findBox.setEditable(true)
  replaceBox.setEditable(true)
  notFoundLabel.setVisible(false)

  getRootPane.setDefaultButton(nextButton)

  setResizable(false)
  setVisible(false)

  locally {
    val findPanel = new TextFieldBox(SwingConstants.LEFT)

    findPanel.setBorder(new EmptyBorder(16, 8, 8, 8))

    findPanel.addField(I18N.gui.get("dialog.find.find"), findBox)
    findPanel.addField(replaceLabel, replaceBox)

    val optionsPanel = new JPanel

    optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.X_AXIS))
    optionsPanel.setBorder(new EmptyBorder(8, 8, 8, 8))

    optionsPanel.add(ignoreCaseCheckBox)
    optionsPanel.add(Box.createHorizontalStrut(12))
    optionsPanel.add(wrapAroundCheckBox)
    optionsPanel.add(Box.createHorizontalStrut(24))
    optionsPanel.add(notFoundLabel)

    val buttonPanel = new ButtonPanel(
      Array[JComponent](
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
      def actionPerformed(e: ActionEvent) {
        setVisible(false)
      }
    })

  def actionPerformed(e: ActionEvent) {
    e.getSource match {
      case `nextButton` | `findBox` =>
        if (!next(findBox.getText, ignoreCaseCheckBox.isSelected, wrapAroundCheckBox.isSelected)) {
          Toolkit.getDefaultToolkit.beep()
          notFoundLabel.setVisible(true)
        } else {
          notFoundLabel.setVisible(false)
        }

      case `prevButton` =>
        if (!prev(findBox.getText, ignoreCaseCheckBox.isSelected, wrapAroundCheckBox.isSelected)) {
          Toolkit.getDefaultToolkit.beep()
          notFoundLabel.setVisible(true)
        } else {
          notFoundLabel.setVisible(false)
        }

      case `replaceButton` =>
        replace(replaceBox.getText)

      case `replaceAndFindButton` =>
        if (target.getSelectedText != null && (
          if (ignoreCaseCheckBox.isSelected)
            target.getSelectedText.equalsIgnoreCase(findBox.getText)
          else
            target.getSelectedText.equals(findBox.getText)
        )) {
          replace(replaceBox.getText)
        }

        if (!next(findBox.getText, ignoreCaseCheckBox.isSelected, wrapAroundCheckBox.isSelected)) {
          Toolkit.getDefaultToolkit.beep()
          notFoundLabel.setVisible(true);
        } else {
          notFoundLabel.setVisible(false);
        }

      case `replaceAllButton` =>
        replaceAll(findBox.getText, ignoreCaseCheckBox.isSelected, replaceBox.getText)

      case _ =>
        notFoundLabel.setVisible(false)
    }
  }

  private def next(search: String, ignoreCase: Boolean, wrapAround: Boolean): Boolean = {
    var searchMut = search
    var text = getTargetText

    if (ignoreCase) {
      // this might get slow with big programs. should be tested. -AZS
      searchMut = searchMut.toUpperCase
      text = text.toUpperCase
    }

    var matchIndex = text.indexOf(searchMut, target.getSelectionEnd)

    if (matchIndex == -1 && wrapAround) {
      text = text.substring(0, target.getSelectionEnd)
      matchIndex = text.indexOf(searchMut)
    }

    if (matchIndex > -1) {
      target.setSelectionStart(matchIndex)
      target.setSelectionEnd(matchIndex + searchMut.length)
      true
    } else {
      false
    }
  }

  private def prev(search: String, ignoreCase: Boolean, wrapAround: Boolean): Boolean = {
    val start = 0.max(target.getSelectionStart - 1)

    var searchMut = search
    var text = getTargetText

    if (ignoreCase) {
      // this might get slow with big programs. should be tested. -AZS
      searchMut = searchMut.toUpperCase
      text = text.toUpperCase
    }

    val matchIndex = text.lastIndexOf(searchMut, start)

    if (matchIndex == -1 && wrapAround)
      text = text.substring(start, text.length)

    if (matchIndex > -1) {
      target.setSelectionStart(matchIndex)
      target.setSelectionEnd(matchIndex + searchMut.length)
      true
    } else {
      false
    }
  }

  private def replace(replacement: String) {
    if (target.getSelectedText == null || target.getSelectedText.isEmpty) {
      Toolkit.getDefaultToolkit.beep()

      return
    }

    try {
      target.getDocument.remove(target.getSelectionStart, target.getSelectionEnd - target.getSelectionStart)
      target.getDocument.insertString(target.getCaretPosition, replacement, null)
    } catch {
      case ex: BadLocationException =>
        Toolkit.getDefaultToolkit.beep()
    }
  }

  private def replaceAll(search: String, ignoreCase: Boolean, replacement: String): Int = {
    target.setSelectionStart(0)
    target.setSelectionEnd(0)

    if (next(search, ignoreCase, false)) {
      var i = 1

      do {
        replace(replacement)
        
        i += 1

        if (i > 50000)
          throw new IllegalStateException("Replace All replaced too many items.")
      } while (next(search, ignoreCase, false)) // never wrap around on replace all

      i
    } else 0
  }

  private def getTargetText: String = {
    if (target.isInstanceOf[JEditorPane]) {
      // we need to get the text this way to avoid returning the HTML
      // tags which screw-up the search - jrn 7/22/05
      try {
        target.getText(0, target.getDocument.getLength)
      } catch {
        case ex: BadLocationException =>
          throw new IllegalStateException(ex)
      }
    } else {
      target.getText
    }
  }

  private def setReplaceEnabled(enabled: Boolean) {
    replaceEnabler.setEnabled(enabled)
    replaceAndFindEnabler.setEnabled(enabled)
    replaceAllEnabler.setEnabled(enabled)
    replaceBox.setEnabled(enabled)
    replaceLabel.setEnabled(enabled)
  }
}
