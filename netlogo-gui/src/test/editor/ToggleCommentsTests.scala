// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import javax.swing.JTextArea

import org.nlogo.util.AnyFunSuiteEx

class ToggleCommentsTests extends AnyFunSuiteEx {
  private val textField = new JTextArea
  private val toggle = () => ToggleComments.perform(textField, textField.getDocument)

  test("Comment empty document") {
    textField.setText("")

    toggle()

    assert(textField.getText == "; ")
  }

  test("Uncomment single whitespace line") {
    textField.setText("; ")

    toggle()

    assert(textField.getText.isEmpty)
  }

  test("Comment current line") {
    textField.setText("to test\n  print 0\nend\n")
    textField.setCaretPosition(13)

    toggle()

    assert(textField.getText == "to test\n  ; print 0\nend\n")
  }

  test("Uncomment current line") {
    textField.setText("to test\n  ; print 0\nend\n")
    textField.setCaretPosition(15)

    toggle()

    assert(textField.getText == "to test\n  print 0\nend\n")
  }

  test("Comment whitespace lines") {
    textField.setText("\n\n\n")
    textField.selectAll()

    toggle()

    assert(textField.getText == "; \n; \n; \n")
  }

  test("Uncomment whitespace lines") {
    textField.setText("; \n; \n; \n")
    textField.selectAll()

    toggle()

    assert(textField.getText == "\n\n\n")
  }

  test("Comment non-empty lines") {
    textField.setText("to test\n  print 0\nend\n")
    textField.selectAll()

    toggle()

    assert(textField.getText == "; to test\n;   print 0\n; end\n")
  }

  test("Uncomment non-empty commented lines") {
    textField.setText("; to test\n;   print 0\n; end\n")
    textField.selectAll()

    toggle()

    assert(textField.getText == "to test\n  print 0\nend\n")
  }

  test("Comment empty and non-emptylines") {
    textField.setText("to test\n  print 0\nend\n\nto test2\n  print 1\nend\n")
    textField.selectAll()

    toggle()

    assert(textField.getText == "; to test\n;   print 0\n; end\n\n; to test2\n;   print 1\n; end\n")
  }

  test("Uncomment empty and non-empty lines") {
    textField.setText("; to test\n;   print 0\n; end\n\n; to test2\n;   print 1\n; end\n")
    textField.selectAll()

    toggle()

    assert(textField.getText == "to test\n  print 0\nend\n\nto test2\n  print 1\nend\n")
  }

  test("Comment commented and uncommented lines") {
    textField.setText("; to test\n;   print 0\n; end\n\nto test2\n  print 1\nend\n")
    textField.selectAll()

    toggle()

    assert(textField.getText == "; ; to test\n; ;   print 0\n; ; end\n\n; to test2\n;   print 1\n; end\n")
  }

  test("Uncomment commented and multi-commented lines") {
    textField.setText("; ; to test\n; ;   print 0\n; ; end\n\n; to test2\n;   print 1\n; end\n")
    textField.selectAll()

    toggle()

    assert(textField.getText == "; to test\n;   print 0\n; end\n\nto test2\n  print 1\nend\n")
  }

  test("Comment lines with varied comment positions") {
    textField.setText("to test\n  ; print 0\nend\n")
    textField.selectAll()

    toggle()

    assert(textField.getText == "; to test\n;   ; print 0\n; end\n")
  }

  test("Uncomment lines with varied comment positions") {
    textField.setText("; to test\n  ; print 0\n; end\n")
    textField.selectAll()

    toggle()

    assert(textField.getText == "to test\n  print 0\nend\n")
  }

  test("Comment lines with comment at end") {
    textField.setText("to test\n  print 0 ; testing one two three\nend\n")
    textField.selectAll()

    toggle()

    assert(textField.getText == "; to test\n;   print 0 ; testing one two three\n; end\n")
  }

  test("Uncomment lines with comment at end") {
    textField.setText("; to test\n;   print 0 ; testing one two three\n; end\n")
    textField.selectAll()

    toggle()

    assert(textField.getText == "to test\n  print 0 ; testing one two three\nend\n")
  }

  test("Comment indented lines") {
    textField.setText("  to test\n    if true [\n      print 0\n      print 1\n      print 2\n    ]\n  end")
    textField.selectAll()

    toggle()

    assert(textField.getText == "  ; to test\n  ;   if true [\n  ;     print 0\n  ;     print 1\n  ;     print 2\n  ;   ]\n  ; end")
  }

  test("Uncomment indented lines") {
    textField.setText("  ; to test\n  ;   if true [\n  ;     print 0\n  ;     print 1\n  ;     print 2\n  ;   ]\n  ; end")
    textField.selectAll()

    toggle()

    assert(textField.getText == "  to test\n    if true [\n      print 0\n      print 1\n      print 2\n    ]\n  end")
  }
}
