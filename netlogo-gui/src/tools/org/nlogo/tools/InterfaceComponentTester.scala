// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tools

import java.awt.BorderLayout
import org.nlogo.api.{ NetLogoListener, CompilerException , StringUtils }
import org.nlogo.lite.InterfaceComponent
import org.nlogo.util.SysInfo
import org.nlogo.awt
import awt.UserCancelException
import javax.swing.{Action, AbstractAction, JOptionPane, JFrame, JButton, JPanel}

object InterfaceComponentTester {
  def main(args: Array[String]) {
    invokeLater { new InterfaceComponentTester } }
  def invokeLater(body: => Unit) {
    awt.EventQueue.invokeLater(
      new Runnable { override def run() {
        body }})}
}

class InterfaceComponentTester extends JFrame {

  val models = "models/Sample Models/"
  val machines = models + "Art/Sound Machines.nlogo"
  val gaslab = models + "Chemistry & Physics/GasLab/GasLab Free Gas.nlogo"
  val comp = new InterfaceComponent(frame)
  comp.listenerManager.addListener(new NoisyListener)
  def frame = awt.Hierarchy.getFrame(this)

  getContentPane.setLayout(new BorderLayout)
  getContentPane.add(comp, BorderLayout.CENTER)
  getContentPane.add(controlPanel, BorderLayout.SOUTH)
  pack()
  openModelAction(machines).actionPerformed(null)
  setVisible(true)

  lazy val controlPanel = {
    def makeButton(action: Action) =
      new JButton(action) { putClientProperty("JComponent.sizeVariant", "small") }
    def buttonPanel(actions: Action*) =
      new JPanel { for(a <- actions) add(makeButton(a)) }
    val panel = new JPanel
    panel.setLayout(new BorderLayout)
    val panel1 = buttonPanel(
      setProceduresAction, compileAction, pressButtonAction, hideWidgetAction,
      showWidgetAction, viewImageAction, dumpAction, plotImageAction)
    val panel2 = buttonPanel(
      exportWorldAction, importWorldAction, reportAndCallbackAction, commandAction,
      reporterAction, openModelAction(machines), openModelAction(gaslab),
      openForeverAction(gaslab))
    panel.add(panel1, BorderLayout.NORTH)
    panel.add(panel2, BorderLayout.SOUTH)
    panel
  }

  def action(name: String)(body: => Unit) =
    new AbstractAction(name) {
      override def actionPerformed(e: java.awt.event.ActionEvent) {
        body
      }}

  def pressButtonAction = action("press button") {
    comp.pressButton(ask("Enter a button name:"))
  }
  def setProceduresAction = action("set procedures") {
    comp.setProcedures(ask("Enter some code:"))
  }
  def commandAction = action("command") {
    val code = ask("Enter a command:")
    onNewThread { comp.command(code) }
  }
  def reporterAction = action("reporter") {
    val code = ask("Enter a reporter:")
    onNewThread { println(comp.report(code)) }
  }
  def openModelAction(path: String) = action("open") {
    comp.open(path)
    pack()
  }
  var count = 0
  def openForeverAction(path: String): Action = action("open forever") {
    openModelAction(path).actionPerformed(null)
    count += 1
    println(count)
    println(SysInfo.getMemoryInfoString)
    onNewThread {
      comp.command("setup display")
      InterfaceComponentTester.invokeLater { openForeverAction(path).actionPerformed(null) }
    }
  }
  def compileAction = action("compile") {
    comp.compile()
  }
  def hideWidgetAction = action("hide widget") {
    comp.hideWidget(ask("Enter the widget name:"))
  }
  def showWidgetAction = action("show widget") {
    comp.showWidget(ask("Enter the widget name:"))
  }
  def viewImageAction = action("view image") {
    ignoring(classOf[UserCancelException]) {
      import java.io._
      val image = comp.getViewImage
      val filename = org.nlogo.swing.FileDialog.show(
        frame, "Choose File", java.awt.FileDialog.SAVE)
      val stream = new FileOutputStream(new File(filename))
      javax.imageio.ImageIO.write(image, "PNG", stream)
      stream.close()
    }
  }
  def dumpAction = action("dump") {
    onNewThread { comp.command("__stdout __dump") }
  }
  def exportWorldAction = action("export world") {
    import java.io._
    val sw = new StringWriter
    comp.exportWorld(new PrintWriter(sw))
    println(sw.toString)
  }
  def importWorldAction = action("import world") {
    import java.io._
    comp.workspace.importWorld(new StringReader(
      """|GLOBALS
         |min-pxcor,max-pxcor,min-pycor,max-pycor,perspective,subject
         |-3,3,-3,3,0,nobody
         |
         |TURTLES
         |who,color,heading,xcor,ycor,shape,label,label-color,breed,hidden?,size,pen-size,pen-mode
         |0,9,0,0,0,"default","",9.9,{all-turtles},false,1,1,"up"
         |
         |PATCHES
         |pxcor,pycor,pcolor,plabel,plabel-color
         |
         |LINKS
         |end1,end2,color,label,label-color,hidden?,breed,thickness,shape,tie-mode
         |
         |""".stripMargin.replaceAll("\"", "\"\"\"")
      ))
    onNewThread {
      val (t, p) = (comp.report("count turtles"), comp.report("count patches"))
      assert(p == Double.box(49), p)
      assert(t == Double.box(1), t)
      comp.command("ask patches [ set pcolor item ((pxcor + pycor) mod 2) [black gray] ]")
      println("success!")
    }
  }
  def plotImageAction = action("plot image") {
    ignoring(classOf[UserCancelException]) {
      val plot = ask("Enter the plot name:")
      val image = comp.getPlotContentsAsImage(plot)
      val filename = org.nlogo.swing.FileDialog.show(
        frame, "Choose File", java.awt.FileDialog.SAVE)
      val stream = new java.io.FileOutputStream(new java.io.File(filename))
      javax.imageio.ImageIO.write(image, "PNG", stream)
      stream.close()
    }
  }
  def reportAndCallbackAction = action("callback") {
    comp.reportAndCallback(
      ask("Enter some code:"),
      new InterfaceComponent.InvocationListener {
        override def handleResult(value: AnyRef) {
          println("received:  " + value.toString)
        }
        override def handleError(e: org.nlogo.api.CompilerException) {
          println(e.getMessage)
        }
      }
    )
  }

  ///

  def ask(msg: String): String =
    JOptionPane.showInputDialog(frame, msg, "NetLogo",
                                JOptionPane.QUESTION_MESSAGE,
                                null, null, "")
    .asInstanceOf[String]

  def onNewThread(body: => Unit) {
    new Thread(new Runnable { override def run() { body } } ).start()
  }

  def ignoring(cs: Class[_]*)(body: =>Unit) {
    try body
    catch {
      case t: Throwable =>
        if(!cs.exists(_.isInstance(t))) throw t
    }
  }

}

class NoisyListener extends NetLogoListener {
  def truncate(s: String) =
    if(s.size <= 10) s
    else s.take(10) + "..."
  def hey(s: String, xs: Any*) = {
    val args =
      xs.map(x => if(x == null) "null"
                  else truncate(StringUtils.escapeString(x.toString)))
        .mkString("(", ", ", ")")
    println("==> " + s + args)
  }
  override def modelOpened(name: String) =
    hey("modelOpened", name)
  override def buttonPressed(buttonName: String) =
    hey("buttonPressed", buttonName)
  override def buttonStopped(buttonName: String) =
    hey("buttonStopped", buttonName)
  override def sliderChanged(name: String, value: Double, min: Double, increment: Double, max: Double, valueChanged: Boolean, buttonReleased: Boolean) =
    hey("sliderChanged", name, value, min, increment, max, valueChanged, buttonReleased)
  override def switchChanged(name: String, value: Boolean, valueChanged: Boolean) =
    hey("switchChanged", name, value, valueChanged)
  override def chooserChanged(name: String, value: AnyRef, valueChanged: Boolean) =
    hey("chooserChanged", name, value, valueChanged)
  override def inputBoxChanged(name: String, value: AnyRef, valueChanged: Boolean) =
    hey("inputBoxChanged", name, value, valueChanged)
  override def commandEntered(owner: String, text: String, agentType: Char, errorMsg: CompilerException) =
    hey("commandEntered", owner, text, agentType, errorMsg)
  override def codeTabCompiled(text: String, errorMsg: CompilerException) =
    hey("codeTabCompiled", text, errorMsg)
  override def tickCounterChanged(ticks: Double) =
    hey("tickCounterChanged", ticks)
  override def possibleViewUpdate() =
    hey("possibleViewUpdate")
}
