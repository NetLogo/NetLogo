// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import javax.swing.SwingConstants
import javax.swing.text.{ JTextComponent, NavigationFilter, Position }

object StickyLineEndNavigationFilter {
  class Logic {
    var savedOffset = Option.empty[Int]
    var offsetValidIndex = Option.empty[Int]

    def move(ea: EditorAreaWrapper, fromPos: Int, endPos: Int): Int = {
      val targetLineNum = ea.offsetToLine(endPos)
      val targetLine = ea.getLineOfText(targetLineNum)
      if (targetLine.trim.isEmpty) {
        val sourceLineNum = ea.offsetToLine(fromPos)
        val lineEndOffset = ea.lineToEndOffset(targetLineNum) - 1
        if (ea.getLineOfText(sourceLineNum).trim.nonEmpty) {
          savedOffset = Some(fromPos - ea.lineToStartOffset(sourceLineNum))
        }
        offsetValidIndex = Some(lineEndOffset)
        lineEndOffset
      } else if (offsetValidIndex.contains(fromPos)) {
        val offset = savedOffset.get
        val targetLineStart = ea.lineToStartOffset(targetLineNum)
        val targetLineEnd = ea.lineToEndOffset(targetLineNum)
        offsetValidIndex = None
        (targetLineStart + offset) min (targetLineEnd - 1)
      } else {
        endPos
      }
    }
  }
}

import StickyLineEndNavigationFilter.Logic

class StickyLineEndNavigationFilter(delegate: NavigationFilter) extends NavigationFilter {
  val logic = new Logic()

  override def getNextVisualPositionFrom(
    text: JTextComponent,
    pos: Int,
    bias: Position.Bias,
    direction: Int,
    biasRet: Array[Position.Bias]): Int = {
      val delegateNVPF = delegate.getNextVisualPositionFrom(text, pos, bias, direction, biasRet)
      if (direction == SwingConstants.EAST || direction == SwingConstants.WEST)
        delegateNVPF
      else
        logic.move(new EditorAreaWrapper { val textComponent: JTextComponent = text }, pos, delegateNVPF)
  }
  override def moveDot(fb: NavigationFilter.FilterBypass, dot: Int, bias: Position.Bias): Unit = {
    delegate.moveDot(fb, dot, bias)
  }
  override def setDot(fb: NavigationFilter.FilterBypass, dot: Int, bias: Position.Bias): Unit = {
    delegate.setDot(fb, dot, bias)
  }

}
