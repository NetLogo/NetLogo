// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Cursor, Dimension, Font, Graphics, Graphics2D, RenderingHints }
import java.awt.event.{ MouseEvent, MouseListener, MouseMotionListener }
import java.awt.image.FilteredImageSource
import javax.swing.ImageIcon

import org.nlogo.api.{ Editable, MersenneTwisterFast, Options }
import org.nlogo.awt.{ DarkenImageFilter, Mouse }, Mouse.hasButton1
import org.nlogo.core.{ AgentKind, Button => CoreButton, I18N }
import org.nlogo.nvm.Procedure
import org.nlogo.swing.Utils.icon

object ButtonWidget {

  val FOREVER_GRAPHIC: ImageIcon = icon("/images/forever.png")

  object ButtonType {

    // the 4 possible button types
    val ObserverButton = ButtonType("observer", AgentKind.Observer, img = None, darkImg = None)
    val TurtleButton = ButtonType("turtle", AgentKind.Turtle, "/images/turtle.gif")
    val LinkButton = ButtonType("link", AgentKind.Link, "/images/link.gif")
    val PatchButton = ButtonType("patch", AgentKind.Patch, "/images/patch.gif")

    val buttonTypes = List(ObserverButton, TurtleButton, LinkButton, PatchButton)

    def darkImage(image: ImageIcon) = new ImageIcon(java.awt.Toolkit.getDefaultToolkit.createImage(
      new FilteredImageSource(image.getImage.getSource, new DarkenImageFilter(0.5))))

    private def apply(headerCode:String, agentKind:AgentKind, imagePath: String): ButtonType = {
      val img = icon(imagePath)
      new ButtonType(headerCode, agentKind, Some(img), Some(darkImage(img)))
    }
    def apply(c:AgentKind): ButtonType = {
      buttonTypes.find(_.agentKind == c).getOrElse(ObserverButton) //TODO or should we say error("bad agent class")
    }
    def apply(name:String): ButtonType = {
      buttonTypes.find(_.name == name).getOrElse(ObserverButton) //TODO or should we say error("bad agent name")
    }
    def getAgentClass(name:String) = {
      if(name == "NIL") ObserverButton.agentKind
      //TODO or should we say error("bad agent name")
      buttonTypes.find(_.name == name).map(_.agentKind).getOrElse(ObserverButton.agentKind)
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
  case class ButtonType(name: String, agentKind:AgentKind,
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

  type WidgetModel = CoreButton

  private var buttonType: ButtonType = ButtonType.ObserverButton

  locally {
    addMouseListener(this)
    addMouseMotionListener(this)

    backgroundColor = InterfaceColors.BUTTON_BACKGROUND
  }

  // buttonType now controls the agentKind. no one should ever be setting
  // agentKind from outside of this class anyway.
  // the ui edits work through agent options, which now just set the button type
  override def kind = buttonType.agentKind
  override def agentKind(c:AgentKind) { /* ignoring, no one should call this. */ }
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
    if (!buttonUp) {
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

  private var hover = false

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

  def mouseEntered(e: MouseEvent) {
    hover = true

    repaint()
  }

  def mouseExited(e: MouseEvent) {
    hover = false

    repaint()
  }

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
    chooseDisplayName()
  }

  override def procedure_=(p: Procedure): Unit = {
    super.procedure_=(p)
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
          new Events.AddJobEvent(this, agents, procedure).raise(this)
        }
      }
    }
  }

  def handle(e: Events.JobRemovedEvent) {
    if (e.owner == this) {
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
  private def chooseDisplayName(): Unit = {
    if (name == "")
      displayName(getSourceName)
    else
      displayName(name)
  }

  // behold the mighty regular expression
  private def getSourceName: String = {
    (innerSource: String).trim.replaceAll("\\s+", " ")
  }

  override def innerSource_=(newInnerSource:String): Unit = {
    super.innerSource_=(newInnerSource)
    chooseDisplayName
  }

  def wrapSource: String = innerSource

  def wrapSource(newInnerSource:String) {
    if (newInnerSource != "" && newInnerSource != innerSource) {
      this.innerSource = newInnerSource
      recompile()
    }
  }

  def recompile(){
    val header = "to __button [] " + buttonType.toHeaderCode + (if(forever) " loop [ " else "")
    val footer = "\n" + // protect against comments
      (if(forever) "__foreverbuttonend ] " else "__done ") + "end"
    new Events.RemoveJobEvent(this).raise(this)
    source(header, innerSource, footer)
    chooseDisplayName()
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
    val drawAsUp = buttonUp && !running
    def paintButtonRectangle(g: Graphics) {
      backgroundColor =
        if (drawAsUp) {
          if (hover)
            InterfaceColors.BUTTON_BACKGROUND_HOVER
          else
            InterfaceColors.BUTTON_BACKGROUND
        }

        else {
          if (hover)
            InterfaceColors.BUTTON_BACKGROUND_PRESSED_HOVER
          else
            InterfaceColors.BUTTON_BACKGROUND_PRESSED
        }
      super.paintComponent(g)
      def renderImages(g: Graphics, dark: Boolean) {
        def maybePaintForeverImage() {
          if (forever)
            FOREVER_GRAPHIC.paintIcon(this, g, getWidth - FOREVER_GRAPHIC.getIconWidth - 4,
                                      getHeight - FOREVER_GRAPHIC.getIconHeight - 4)
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
      g.setColor(
        if (error == null && disabledWaitingForSetup)
          Color.GRAY
        else
          InterfaceColors.BUTTON_TEXT
      )
      val availableWidth = getSize().width - 8
      val shortString = org.nlogo.awt.Fonts.shortenStringToFit(displayName, availableWidth, g.getFontMetrics)
      val nx = if (stringWidth > availableWidth) 4 else (getSize().width / 2) - (stringWidth / 2)
      val ny = (getSize().height - g.getFontMetrics.getHeight) / 2 + g.getFontMetrics.getMaxAscent
      g.drawString(shortString, nx, ny)  //if (disabledWaitingForSetup) Color.GRAY
      setToolTipText(if (displayName != shortString) displayName else null)
    }
    paintButtonRectangle(g)
    paintButtonText(g)
    paintKeyboardShortcut(g)
  }

  // saving and loading
  override def model: WidgetModel = {
    val b              = getBoundsTuple
    val savedActionKey = if (actionKey == 0 || actionKey == ' ') None else Some(actionKey)
    CoreButton(
      display = name.potentiallyEmptyStringToOption,
      left = b._1, top = b._2, right = b._3, bottom = b._4,
      source    = innerSource.potentiallyEmptyStringToOption,
      forever   = forever,        buttonKind             = buttonType.agentKind,
      actionKey = savedActionKey, disableUntilTicksStart = goTime)
  }

  override def load(button: WidgetModel): Object = {
    forever = button.forever
    buttonType = ButtonType(button.buttonKind)

    button.actionKey.foreach(k => actionKey = k)

    goTime = button.disableUntilTicksStart
    name = button.display.optionToPotentiallyEmptyString

    button.source.foreach(wrapSource)

    setSize(button.right - button.left, button.bottom - button.top)
    chooseDisplayName()
    this
  }
}
