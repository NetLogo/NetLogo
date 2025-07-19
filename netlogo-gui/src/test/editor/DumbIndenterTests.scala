// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import javax.swing.JTextArea

import org.scalatest.funsuite.AnyFunSuite

class DumbIndenterTests extends AnyFunSuite {
  private val dummy = new JTextArea
  private val indenter = new DumbIndenter(dummy)

  test("tab and untab with empty text") {
    dummy.setText("")
    dummy.setCaretPosition(0)
    indenter.handleTab()
    assert(dummy.getText == "  ")
    indenter.handleUntab()
    assert(dummy.getText == "")
  }

  test("tab at beginning of line") {
    dummy.setText("test")
    dummy.setCaretPosition(0)
    indenter.handleTab()
    assert(dummy.getText == "  test")
  }

  test("tab in middle of line") {
    dummy.setText("test")
    dummy.setCaretPosition(2)
    indenter.handleTab()
    assert(dummy.getText == "te  st")
  }

  test("tab at end of line") {
    dummy.setText("test")
    dummy.setCaretPosition(4)
    indenter.handleTab()
    assert(dummy.getText == "test  ")
  }

  test("untab at beginning of line") {
    dummy.setText("test")
    dummy.setCaretPosition(0)
    indenter.handleUntab()
    assert(dummy.getText == "test")
  }

  test("untab at beginning of line with indentation") {
    dummy.setText("  test")
    dummy.setCaretPosition(0)
    indenter.handleUntab()
    assert(dummy.getText == "test")
  }

  test("untab at beginning of text with indentation") {
    dummy.setText("  test")
    dummy.setCaretPosition(2)
    indenter.handleUntab()
    assert(dummy.getText == "test")
  }

  test("untab at end of text") {
    dummy.setText("test")
    dummy.setCaretPosition(4)
    indenter.handleUntab()
    assert(dummy.getText == "test")
  }

  test("untab at end of text with indentation") {
    dummy.setText("  test")
    dummy.setCaretPosition(6)
    indenter.handleUntab()
    assert(dummy.getText == "test")
  }

  test("tab with following lines") {
    dummy.setText("test\ntest")
    dummy.setCaretPosition(0)
    indenter.handleTab()
    assert(dummy.getText == "  test\ntest")
  }

  test("tab with previous lines") {
    dummy.setText("test\ntest")
    dummy.setCaretPosition(5)
    indenter.handleTab()
    assert(dummy.getText == "test\n  test")
  }

  test("untab with following lines") {
    dummy.setText("  test\ntest")
    dummy.setCaretPosition(6)
    indenter.handleUntab()
    assert(dummy.getText == "test\ntest")
  }

  test("untab with previous lines") {
    dummy.setText("test\n  test")
    dummy.setCaretPosition(5)
    indenter.handleUntab()
    assert(dummy.getText == "test\ntest")
  }
}
