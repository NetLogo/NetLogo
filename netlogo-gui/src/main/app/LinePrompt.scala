// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.agent.{Agent, Observer, Turtle, Patch, Link}
import org.nlogo.swing.Implicits._
import java.awt._
import event.{MouseEvent,MouseListener}
import javax.swing._
import org.nlogo.api.I18N

class LinePrompt(commandLine: CommandLine) extends JComponent with MouseListener {

  locally{
    setOpaque(false)
    addMouseListener(this)
    org.nlogo.awt.Fonts.adjustDefaultFont(this)
  }

  private[this] var enabled = true
  override def setEnabled(enabled: Boolean) { this.enabled = enabled; repaint() }

  private var mouseInBounds = false
  def mouseEntered(e: MouseEvent) { mouseInBounds = true; if(enabled) repaint() }
  def mouseExited(e: MouseEvent) { mouseInBounds = false; if(enabled) repaint() }
  def mouseClicked(e: MouseEvent) { }
  def mouseReleased(e: MouseEvent) { }
  def mousePressed(e: MouseEvent) {
    def doPopupMenu() {
      val popMenu = new JPopupMenu("Ask who?")
      def addItem(name: String, clazz: Class[_ <: Agent]) {
        popMenu.add(new JMenuItem(name) {
          addActionListener(() => {
            commandLine.agentClass(clazz)
            LinePrompt.this.repaint()
            commandLine.requestFocus()
          })
        })
      }
      addItem(I18N.gui.get("common.observer"), classOf[Observer])
      addItem(I18N.gui.get("common.turtles"), classOf[Turtle])
      addItem(I18N.gui.get("common.patches"), classOf[Patch])
      addItem(I18N.gui.get("common.links"), classOf[Link])
      popMenu.add(new JPopupMenu.Separator)
      val hintItem = new JMenuItem(I18N.gui.get("tabs.run.commandcenter.orusetabkey")) {setEnabled(false)}
      popMenu.add(hintItem)
      popMenu.show(this, getWidth / 2, getHeight / 2)
    }
    if(enabled) doPopupMenu()
  }

  override def paintComponent(g: Graphics) {
    g.asInstanceOf[Graphics2D].setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

    def drawAgentSymbol(g: Graphics) {
      def getPrompt() = {
        import org.nlogo.agent._
        val O = classOf[Observer]; val T = classOf[Turtle]
        val P = classOf[Patch];    val L = classOf[Link]
        commandLine.agentClass match {
          case O => CommandLine.OBSERVER_PROMPT
          case T => CommandLine.TURTLE_PROMPT
          case P => CommandLine.PATCH_PROMPT
          case L => CommandLine.LINK_PROMPT
        }
      }
      val fontMetrics = g.getFontMetrics
      val halfSymHeight = fontMetrics.getHeight / 2
      val y = (getHeight / 2) + halfSymHeight - fontMetrics.getMaxDescent
      val str = getPrompt
      val width = fontMetrics.stringWidth(str)
      g.drawString(str, getWidth - width, y)
      if(mouseInBounds)
        g.drawLine(getWidth - width, (getHeight / 2) + halfSymHeight,
                   getWidth, (getHeight / 2) + halfSymHeight)
    }

    if(enabled) {
      g.setColor(if (mouseInBounds) Color.BLUE else getForeground)
      drawAgentSymbol(g)
    }
  }

  override def getPreferredSize = getMinimumSize
  override def getMinimumSize = {
    val fm = getFontMetrics(getFont)
    val width =
      if (!enabled) 20
      else math.max(
        fm.stringWidth(CommandLine.OBSERVER_PROMPT),
        math.max(fm.stringWidth(CommandLine.TURTLE_PROMPT),
                 fm.stringWidth(CommandLine.PATCH_PROMPT))
      )
    new Dimension(width, fm.getHeight)
  }
}
