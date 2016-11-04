// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import javax.swing.text.{ Document, Element }

object RichDocument {
  // document is assumed to be element-divided by lines
  implicit class RichDoc(document: Document) {

    // Takes an offset and returns a line. The first line has index 0.
    def offsetToLine(offset: Int): Int =
      document.getDefaultRootElement.getElementIndex(offset)

    protected def elementAtIndex(i: Int): Option[Element] = {
      val root = document.getDefaultRootElement
      if (root.getElementCount - 1 < i || i < 0) None
      else Some(root.getElement(i))
    }

    // Takes a zero-indexed line and returns the offset from the start of the document
    // to the beginning of the line. If the line does not exist, returns -1.
    def lineToStartOffset(line: Int): Int =
      elementAtIndex(line).map(_.getStartOffset).getOrElse(-1)

    // Takes a zero-indexed line and returns the offset from the start of the document
    // to the end of the line. If the line does not exist, returns -1. Note: The end offset
    // will typically take into account an additional character for the line-break at EOL.
    def lineToEndOffset(line: Int): Int =
      elementAtIndex(line).map(_.getEndOffset).getOrElse(-1)

    // Takes a zero-indexed line and returns the text of that line.
    def getLineText(line: Int): Option[String] = {
      elementAtIndex(line)
        .map(lineElem =>
            document.getText(lineElem.getStartOffset, lineElem.getEndOffset - lineElem.getStartOffset))
    }

    def insertBeforeLinesInRange(startLine: Int, endLine: Int, text: String): Unit = {
      val lineStart = lineToStartOffset(0)
      val root = document.getDefaultRootElement
      for (line <- (startLine max 0) to (endLine min root.getElementCount - 1)) {
        document.insertString(root.getElement(line).getStartOffset, text, null)
      }
    }

    def selectionLineRange(start: Int, end: Int): (Int, Int) = {
      val startLine = offsetToLine(start)
      val endLine = offsetToLine(end)
      if (start == lineToEndOffset(startLine) - 1 && startLine < endLine)
        (startLine + 1, endLine)
      else if (end == lineToStartOffset(endLine) && endLine > startLine)
        (startLine, endLine - 1)
      else
        (startLine, endLine)
    }
  }
}
