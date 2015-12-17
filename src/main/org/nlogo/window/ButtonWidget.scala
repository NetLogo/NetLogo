// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{List=>AWTList, _}
import event.{MouseEvent, MouseListener, MouseMotionListener}
import image.FilteredImageSource
import org.nlogo.awt.DarkenImageFilter
import javax.swing.ImageIcon
import org.nlogo.util.MersenneTwisterFast
import org.nlogo.awt.Mouse.hasButton1
import org.nlogo.agent.{Agent, Observer, Turtle, Patch, Link}
import org.nlogo.nvm.Procedure
import org.nlogo.api.{I18N, Editable, ModelReader, Options, Version}
import scala.language.existentials

object ButtonWidget {

  def image(path: String) = new ImageIcon(classOf[ButtonWidget].getResource(path))

  val FOREVER_GRAPHIC_DARK: ImageIcon = image("/images/forever.gif")
  val FOREVER_GRAPHIC: ImageIcon = image("/images/forever2.gif")

  object ButtonType {

    // the 4 possible button types
    val ObserverButton = ButtonType("observer", classOf[Observer], img = None, darkImg = None)
    val TurtleButton = ButtonType("turtle", classOf[Turtle], "/images/turtle.gif")
    val LinkButton = ButtonType("link", classOf[Link], "/images/link.gif")
    val PatchButton = ButtonType("patch", classOf[Patch], "/images/patch.gif")

    val buttonTypes = List(ObserverButton, TurtleButton, LinkButton, PatchButton)

    def darkImage(image: ImageIcon) = new ImageIcon(java.awt.Toolkit.getDefaultToolkit.createImage(
      new FilteredImageSource(image.getImage.getSource, new DarkenImageFilter(0.5))))

    private def apply(headerCode:String, agentClass:Class[_ <: Agent], imagePath: String): ButtonType = {
      val img = image(imagePath)
      new ButtonType(headerCode, agentClass, Some(img), Some(darkImage(img)))
    }
    def apply(c:Class[_ <: Agent]): ButtonType = {
      buttonTypes.find(_.agentClass == c).getOrElse(ObserverButton) //TODO or should we say error("bad agent class")
    }
    def apply(name:String): ButtonType = {
      buttonTypes.find(_.name == name).getOrElse(ObserverButton) //TODO or should we say error("bad agent name")
    }
    def getAgentClass(name:String) = {
      if(name == "NIL") ObserverButton.agentClass
      //TODO or should we say error("bad agent name")
      buttonTypes.find(_.name == name).map(_.agentClass).getOrElse(ObserverButton.agentClass)
    }

    // used for the dropdown in the button editor in the UI.
    def defaultAgentOptions = new Options[String](){
      implicit val i18nPrefix = I18N.Prefix("common")
      addOption(I18N.gui("observer"), ButtonType.ObserverButton.name)
      addOption(I18N.gui("turtles"), ButtonType.TurtleButton.name)
      addOption(I18N.gui("patches"), ButtonType.PatchButton.name)
      addOption(I18N.gui("links"), ButtonType.LinkButton.name)
    }
  }
  // encapsulates what used to be a bunch of 4 way if statements.
  // ButtonWidget now has a single ButtonType object that handles all this logic for it.
  case class ButtonType(name: String, agentClass:Class[_ <: Agent],
                        img:Option[ImageIcon], darkImg:Option[ImageIcon]){
    def img(dark:Boolean): Option[ImageIcon] = if(dark) darkImg else img
    def toHeaderCode = "__" + name.toLowerCase + "code "
    def toAgentOptions = {
      val opts = ButtonType.defaultAgentOptions
      opts.selectValue(name)
      opts
    }
  }
}
class ButtonWidget(random:MersenneTwisterFast) extends JobWidget(random)
        with Editable with MouseListener with MouseMotionListener
        with Events.JobRemovedEvent.Handler with Events.TickStateChangeEvent.Handler {

  import ButtonWidget._

  private var buttonType: ButtonType = ButtonType.ObserverButton

  locally {
    addMouseListener(this)
    addMouseMotionListener(this)
    setBackground(InterfaceColors.BUTTON_BACKGROUND)
    setBorder(widgetBorder)
    org.nlogo.awt.Fonts.adjustDefaultFont(this)
  }

  // buttonType now controls the agentClass. no one should ever be setting
  // agentClass from outside of this class anyway.
  // the ui edits work through agent options, which now just set the button type
  override def agentClass = buttonType.agentClass
  override def agentClass(c:Class[_ <: Agent]) { /** ignoring, no one should call this. */ }
  def agentOptions = buttonType.toAgentOptions
  def agentOptions(newAgentOptions:Options[String]){
    if (newAgentOptions.chosenValue != this.agentOptions.chosenValue){
      this.buttonType = ButtonType(newAgentOptions.chosenValue)
      recompile()
      repaint()
    }
  }

  var foreverOn = false
  var goTime = false
  var setupFinished = false

  private var _buttonUp = true
  def buttonUp = _buttonUp
  def buttonUp_=(newButtonUp:Boolean){
    if(newButtonUp) foreverOn = false
    _buttonUp = newButtonUp
    if(buttonUp) setBorder(widgetBorder)
    else{
      setBorder(widgetPressedBorder)
      // this is an attempt to get the button to invert for at least
      // a fraction of a second when a keyboard shortcut is used on
      // a once button - ST 8/6/04
      paintImmediately(0, 0, getWidth, getHeight)
    }
  }

  protected var _forever = false
  def forever = _forever
  def forever_=(newForever: Boolean){
    if(newForever != _forever){
      _forever = newForever
      stopping = false
      recompile()
      repaint()
    }
  }

  /// keyboard stuff
  private var _actionKey: Option[Char] = None
  def actionKey = _actionKey.getOrElse(0.toChar)
  def actionKey_=(newActionKey:Char) {
    _actionKey = newActionKey match {
      case 0 => None
      case _ => Some(newActionKey)
    }
  }
  private def actionKeyString = _actionKey.map(_.toString).getOrElse("")

  private var _keyEnabled = false
  def keyEnabled = _keyEnabled
  def keyEnabled(newKeyEnabled:Boolean){
    if(_keyEnabled != newKeyEnabled){
      _keyEnabled = newKeyEnabled
      repaint()
    }
  }

  def keyTriggered(){
    if (error == null){
      buttonUp = false
      respondToClick(true)
    }
  }

  /// mouse handlers

  // This is used so that when the user right or control clicks
  // on a button with a syntax error, we know not to bring up
  // the edit dialog for the button, as we would if they did
  // a normal click. - ST 1/3/06
  private var lastMousePressedWasPopupTrigger = false

  def mouseReleased(e:MouseEvent){
    if (error == null && ! e.isPopupTrigger() && isEnabled() &&
            ! lastMousePressedWasPopupTrigger && ! disabledWaitingForSetup){
      e.translatePoint(getX(), getY())
      respondToClick(getBounds().contains(e.getPoint()))
    }
  }

  private def disabledWaitingForSetup = goTime && ! setupFinished

  private def respondToClick(inBounds: Boolean) {
    if(disabledWaitingForSetup){
      buttonUp = true
    }
    else if (error == null) {
      if (forever) {
        if (inBounds) {
          foreverOn = !foreverOn
          buttonUp = !foreverOn
          action()
        }
        else buttonUp = !foreverOn
      }
      else {
        buttonUp = true
        if (inBounds) action()
      }
    }
  }

  def mousePressed(e: MouseEvent) {
    new Events.InputBoxLoseFocusEvent().raise(this)
    lastMousePressedWasPopupTrigger = e.isPopupTrigger()
    if (error == null && !e.isPopupTrigger && hasButton1(e) && isEnabled && !disabledWaitingForSetup) buttonUp = false
  }

  def mouseDragged(e: MouseEvent) {
    if (error == null){
      if (hasButton1(e) && isEnabled) {
        e.translatePoint(getX(), getY())
        if (getBounds().contains(e.getPoint()) && !e.isPopupTrigger && ! disabledWaitingForSetup) buttonUp = false
        else if (!forever || !foreverOn) buttonUp = true
      }
    }
  }

  def mouseEntered(e:MouseEvent) {}
  def mouseExited(e:MouseEvent) {}
  def mouseMoved(e: MouseEvent) {}
  def mouseClicked(e: MouseEvent) {
    if (!e.isPopupTrigger() && error != null && !lastMousePressedWasPopupTrigger && hasButton1(e))
      new Events.EditWidgetEvent(this).raise(this)
  }

  /// editability
  override def classDisplayName = I18N.gui.get("tabs.run.widgets.button")
  def propertySet = Properties.button

  /// compilation & jobs
  var running = false
  var stopping = false
  override def isButton = true
  override def isTurtleForeverButton = buttonType == ButtonType.TurtleButton && forever
  override def isLinkForeverButton = buttonType == ButtonType.LinkButton && forever
  private var _name = ""
  def name = _name
  def name_=(newName:String){
    _name = newName
    chooseDisplayName
  }

  override def procedure(p: Procedure) {
    super.procedure(p)
  }

  def action() {
    if (error == null) {
      // warning, confusing code ahead. not sure if there's a
      // clearer way to write this hard to know without trying.
      // it looks like maybe the forever button and the once button
      // cases should be completely separate. that might help
      // - ST 4/28/10

      // comments below added by - JC 9/16/10

      // handle a click if this button is a forever button,
      // or its a once button thats not running
      // this means we don't process clicks on once buttons that are already running.
      if (forever || !running) {

        // if its a once button or a forever button that is running
        // signal to the user that the button is in the process of stopping.
        if (!forever || running) {
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR))
        }

        // if it's a forever button that is running, that means we need to stop it.
        if (forever && running) {
          // the mouseReleased method in ButtonWidget will have prematurely
          // popped the button back up... so first we immediately undo what it did
          foreverOn = true
          buttonUp = false
          // then we mark the job for stopping -- the button will pop back up
          // when the job stops
          stopping = true
        }
        else {
          // in this case, it could be a forever button, but its not running
          // or it could be a once button that is not running.
          // remember, we couldn't have gotten into this if statement
          // if it was a once button that was already running.
          // so we've definitely clicked on a button that was up, and its time to run it.
          stopping = false
          running = true
        }

        // http://ccl.northwestern.edu/netlogo/docs/programming.html#buttons :
        // Forever buttons keep running their code over and over again,
        // until either the code hits the stop command, or you press the button again to stop it.
        // If you stop the button, the code doesn't get interrupted.
        // The button waits until the code has finished, then pops up.

        // if this is a forever button that was running, was clicked, and is now up.
        if (forever && buttonUp) {
          new Events.RemoveJobEvent(this).raise(this)
        }
        // a forever button that was stopped with the stop command.
        else if (forever && !buttonUp && stopping) {
          new Events.JobStoppingEvent(this).raise(this)
        }
        // a forever button or a once button that is now down because
        // it was just clicked.  it needs to run.
        else {
          new Events.AddJobEvent(this, agents(), procedure()).raise(this)
          if(Version.isLoggingEnabled)
            org.nlogo.log.Logger.logButtonPressed(displayName)
        }
      }
    }
  }

  def handle(e: Events.JobRemovedEvent) {
    if (e.owner == this) {
      if(Version.isLoggingEnabled)
        org.nlogo.log.Logger.logButtonStopped(displayName, buttonUp, stopping)
      popUpStoppingButton()
    }
  }

  def handle(e: Events.TickStateChangeEvent) {
    setupFinished = e.tickCounterInitialized
    repaint()
  }

  def popUpStoppingButton() {
    buttonUp = true
    running = false
    stopping = false
    repaint()
    setCursor(null)
  }

  /// source code
  private def chooseDisplayName = if (name == "") displayName(getSourceName) else displayName(name)

  // behold the mighty regular expression
  private def getSourceName = innerSource().trim.replaceAll("\\s+", " ")
  override def innerSource(newInnerSource:String){
    super.innerSource(newInnerSource)
    chooseDisplayName
  }
  def wrapSource = innerSource()
  def wrapSource(newInnerSource:String){
    if(newInnerSource != "" && newInnerSource != innerSource()){
      this.innerSource(newInnerSource)
      recompile()
    }
  }

  def recompile(){
    val header = "to __button [] " + buttonType.toHeaderCode + (if(forever) " loop [ " else "")
    val footer = "\n" + // protect against comments
      (if(forever) "__foreverbuttonend ] " else "__done ") + "end"
    new Events.RemoveJobEvent(this).raise(this)
    source(header, innerSource, footer)
    chooseDisplayName
  }

  /// sizing
  override def getMinimumSize = new Dimension(55, 33)
  override def getPreferredSize(font: Font) = {
    val size = getMinimumSize
    size.width = StrictMath.max(size.width, getFontMetrics(font).stringWidth(displayName) + 28)
    size.height = StrictMath.max(size.height,
      getFontMetrics(font).getMaxDescent() + getFontMetrics(font).getMaxAscent() + 12)
    size
  }

  /// painting
  override def paintComponent(g: Graphics) {
    val g2d = g.asInstanceOf[Graphics2D]
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    def drawAsUp = buttonUp && !running
    def getPaintColor = if (drawAsUp) getBackground else getForeground
    def paintButtonRectangle(g: Graphics) {
      g.setColor(getPaintColor)
      g.fillRect(0, 0, getWidth(), getHeight())
      def renderImages(g: Graphics, dark: Boolean) {
        def maybePaintForeverImage() {
          if (forever) {
            val image = if (dark) FOREVER_GRAPHIC_DARK else FOREVER_GRAPHIC
            image.paintIcon(this, g, getWidth() - image.getIconWidth - 4, getHeight() - image.getIconHeight - 4)
          }
        }
        def maybePaintAgentImage() {
          buttonType.img(dark).map(_.paintIcon(this, g, 3, 3))
        }
        maybePaintForeverImage()
        maybePaintAgentImage()
      }
      renderImages(g, !drawAsUp)
    }
    def paintKeyboardShortcut(g: Graphics) {
      if (actionKeyString != "") {
        val ax = getSize().width - 4 - g.getFontMetrics.stringWidth(actionKeyString)
        val ay = g.getFontMetrics.getMaxAscent + 2
        if (drawAsUp) g.setColor(if (keyEnabled) Color.BLACK else Color.GRAY)
        else g.setColor(if (keyEnabled && forever) getBackground else Color.BLACK)
        g.drawString(actionKeyString, ax - 1, ay)
      }
    }
    def paintButtonText(g: Graphics) {
      val stringWidth = g.getFontMetrics.stringWidth(displayName)
      val color = {
        val c = if (drawAsUp) getForeground else getBackground
        if(error != null) c else if (disabledWaitingForSetup) Color.GRAY else c
      }
      g.setColor(color)
      val availableWidth = getSize().width - 8
      val shortString = org.nlogo.awt.Fonts.shortenStringToFit(displayName, availableWidth, g.getFontMetrics)
      val nx = if (stringWidth > availableWidth) 4 else (getSize().width / 2) - (stringWidth / 2)
      val labelHeight = g.getFontMetrics.getMaxDescent + g.getFontMetrics.getMaxAscent
      val ny = (getSize().height / 2) + (labelHeight / 2)
      g.drawString(shortString, nx, ny)  //if (disabledWaitingForSetup) Color.GRAY
      setToolTipText(if (displayName != shortString) displayName else null)
    }
    paintButtonRectangle(g)
    paintButtonText(g)
    paintKeyboardShortcut(g)
  }

  // saving and loading
  override def save = {
    val s = new StringBuilder()
    s.append("BUTTON\n")
    s.append(getBoundsString)

    if(name.trim != "") s.append(name + "\n") else s.append("NIL\n")

    if(innerSource() != null  && innerSource().trim != "")
      s.append(ModelReader.stripLines(innerSource()) + "\n")
    else s.append("NIL\n")

    if(forever) s.append("T\n") else s.append("NIL\n")

    s.append(1 + "\n") // for compatability
    s.append("T\n")  // show display name

    // agent type
    s.append(buttonType.name.toUpperCase + "\n")

    // former autoUpdate flag
    s.append("NIL\n")

    if(actionKey == 0 || actionKey == ' ') s.append("NIL\n")
    else s.append(actionKey + "\n")

    s.append("NIL\n") // intermediateupdates were optional for a short time
    s.append("NIL\n") // being affected by the speed slider was optional for a short time

    // go time only button.
    s.append((if(goTime) 0 else 1) + "\n")

    s.toString
  }

  override def load(strings:Array[String], helper: Widget.LoadHelper) = {
    forever = strings(7) == "T"
    // ButtonType handles converting the saved button type name into a ButtonType object.
    if (10 < strings.length) buttonType = ButtonType(strings(10).toLowerCase)

    // strings[11] used to control the autoUpdate flag,
    // but that's now a global setting
    if(strings.length > 12 && strings(12) != "NIL") actionKey = strings(12).charAt(0)

    if(strings.length > 15) goTime = strings(15) == "0"

    // strings[13] and strings[14] were temporarily added, then gotten rid of,
    // so we just skip those lines
    name = if(strings(5) != "NIL") strings(5) else ""

    val source = org.nlogo.api.ModelReader.restoreLines(strings(6))
    wrapSource(helper.convert(if(source=="NIL") "" else source, false))

    val List(x1,y1,x2,y2) = strings.drop(1).take(4).map(_.toInt).toList
    setSize(x2 - x1, y2 - y1)
    chooseDisplayName
    this
  }
}
