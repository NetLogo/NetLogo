// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.event.ActionEvent
import javax.swing.text.{ DefaultEditorKit, JTextComponent, TextAction }

// helpers for applying correct shortcuts to text components (Isaac B 6/19/25)
object TextActions {
  def applyToComponent(comp: JTextComponent): Unit = {
    comp.getActionMap.put(DefaultEditorKit.previousWordAction, new CorrectPreviousWordAction(comp, false))
    comp.getActionMap.put(DefaultEditorKit.selectionPreviousWordAction, new CorrectPreviousWordAction(comp, true))
    comp.getActionMap.put(DefaultEditorKit.nextWordAction, new CorrectNextWordAction(comp, false))
    comp.getActionMap.put(DefaultEditorKit.selectionNextWordAction, new CorrectNextWordAction(comp, true))
    comp.getActionMap.put(DefaultEditorKit.selectWordAction, new CorrectSelectWordAction(comp))
    comp.getActionMap.put(DefaultEditorKit.deletePrevWordAction, new CorrectDeletePrevWordAction(comp))
    comp.getActionMap.put(DefaultEditorKit.deleteNextWordAction, new CorrectDeleteNextWordAction(comp))
    comp.getActionMap.put(DefaultEditorKit.backwardAction, new CorrectBackwardAction(comp))
    comp.getActionMap.put(DefaultEditorKit.forwardAction, new CorrectForwardAction(comp))
  }

  // copied from org.nlogo.lex.Charset, which is used during lexing
  // to determine valid NetLogo identifiers (Isaac B 12/29/25)
  private def isIdentChar(c: Char): Boolean =
    c.isLetterOrDigit || "_.?=*!<>:#+/%$^'&-".contains(c)

  abstract class ExtendedTextAction(name: String, comp: JTextComponent, select: Boolean) extends TextAction(name) {
    protected var text: String = ""

    // getText makes a copy of the entire text, so each text action caches it before doing all of its computation to
    // prevent unnecessary repeated copying of potentially large strings. (Isaac B 9/14/26)
    override def actionPerformed(e: ActionEvent): Unit = {
      text = comp.getText

      performAction()
    }

    protected def prevChar: Option[Char] = {
      val caret: Int = comp.getCaretPosition

      if (caret > 0) {
        Option(text(caret - 1))
      } else {
        None
      }
    }

    protected def nextChar: Option[Char] = {
      val caret: Int = comp.getCaretPosition

      if (caret < text.size) {
        Option(text(caret))
      } else {
        None
      }
    }

    protected def prevCharType: Option[CharType] =
      prevChar.map(CharType(_))

    protected def nextCharType: Option[CharType] =
      nextChar.map(CharType(_))

    protected def moveCaret(offset: Int): Unit = {
      if (select) {
        comp.moveCaretPosition(comp.getCaretPosition + offset)
      } else {
        comp.setCaretPosition(comp.getCaretPosition + offset)
      }
    }

    protected def performAction(): Unit

    protected sealed abstract trait CharType

    protected object CharType {
      case object Ident extends CharType
      case object Plain extends CharType
      case object Space extends CharType

      def apply(char: Char): CharType = {
        if (char.isWhitespace) {
          CharType.Space
        } else if (isIdentChar(char)) {
          CharType.Ident
        } else {
          CharType.Plain
        }
      }
    }
  }

  class CorrectPreviousWordAction(comp: JTextComponent, select: Boolean)
    extends ExtendedTextAction("previous word", comp, select) {

    override def performAction(): Unit = {
      while (prevChar.exists(_.isWhitespace))
        moveCaret(-1)

      prevCharType.foreach { tpe =>
        while (prevCharType.contains(tpe))
          moveCaret(-1)
      }
    }
  }

  class CorrectNextWordAction(comp: JTextComponent, select: Boolean)
    extends ExtendedTextAction("next word", comp, select) {

    override def performAction(): Unit = {
      while (nextChar.exists(_.isWhitespace))
        moveCaret(1)

      nextCharType.foreach { tpe =>
        while (nextCharType.contains(tpe))
          moveCaret(1)
      }
    }
  }

  class CorrectSelectWordAction(comp: JTextComponent) extends ExtendedTextAction("select word", comp, true) {
    override def performAction(): Unit = {
      val prev: Option[Char] = prevChar
      val next: Option[Char] = nextChar

      // this defines a hierarchy of selection choices that attempts to find the thing that the user most likely wants
      // to select, which is especially important when the action is activated with the caret on a boundary between
      // character types. if there's a word character on either side of the caret, select the word it belongs to. if
      // neither side has a word character but there is another non-space character on either side, select the sequence
      // of non-word, non-space characters it belongs to. otherwise, select the sequence of whitespace characters
      // surrounding the caret. (Isaac B 9/14/26)
      val selectType: CharType = prev.filter(isIdentChar).orElse(next.filter(isIdentChar))
                                     .orElse(prev.filterNot(_.isWhitespace)).orElse(next.filterNot(_.isWhitespace))
                                     .fold(CharType.Space)(CharType(_))

      while (prevCharType.contains(selectType))
        comp.setCaretPosition(comp.getCaretPosition - 1)

      while (nextCharType.contains(selectType))
        moveCaret(1)
    }
  }

  class CorrectDeletePrevWordAction(comp: JTextComponent)
    extends ExtendedTextAction("delete previous word", comp, true) {

    override def performAction(): Unit = {
      if (prevChar.exists(_.isWhitespace))
        moveCaret(-1)

      prevCharType.foreach { tpe =>
        while (prevCharType.contains(tpe))
          moveCaret(-1)
      }

      comp.replaceSelection(null)
      comp.setCaretPosition(comp.getCaretPosition)
    }
  }

  class CorrectDeleteNextWordAction(comp: JTextComponent) extends ExtendedTextAction("delete next word", comp, true) {
    override def performAction(): Unit = {
      val start: Int = comp.getCaretPosition

      if (nextChar.exists(_.isWhitespace))
        moveCaret(1)

      nextCharType.foreach { tpe =>
        while (nextCharType.contains(tpe))
          moveCaret(1)
      }

      comp.replaceSelection(null)
      comp.setCaretPosition(start)
    }
  }

  class CorrectBackwardAction(comp: JTextComponent) extends TextAction("caret backward") {
    def actionPerformed(e: ActionEvent): Unit = {
      if (comp.getSelectionStart == comp.getSelectionEnd) {
        comp.setCaretPosition((comp.getCaretPosition - 1).max(0))
      } else {
        comp.setCaretPosition(comp.getSelectionStart)
      }
    }
  }

  class CorrectForwardAction(comp: JTextComponent) extends TextAction("caret forward") {
    def actionPerformed(e: ActionEvent): Unit = {
      if (comp.getSelectionStart == comp.getSelectionEnd) {
        comp.setCaretPosition((comp.getCaretPosition + 1).min(comp.getText.size))
      } else {
        comp.setCaretPosition(comp.getSelectionEnd)
      }
    }
  }
}
