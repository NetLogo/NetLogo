// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import javax.swing.JTextArea

import org.nlogo.util.AnyFunSuiteEx

// the behavior tested here is mostly based on behaviors seen in VS Code and Vim,
// with slight modifications to make "word" identification more NetLogo-y (Isaac 12/29/25)
class TestTextActions extends AnyFunSuiteEx {
  /// previous word

  test("previous word doesn't explode at boundaries") {
    testState("", 0, previousWord, "", 0)
    testState("    ", 0, previousWord, "    ", 0)
  }

  test("previous word goes to start of current word") {
    testState("test", 4, previousWord, "test", 0)
    testState("test", 2, previousWord, "test", 0)
    testState("test    test", 12, previousWord, "test    test", 8)
    testState("test    test", 10, previousWord, "test    test", 8)
  }

  test("previous word skips trailing whitespace") {
    testState("test    ", 8, previousWord, "test    ", 0)
    testState("test    ", 6, previousWord, "test    ", 0)
  }

  test("previous word recognizes all ident chars") {
    testState("_.?=*!<>:#+/%$^'&-", 18, previousWord, "_.?=*!<>:#+/%$^'&-", 0)
  }

  test("previous word stops at newline") {
    testState("test test\ntest", 14, previousWord, "test test\ntest", 10)
    testState("test test\ntest", 12, previousWord, "test test\ntest", 10)
  }

  test("previous word wraps to previous line") {
    testState("test test\ntest", 10, previousWord, "test test\ntest", 5)
    testState("test test    \ntest", 14, previousWord, "test test    \ntest", 5)
  }

  /// next word

  test("next word doesn't explode at boundaries") {
    testState("", 0, nextWord, "", 0)
    testState("    ", 4, nextWord, "    ", 4)
  }

  test("next word goes to end of current word") {
    testState("test", 0, nextWord, "test", 4)
    testState("test", 2, nextWord, "test", 4)
    testState("test    test", 0, nextWord, "test    test", 4)
    testState("test    test", 2, nextWord, "test    test", 4)
  }

  test("next word skips leading whitespace") {
    testState("    test", 0, nextWord, "    test", 8)
    testState("    test", 2, nextWord, "    test", 8)
  }

  test("next word recognizes all ident chars") {
    testState("_.?=*!<>:#+/%$^'&-", 0, nextWord, "_.?=*!<>:#+/%$^'&-", 18)
  }

  test("next word stops at newline") {
    testState("test test\ntest", 5, nextWord, "test test\ntest", 9)
    testState("test test\ntest", 7, nextWord, "test test\ntest", 9)
  }

  test("next word wraps to next line") {
    testState("test test\ntest", 9, nextWord, "test test\ntest", 14)
    testState("test test    \ntest", 9, nextWord, "test test    \ntest", 18)
  }

  /// select previous word

  test("select previous word doesn't explode at boundaries") {
    testState("", 0, selectPreviousWord, "", 0)
    testState("    ", 0, selectPreviousWord, "    ", 0)
  }

  test("select previous word goes to start of current word") {
    testStateSelected("test", 4, selectPreviousWord, "test", 0, 0, 4)
    testStateSelected("test", 2, selectPreviousWord, "test", 0, 0, 2)
    testStateSelected("test    test", 12, selectPreviousWord, "test    test", 8, 8, 12)
    testStateSelected("test    test", 10, selectPreviousWord, "test    test", 8, 8, 10)
  }

  test("select previous word skips trailing whitespace") {
    testStateSelected("test    ", 8, selectPreviousWord, "test    ", 0, 0, 8)
    testStateSelected("test    ", 6, selectPreviousWord, "test    ", 0, 0, 6)
  }

  test("select previous word recognizes all ident chars") {
    testStateSelected("_.?=*!<>:#+/%$^'&-", 18, selectPreviousWord, "_.?=*!<>:#+/%$^'&-", 0, 0, 18)
  }

  test("select previous word stops at newline") {
    testStateSelected("test test\ntest", 14, selectPreviousWord, "test test\ntest", 10, 10, 14)
    testStateSelected("test test\ntest", 12, selectPreviousWord, "test test\ntest", 10, 10, 12)
  }

  test("select previous word wraps to previous line") {
    testStateSelected("test test\ntest", 10, selectPreviousWord, "test test\ntest", 5, 5, 10)
    testStateSelected("test test    \ntest", 14, selectPreviousWord, "test test    \ntest", 5, 5, 14)
  }

  /// select next word

  test("select next word doesn't explode at boundaries") {
    testState("", 0, selectNextWord, "", 0)
    testState("    ", 4, selectNextWord, "    ", 4)
  }

  test("select next word goes to end of current word") {
    testStateSelected("test", 0, selectNextWord, "test", 4, 0, 4)
    testStateSelected("test", 2, selectNextWord, "test", 4, 2, 4)
    testStateSelected("test    test", 0, selectNextWord, "test    test", 4, 0, 4)
    testStateSelected("test    test", 2, selectNextWord, "test    test", 4, 2, 4)
  }

  test("select next word skips leading whitespace") {
    testStateSelected("    test", 0, selectNextWord, "    test", 8, 0, 8)
    testStateSelected("    test", 2, selectNextWord, "    test", 8, 2, 8)
  }

  test("select next word recognizes all ident chars") {
    testStateSelected("_.?=*!<>:#+/%$^'&-", 0, selectNextWord, "_.?=*!<>:#+/%$^'&-", 18, 0, 18)
  }

  test("select next word stops at newline") {
    testStateSelected("test test\ntest", 5, selectNextWord, "test test\ntest", 9, 5, 9)
    testStateSelected("test test\ntest", 7, selectNextWord, "test test\ntest", 9, 7, 9)
  }

  test("select next word wraps to next line") {
    testStateSelected("test test\ntest", 9, selectNextWord, "test test\ntest", 14, 9, 14)
    testStateSelected("test test    \ntest", 9, selectNextWord, "test test    \ntest", 18, 9, 18)
  }

  /// select word

  test("select word doesn't explode at boundary") {
    testState("", 0, selectWord, "", 0)
  }

  test("select word selects current word") {
    testStateSelected("test", 0, selectWord, "test", 4, 0, 4)
    testStateSelected("test", 2, selectWord, "test", 4, 0, 4)
    testStateSelected("test", 4, selectWord, "test", 4, 0, 4)
    testStateSelected("    test    ", 4, selectWord, "    test    ", 8, 4, 8)
    testStateSelected("    test    ", 6, selectWord, "    test    ", 8, 4, 8)
    testStateSelected("    test    ", 8, selectWord, "    test    ", 8, 4, 8)
  }

  test("select word selects current non-word") {
    testStateSelected("    ", 0, selectWord, "    ", 4, 0, 4)
    testStateSelected("    ", 2, selectWord, "    ", 4, 0, 4)
    testStateSelected("    ", 4, selectWord, "    ", 4, 0, 4)
    testStateSelected("test    test", 5, selectWord, "test    test", 8, 4, 8)
    testStateSelected("test    test", 6, selectWord, "test    test", 8, 4, 8)
    testStateSelected("test    test", 7, selectWord, "test    test", 8, 4, 8)
  }

  test("select word recognizes all ident chars") {
    testStateSelected("_.?=*!<>:#+/%$^'&-", 0, selectWord, "_.?=*!<>:#+/%$^'&-", 18, 0, 18)
  }

  /// delete previous word

  test("delete previous word doesn't explode at boundaries") {
    testState("", 0, deletePreviousWord, "", 0)
    testState("    ", 0, deletePreviousWord, "    ", 0)
  }

  test("delete previous word goes to start of current word") {
    testState("test", 4, deletePreviousWord, "", 0)
    testState("test", 2, deletePreviousWord, "st", 0)
    testState("test    test", 12, deletePreviousWord, "test    ", 8)
    testState("test    test", 10, deletePreviousWord, "test    st", 8)
  }

  test("delete previous word does not skip trailing whitespace") {
    testState("test    ", 8, deletePreviousWord, "test", 4)
    testState("test    ", 6, deletePreviousWord, "test  ", 4)
  }

  test("delete previous word recognizes all ident chars") {
    testState("_.?=*!<>:#+/%$^'&-", 18, deletePreviousWord, "", 0)
  }

  test("delete previous word stops at newline") {
    testState("test test\ntest", 14, deletePreviousWord, "test test\n", 10)
    testState("test test\ntest", 12, deletePreviousWord, "test test\nst", 10)
  }

  test("delete previous word wraps to previous line") {
    testState("test test\ntest", 10, deletePreviousWord, "test testtest", 9)
    testState("test test    \ntest", 14, deletePreviousWord, "test testtest", 9)
  }

  /// delete next word

  test("delete next word doesn't explode at boundaries") {
    testState("", 0, deleteNextWord, "", 0)
    testState("    ", 4, deleteNextWord, "    ", 4)
  }

  test("delete next word goes to end of current word") {
    testState("test", 0, deleteNextWord, "", 0)
    testState("test", 2, deleteNextWord, "te", 2)
    testState("test    test", 0, deleteNextWord, "    test", 0)
    testState("test    test", 2, deleteNextWord, "te    test", 2)
  }

  test("delete next word does not skip leading whitespace") {
    testState("    test", 0, deleteNextWord, "test", 0)
    testState("    test", 2, deleteNextWord, "  test", 2)
  }

  test("delete next word recognizes all ident chars") {
    testState("_.?=*!<>:#+/%$^'&-", 0, deleteNextWord, "", 0)
  }

  test("delete next word stops at newline") {
    testState("test test\ntest", 5, deleteNextWord, "test \ntest", 5)
    testState("test test\ntest", 7, deleteNextWord, "test te\ntest", 7)
  }

  test("delete next word wraps to next line") {
    testState("test test\ntest", 9, deleteNextWord, "test testtest", 9)
    testState("test test    \ntest", 9, deleteNextWord, "test testtest", 9)
  }

  /// backward

  test("backward doesn't explode at boundaries") {
    testState("", 0, backward, "", 0)
    testState("    ", 0, backward, "    ", 0)
  }

  test("backward goes to beginning of selection") {
    testState("test", 4, () => {
      selectPreviousWord()
      backward()
    }, "test", 0)

    testState("test", 0, () => {
      selectNextWord()
      backward()
    }, "test", 0)
  }

  /// forward

  test("forward doesn't explode at boundaries") {
    testState("", 0, forward, "", 0)
    testState("    ", 4, forward, "    ", 4)
  }

  test("forward goes to end of selection") {
    testState("test", 4, () => {
      selectPreviousWord()
      forward()
    }, "test", 4)

    testState("test", 0, () => {
      selectNextWord()
      forward()
    }, "test", 4)
  }

  /// test helpers

  private val comp = new JTextArea

  private val previousWord = () => new TextActions.CorrectPreviousWordAction(comp, false).actionPerformed(null)
  private val nextWord = () => new TextActions.CorrectNextWordAction(comp, false).actionPerformed(null)
  private val selectPreviousWord = () => new TextActions.CorrectPreviousWordAction(comp, true).actionPerformed(null)
  private val selectNextWord = () => new TextActions.CorrectNextWordAction(comp, true).actionPerformed(null)
  private val selectWord = () => new TextActions.CorrectSelectWordAction(comp).actionPerformed(null)
  private val deletePreviousWord = () => new TextActions.CorrectDeletePrevWordAction(comp).actionPerformed(null)
  private val deleteNextWord = () => new TextActions.CorrectDeleteNextWordAction(comp).actionPerformed(null)
  private val backward = () => new TextActions.CorrectBackwardAction(comp).actionPerformed(null)
  private val forward = () => new TextActions.CorrectForwardAction(comp).actionPerformed(null)

  private def testState(startText: String, startCaret: Int, action: () => Unit, endText: String,
                        endCaret: Int): Unit = {
    comp.setText(startText)
    comp.setCaretPosition(startCaret)

    action()

    assert(comp.getText == endText)
    assert(comp.getCaretPosition == endCaret)
    assert(comp.getSelectedText == null)
  }

  private def testStateSelected(startText: String, startCaret: Int, action: () => Unit, endText: String,
                                endCaret: Int, startSelected: Int, endSelected: Int): Unit = {
    comp.setText(startText)
    comp.setCaretPosition(startCaret)

    action()

    assert(comp.getText == endText)
    assert(comp.getCaretPosition == endCaret)
    assert(comp.getSelectionStart == startSelected)
    assert(comp.getSelectionEnd == endSelected)
  }
}
